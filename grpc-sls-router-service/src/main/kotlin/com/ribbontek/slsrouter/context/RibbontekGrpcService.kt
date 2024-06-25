package com.ribbontek.slsrouter.context

import net.devh.boot.grpc.server.service.GrpcService
import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@GrpcService
@NoArg
annotation class RibbontekGrpcService(
    val basePath: String = "",
    val summary: String = ""
)
