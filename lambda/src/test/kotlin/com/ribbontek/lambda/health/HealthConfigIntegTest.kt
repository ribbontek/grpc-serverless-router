package com.ribbontek.lambda.health

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.ribbontek.lambda.context.AbstractIntegTest
import com.ribbontek.lambda.health.HealthStatusEnum.DOWN
import com.ribbontek.lambda.health.HealthStatusEnum.UP
import com.ribbontek.lambda.util.toJson
import io.grpc.health.v1.HealthCheckRequest
import io.grpc.health.v1.HealthCheckResponse
import io.grpc.health.v1.HealthCheckResponse.ServingStatus
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired

class HealthConfigIntegTest : AbstractIntegTest() {

    @Autowired
    private lateinit var health: (APIGatewayProxyRequestEvent) -> APIGatewayProxyResponseEvent

    @Test
    fun `test health endpoint - success`() {
        val response = HealthCheckResponse.newBuilder().setStatus(ServingStatus.SERVING).build()
        `when`(healthStub.check(Mockito.any(HealthCheckRequest::class.java))).thenReturn(response)
        // run the health function
        val result = health.invoke(APIGatewayProxyRequestEvent())

        assertThat(result.statusCode, equalTo(200))
        assertThat(result.headers, equalTo(mapOf("Content-Type" to "application/json")))
        assertThat(result.body, equalTo(HealthResponse(status = UP).toJson()))
        verify(healthStub).check(Mockito.any(HealthCheckRequest::class.java))
    }

    @Test
    fun `test health endpoint - error`() {
        val response = HealthCheckResponse.newBuilder().setStatus(ServingStatus.NOT_SERVING).build()
        `when`(healthStub.check(Mockito.any(HealthCheckRequest::class.java))).thenReturn(response)
        // run the health function
        val result = health.invoke(APIGatewayProxyRequestEvent())

        assertThat(result.statusCode, equalTo(200))
        assertThat(result.headers, equalTo(mapOf("Content-Type" to "application/json")))
        assertThat(result.body, equalTo(HealthResponse(status = DOWN).toJson()))
        verify(healthStub).check(Mockito.any(HealthCheckRequest::class.java))
    }
}
