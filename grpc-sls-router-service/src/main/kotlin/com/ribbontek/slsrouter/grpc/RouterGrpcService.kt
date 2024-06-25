package com.ribbontek.slsrouter.grpc

import com.ribbontek.slsrouter.service.RouterService
import com.ribbontek.stubs.slsrouter.router.RouteCommand
import com.ribbontek.stubs.slsrouter.router.RouteResponse
import com.ribbontek.stubs.slsrouter.router.RouterServiceGrpcKt.RouterServiceCoroutineImplBase
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService // no documentation required for router
class RouterGrpcService(
    private val routerService: RouterService
) : RouterServiceCoroutineImplBase() {
    override suspend fun route(request: RouteCommand): RouteResponse {
        return routerService.route(request)
    }
}
