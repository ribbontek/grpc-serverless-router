package com.ribbontek.lambda.router

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.ribbontek.lambda.util.logger
import com.ribbontek.lambda.util.toJson
import com.ribbontek.stubs.slsrouter.router.RouterServiceGrpcKt.RouterServiceCoroutineStub
import com.ribbontek.stubs.slsrouter.router.routeCommand
import kotlinx.coroutines.runBlocking
import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.client.inject.GrpcClientBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * This lambda function routes all http1 paths to a single grpc proxy endpoint
 */
@Configuration
@GrpcClientBean(clazz = RouterServiceCoroutineStub::class, beanName = "routerServiceCoroutineStub", client = GrpcClient("clientstub"))
class RouterConfig {

    @Autowired
    private lateinit var routerServiceCoroutineStub: RouterServiceCoroutineStub
    private val log = logger()

    @Bean
    fun router(): (APIGatewayProxyRequestEvent) -> APIGatewayProxyResponseEvent {
        return { event ->
            log.info("Found event {}", event)
            event.pathParameters["path"]?.let { pathParam ->
                val result = runBlocking {
                    routerServiceCoroutineStub.route(
                        routeCommand {
                            path = pathParam
                            body = event.body
                            headers.putAll(event.headers)
                            method = event.httpMethod
                        }
                    )
                }
                APIGatewayProxyResponseEvent()
                    .withStatusCode(result.statusCode)
                    .withBody(result.body)
                    .withHeaders(result.headersMap)
            } ?: run {
                APIGatewayProxyResponseEvent()
                    .withStatusCode(404)
                    .withBody(ExceptionMessageResponse(description = "Path not found").toJson())
            }
        }
    }

    private data class ExceptionMessageResponse(
        val description: String
    )
}
