package com.ribbontek.lambda.router

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.verifyBlocking
import com.ribbontek.lambda.context.AbstractIntegTest
import com.ribbontek.lambda.util.toJson
import com.ribbontek.stubs.slsrouter.router.RouteCommand
import com.ribbontek.stubs.slsrouter.router.routeResponse
import io.grpc.Status
import io.grpc.StatusException
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

class RouterConfigIntegTest : AbstractIntegTest() {

    @Autowired
    private lateinit var router: (APIGatewayProxyRequestEvent) -> APIGatewayProxyResponseEvent

    @Test
    fun `test router endpoint - success route response - success`() {
        val argumentCaptor = argumentCaptor<RouteCommand>()
        val expectedRouteResponse = routeResponse {
            statusCode = HttpStatus.OK.value()
            body = "{}"
            headers.putAll(mapOf("Content-Type" to "application/json"))
        }
        routerServiceCoroutineStub.stub {
            onBlocking { route(argumentCaptor.capture(), any()) }.thenReturn(expectedRouteResponse)
        }
        val request = createMockRequest()
        // run the router function
        val result = router.invoke(request)

        assertThat(result.statusCode, equalTo(expectedRouteResponse.statusCode))
        assertThat(result.body, equalTo(expectedRouteResponse.body))
        assertThat(result.headers, equalTo(expectedRouteResponse.headersMap))

        val capturedRouteCommand = argumentCaptor.firstValue
        assertThat(capturedRouteCommand.path, equalTo(request.pathParameters["path"]))
        assertThat(capturedRouteCommand.body, equalTo(request.body))
        assertThat(capturedRouteCommand.headersMap, equalTo(request.headers))
        assertThat(capturedRouteCommand.method, equalTo(request.httpMethod))

        verifyBlocking(routerServiceCoroutineStub) { route(any(), any()) }
    }

    @Test
    fun `test router endpoint - error route response - success`() {
        val argumentCaptor = argumentCaptor<RouteCommand>()
        val expectedRouteResponse = routeResponse {
            statusCode = HttpStatus.BAD_REQUEST.value()
            body = ExceptionMessageResponse(
                description = "Test Description",
                cause = RuntimeException("Something went wrong").cause?.stackTraceToString()?.take(1000)
            ).toJson()
            headers.putAll(mapOf("Content-Type" to "application/json"))
        }
        routerServiceCoroutineStub.stub {
            onBlocking { route(argumentCaptor.capture(), any()) }.thenReturn(expectedRouteResponse)
        }
        val request = createMockRequest()
        // run the router function
        val result = router.invoke(request)

        assertThat(result.statusCode, equalTo(expectedRouteResponse.statusCode))
        assertThat(result.body, equalTo(expectedRouteResponse.body))
        assertThat(result.headers, equalTo(expectedRouteResponse.headersMap))

        val capturedRouteCommand = argumentCaptor.firstValue
        assertThat(capturedRouteCommand.path, equalTo(request.pathParameters["path"]))
        assertThat(capturedRouteCommand.body, equalTo(request.body))
        assertThat(capturedRouteCommand.headersMap, equalTo(request.headers))
        assertThat(capturedRouteCommand.method, equalTo(request.httpMethod))

        verifyBlocking(routerServiceCoroutineStub) { route(any(), any()) }
    }

    @Test
    fun `test router endpoint - stub throws exception - error`() {
        routerServiceCoroutineStub.stub {
            onBlocking { route(any(), any()) }.thenAnswer { throw StatusException(Status.UNIMPLEMENTED) }
        }
        // run the router function
        assertThrows<StatusException> { router.invoke(createMockRequest()) }

        verifyBlocking(routerServiceCoroutineStub) { route(any(), any()) }
    }

    private fun createMockRequest(): APIGatewayProxyRequestEvent {
        return APIGatewayProxyRequestEvent()
            .withHttpMethod("POST")
            .withPath("/router/product-api/v1/products/_paged")
            .withPathParameters(mapOf("path" to "/product-api/v1/products/_paged"))
            .withBody("{\"number\":0,\"size\":100,\"query\":{}}")
            .withHeaders(
                mapOf(
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer some_token"
                )
            )
    }

    private data class ExceptionMessageResponse(
        val description: String,
        val cause: String?
    )
}
