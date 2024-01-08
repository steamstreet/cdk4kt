import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileNotFoundException

abstract class GenerateKotlinWrappers : DefaultTask() {
    @get:Input
    abstract val outputDir: Property<String>

    @get:Input
    abstract val outputPackage: Property<String>

    @TaskAction
    fun generate() {
        val cdkJar = findJar()
        KCDKBuilder(
            this.outputPackage.get(),
            File(this.outputDir.get())
        ).run(cdkJar)
    }

    private fun findJar(): String {
        val config = project.configurations.getByName("runtimeClasspath")

        val resolved = config.resolvedConfiguration
        resolved.files.forEach {
            val fileName = it.canonicalPath.substringAfterLast("/")
            if (fileName.matches(Regex("aws-cdk-lib-(.*)\\.jar"))) {
                return it.canonicalPath
            }
        }
        throw FileNotFoundException("Can't find CDK lib on classpath")
    }
}