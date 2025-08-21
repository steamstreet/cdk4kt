import kotlin.reflect.full.isSubclassOf

fun main() {
    println("=== Analyzing Queue, Alarm, Rule in CDK 2.170.0 ===\n")
    
    val testClasses = listOf(
        "software.amazon.awscdk.services.sqs.Queue",
        "software.amazon.awscdk.services.cloudwatch.Alarm", 
        "software.amazon.awscdk.services.events.Rule"
    )
    
    for (className in testClasses) {
        val clazz = Class.forName(className).kotlin
        println("${clazz.simpleName}: has builder = ${clazz.members.any { it.name == "builder" }}")
    }
}
