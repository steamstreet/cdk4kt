import com.squareup.kotlinpoet.*
import software.amazon.awscdk.services.lambda.FunctionProps
import software.constructs.Construct
import java.io.File
import java.io.IOException
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubclassOf

class KCDKBuilder(val outputPackage: String, val baseDir: File) {
    val files = HashMap<String, FileSpec.Builder>()
    val builderFunctionNames = HashSet<String>()

    fun example() {

        FunctionProps.builder().apply {

        }.build()
    }

    private fun fileForGroup(group: String): FileSpec.Builder {
        return files.getOrPut(group) {
            FileSpec.builder("${outputPackage}.$group", group).apply {
                addAnnotation(
                    AnnotationSpec.builder(Suppress::class)
                        .useSiteTarget(AnnotationSpec.UseSiteTarget.FILE)
                        .addMember("%S", "unused")
                        .addMember("%S", "FunctionName")
                        .build()
                )
            }
        }
    }

    private val List<Annotation>.isDeprecated: Boolean
        get() = any {
            it.annotationClass.qualifiedName == "java.lang.Deprecated"
        }
    private val KClass<*>.isDeprecated: Boolean get() = annotations.isDeprecated

    private fun addBuilderShortcuts(group: String, clazz: KClass<*>) {
        if (clazz.isDeprecated) return

        clazz.functions.filter {
            it.parameters.size == 2
        }.forEach { parameterFunction ->
            val input = parameterFunction.parameters.last().type.classifier as? KClass<*>
            val builderFunction = input?.functions?.find { it.name == "builder" }

            if (builderFunction != null && builderFunction.parameters.isEmpty()
                && !builderFunction.annotations.isDeprecated
                && !input.isDeprecated
            ) {
                val id = "${clazz.qualifiedName}.${parameterFunction.name}"
                if (!builderFunctionNames.contains(id)) {
                    builderFunctionNames.add(id)

                    // we have a single parameter with a builder function. We can add a builder function
                    fileForGroup(group).addFunction(
                        FunSpec.builder(parameterFunction.name)
                            .receiver(clazz)
                            .apply {
                                modifiers += KModifier.PUBLIC
                                addParameter(
                                    "props",
                                    LambdaTypeName.get(
                                        builderFunction.returnType.asTypeName(),
                                        returnType = Unit::class.asTypeName()
                                    )
                                )
                                addStatement(
                                    "${parameterFunction.name}(%T.builder().apply(props).build())",
                                    input
                                )
                            }.build()
                    )
                }
            }
        }
    }

    fun buildConstruct(clazz: KClass<*>) {
        val group = clazz.qualifiedName
            ?.substringAfter("software.amazon.awscdk.services.", "")
            ?.substringBefore(".", "")?.ifBlank { null } ?: return

        val constructorsFile = fileForGroup(group)

        if (clazz.isSubclassOf(software.amazon.jsii.Builder::class)) {
            addBuilderShortcuts(group, clazz)
            return
        }

        val builderFunction = clazz.members.find { it.name == "builder" }
        if (builderFunction != null) {
            if (!builderFunction.annotations.isDeprecated) {
                constructorsFile.addFunction(
                    FunSpec.builder(clazz.simpleName!!).returns(clazz)
                        .apply {
                            modifiers += KModifier.PUBLIC
                            addParameter(
                                "props",
                                LambdaTypeName.get(
                                    builderFunction.returnType.asTypeName(),
                                    returnType = Unit::class.asTypeName()
                                )
                            )
                            addStatement(
                                "return %T.builder().apply(props).build()",
                                clazz
                            )
                        }.build()
                )
            }
            return
        }

        clazz.constructors.filter {
            it.visibility == KVisibility.PUBLIC && !it.annotations.isDeprecated
        }.forEach { constructor ->
            val first = constructor.parameters.firstOrNull()
            if (constructor.parameters.size == 1) {
                val propsType = (first?.type?.classifier as? KClass<*>)
                val builder = propsType?.members?.find { it.name == "builder" }

                if (builder != null) {
                    constructorsFile.addFunction(
                        FunSpec.builder(clazz.simpleName!!).returns(clazz)
                            .apply {
                                modifiers += KModifier.PUBLIC
                                addParameter(
                                    "props",
                                    LambdaTypeName.get(
                                        builder.returnType.asTypeName(),
                                        returnType = Unit::class.asTypeName()
                                    )
                                )
                                addStatement(
                                    "return %T(%T.builder().apply(props).build())",
                                    clazz,
                                    propsType
                                )
                            }.build()
                    )
                }
            } else {
                val firstType = (first?.type?.classifier as? KClass<*>)
                if (firstType?.isSubclassOf(Construct::class) == true) {
                    val second = constructor.parameters.getOrNull(1)
                    val isIdConstructor = (second?.type?.classifier as? KClass<*>)?.isSubclassOf(String::class) == true

                    val propsParam = constructor.parameters.getOrNull(2)
                    val propsType = (propsParam?.type?.classifier as? KClass<*>)
                    val builder = propsType?.members?.find { it.name == "builder" }

                    if (isIdConstructor) {
                        constructorsFile.addFunction(
                            FunSpec.builder(clazz.simpleName!!).returns(clazz)
                                .receiver(firstType)
                                .apply {
                                    modifiers += KModifier.PUBLIC

                                    addParameter("id", String::class)

                                    if (builder != null) {
                                        addParameter(
                                            "props",
                                            LambdaTypeName.get(
                                                builder.returnType.asTypeName(),
                                                returnType = Unit::class.asTypeName()
                                            )
                                        )
                                        addStatement(
                                            "return %T(this, id, %T.builder().apply(props).build())",
                                            clazz,
                                            propsType
                                        )
                                    } else {
                                        addStatement("return %T(this, id)", clazz)
                                    }
                                }.build()
                        )
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    fun getClassNamesFromJarFile(givenFile: File): Set<String> {
        val classNames: MutableSet<String> = HashSet()
        JarFile(givenFile).use { jarFile ->
            val e: Enumeration<JarEntry> = jarFile.entries()
            while (e.hasMoreElements()) {
                val jarEntry: JarEntry = e.nextElement()
                if (jarEntry.getName().endsWith(".class")) {
                    val className: String = jarEntry.name
                        .replace("/", ".")
                        .replace(".class", "")
                    classNames.add(className)
                }
            }
            return classNames
        }
    }

    fun run(jarPath: String) {
        val classes = getClassNamesFromJarFile(File(jarPath))
        classes.forEach {
            try {
                val clazz = Class.forName(it)
                buildConstruct(clazz.kotlin)
            } catch (_: Throwable) {
            }
        }
        files.forEach { (key, value) ->
            value.build().writeTo(baseDir)
        }
    }
}

fun main() {
    val classpath = System.getProperty("java.class.path")
    val classPathValues = classpath.split(File.pathSeparator.toRegex()).dropLastWhile { it.isEmpty() }
        .toTypedArray()
    for (classPath in classPathValues) {
        val fileName = classPath.substringAfterLast("/")
        if (fileName.matches(Regex("aws-cdk-lib-(.*)\\.jar"))) {
            val output = File("../kotlin-cdk-lib/build/cdk")
            output.mkdirs()
            output.mkdir()
            println(output.canonicalPath)
            KCDKBuilder("com.steamstreet.cdk.kotlin", output).run(classPath)
        }
    }
//    KCDKBuilder("com.steamstreet.cdk.kotlin", File("kotlin-cdk/kotlin-cdk-lib/build/cdk")).run()
}