package com.steamstreet.cdk.extensions

import com.steamstreet.cdk.kotlin.events.EventPattern
import com.steamstreet.cdk.kotlin.events.Rule
import com.steamstreet.cdk.kotlin.iam.PolicyDocument
import com.steamstreet.cdk.kotlin.iam.PolicyStatement
import com.steamstreet.cdk.kotlin.iam.Role
import com.steamstreet.cdk.kotlin.pipes.*
import software.amazon.awscdk.services.dynamodb.Table
import software.amazon.awscdk.services.events.IEventBus
import software.amazon.awscdk.services.events.IRuleTarget
import software.amazon.awscdk.services.events.targets.LambdaFunction
import software.amazon.awscdk.services.iam.Effect
import software.amazon.awscdk.services.iam.ServicePrincipal
import software.amazon.awscdk.services.lambda.IFunction
import software.constructs.Construct

public class DynamoPipeConfig {
    public lateinit var table: Table
    public lateinit var source: String
    public lateinit var detailType: String
    public lateinit var eventBus: IEventBus

    public var batchSize: Int = 5
    public var retries: Int = 3

    public var startingPosition: String = "LATEST"

    public var onPartialFailure: String = "AUTOMATIC_BISECT"
}

public class DynamoPipe(scope: Construct, id: String, props: DynamoPipeConfig) : Construct(scope, id) {
    private val pipeProps = props

    init {
        val dataTablePipeRole = Role("PipeRole") {
            assumedBy(ServicePrincipal("pipes"))
            inlinePolicies(
                mapOf(
                    "EventBusPutEvents" to PolicyDocument {
                        statements(
                            listOf(
                                PolicyStatement {
                                    effect(Effect.ALLOW)
                                    actions(listOf("events:PutEvents"))
                                    resources(listOf(props.eventBus.eventBusArn))
                                },
                                PolicyStatement {
                                    effect(Effect.ALLOW)
                                    actions(
                                        listOf(
                                            "dynamodb:GetRecords",
                                            "dynamodb:GetShardIterator",
                                            "dynamodb:DescribeStream",
                                            "dynamodb:ListStreams"
                                        )
                                    )
                                    resources(
                                        listOf(
                                            props.table.tableStreamArn
                                        )
                                    )
                                }
                            )
                        )
                    }
                ))
        }

        CfnPipe("TablePipe") {
            source(props.table.tableStreamArn)
            sourceParameters {
                this.dynamoDbStreamParameters(PipeSourceDynamoDBStreamParametersProperty {
                    batchSize(props.batchSize)
                    startingPosition(props.startingPosition)
                    onPartialBatchItemFailure(props.onPartialFailure)
                    maximumRetryAttempts(props.retries)
                })
            }
            roleArn(dataTablePipeRole.roleArn)

            target(props.eventBus.eventBusArn)
            targetParameters {
                eventBridgeEventBusParameters {
                    source(props.source)
                    detailType(props.detailType)
                }
            }
        }
    }


    /**
     * Create a rule to send events from this pipe to a target.
     */
    public fun addTarget(id: String, target: IRuleTarget) {
        Rule(id) {
            eventBus(pipeProps.eventBus)
            eventPattern(EventPattern {
                detailType(listOf(pipeProps.detailType))
                source(listOf(pipeProps.source))
            })
        }.also {
            it.addTarget(target)
        }
    }

    /**
     * Create a rule to send events from this pipe to a lambda.
     */
    public fun addTarget(id: String, function: IFunction) {
        addTarget(id, LambdaFunction(function))
    }
}

/**
 * Optimized constructor of a DynamoPipe.
 */
public fun Construct.DynamoPipe(
    id: String, table: Table, eventBus: IEventBus,
    eventSource: String, eventDetailType: String,
    config: DynamoPipeConfig.() -> Unit = {}
): DynamoPipe =
    DynamoPipe(this, id, DynamoPipeConfig().apply {
        this.table = table
        this.eventBus = eventBus
        this.source = eventSource
        this.detailType = eventDetailType
        config()
    })