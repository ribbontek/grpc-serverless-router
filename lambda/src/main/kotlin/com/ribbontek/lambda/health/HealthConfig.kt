package com.ribbontek.lambda.health

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.ribbontek.lambda.util.logger
import com.ribbontek.lambda.util.toJson
import io.grpc.health.v1.HealthCheckRequest
import io.grpc.health.v1.HealthCheckResponse
import io.grpc.health.v1.HealthCheckResponse.ServingStatus.SERVING
import io.grpc.health.v1.HealthGrpc.HealthBlockingStub
import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.client.inject.GrpcClientBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

data class HealthResponse(val status: HealthStatusEnum)

enum class HealthStatusEnum {
    UP, DOWN
}

/**
 * This lambda function checks connectivity with main server
 */
@Configuration
@GrpcClientBean(clazz = HealthBlockingStub::class, beanName = "healthStub", client = GrpcClient("clientstub"))
class HealthConfig {

    @Autowired
    private lateinit var healthStub: HealthBlockingStub
    private val log = logger()

    @Bean
    fun health(): (APIGatewayProxyRequestEvent) -> APIGatewayProxyResponseEvent {
        return { event ->
            log.info("Found event {}", event)
            val result = checkServerConnectivity()
            APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(mapOf("Content-Type" to "application/json"))
                .withBody(HealthResponse(status = result).toJson())
        }
    }

    private fun checkServerConnectivity(): HealthStatusEnum {
        val result = try {
            healthStub.check(HealthCheckRequest.getDefaultInstance())
        } catch (ex: Exception) {
            log.error("Caught exception", ex)
            HealthCheckResponse.getDefaultInstance()
        }
        return when (result.status) {
            SERVING -> HealthStatusEnum.UP
            else -> HealthStatusEnum.DOWN
        }
    }
}
