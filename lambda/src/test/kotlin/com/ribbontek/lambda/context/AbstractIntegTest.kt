package com.ribbontek.lambda.context

import com.ribbontek.stubs.slsrouter.router.RouterServiceGrpcKt.RouterServiceCoroutineStub
import io.grpc.health.v1.HealthGrpc.HealthBlockingStub
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest
abstract class AbstractIntegTest {

    @MockBean
    protected lateinit var healthStub: HealthBlockingStub

    @MockBean
    protected lateinit var routerServiceCoroutineStub: RouterServiceCoroutineStub
}
