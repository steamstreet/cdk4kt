package com.steamstreet.cdk.kotlin.extensions

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.steamstreet.cdk.kotlin.cloudwatch.Alarm
import com.steamstreet.cdk.kotlin.cloudwatch.MetricOptions
import com.steamstreet.cdk.kotlin.events.LambdaFunctionProps
import com.steamstreet.cdk.kotlin.events.Rule
import software.amazon.awscdk.Duration
import software.amazon.awscdk.services.cloudwatch.ComparisonOperator
import software.amazon.awscdk.services.cloudwatch.IAlarmAction
import software.amazon.awscdk.services.cloudwatch.TreatMissingData
import software.amazon.awscdk.services.events.RuleTargetInput
import software.amazon.awscdk.services.events.Schedule
import software.amazon.awscdk.services.events.targets.LambdaFunction
import software.amazon.awscdk.services.lambda.Function
import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern

/**
 * Adds an alarm for a failing function.
 */
public fun Function.failureAlarm(
    numFailuresInPeriod: Int, numFailingPeriods: Int, numPeriodsToEvaluate: Int,
    period: kotlin.time.Duration, action: IAlarmAction, okAction: IAlarmAction? = null
) {

    Alarm("FunctionErrorAlarm") {
        comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL_TO_THRESHOLD)
        threshold(numFailuresInPeriod)
        evaluationPeriods(numPeriodsToEvaluate)
        treatMissingData(TreatMissingData.NOT_BREACHING)
        alarmDescription("Error handler for ${this@failureAlarm.functionName}")
        metric(
            metricErrors().with(
                MetricOptions {
                    this.period(Duration.millis(period.inWholeMilliseconds))
                }
            )
        )
        datapointsToAlarm(numFailingPeriods)
    }.also {
        it.addAlarmAction(action)
        if (okAction != null) {
            it.addOkAction(action)
        }
    }
}

/**
 * Adds a cron schedule for this function.
 */
public fun Function.cron(cronExpression: String, input: String? = null) {
    return schedule("cron(${cronExpression})", input)
}

/**
 * Adds a cron schedule for this function.
 */
public fun Function.rate(rate: Duration, input: String? = null) {
    return schedule("rate(${rate.toMinutes()} minutes)", input)
}

/**
 * Adds a schedule for this function.
 */
public fun Function.schedule(schduleExpression: String, input: String? = null) {
    val rule = this.Rule("${schduleExpression.toSlug()}-Schedule") {
        this.schedule(Schedule.expression(schduleExpression))
    }

    rule.addTarget(LambdaFunction(this, LambdaFunctionProps {
        if (input != null) {
            val jsonValue = jacksonObjectMapper().readValue<Map<String, Any>>(input)
            event(RuleTargetInput.fromObject(jsonValue))
        }
    }))
}

private val NON_LATIN = Pattern.compile("[^\\w-]")
private val WHITESPACE = Pattern.compile("[\\s]")

/**
 * Get a unique identifier 'slug' from a name. We'll be using these for
 * identifiers.
 */
public fun String.toSlug(): String {
    val noWhitespace = WHITESPACE.matcher(this).replaceAll("-")
    val normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD)
    val slug = NON_LATIN.matcher(normalized).replaceAll("")
    return slug.lowercase(Locale.ENGLISH)
}