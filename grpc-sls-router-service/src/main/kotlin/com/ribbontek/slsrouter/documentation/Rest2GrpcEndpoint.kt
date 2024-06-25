package com.ribbontek.slsrouter.documentation

import com.ribbontek.slsrouter.documentation.R2GAuthentication.NONE
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Rest2GrpcEndpoint(
    val path: String,
    val method: R2GHttpMethod,
    val description: String = "",
    val request: Rest2GrpcRequest,
    val responses: Array<Rest2GrpcResponse> = [],
    val authentication: R2GAuthentication = NONE
)

enum class R2GAuthentication {
    NONE, OAUTH_TOKEN
}

enum class R2GHttpMethod {
    GET, POST, PUT, DELETE
}

annotation class Rest2GrpcRequest(
    val description: String = "",
    val type: KClass<*>,
    val subTypes: Array<KClass<*>> = [],
    val models: Array<KClass<*>> = [] // these are the override models to be used in swagger docs
)

annotation class Rest2GrpcResponse(
    val code: Int,
    val description: String = "",
    val type: KClass<*>,
    val subTypes: Array<KClass<*>> = [],
    val models: Array<KClass<*>> = []
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
annotation class Rest2GrpcModel(
    val name: String = "", // overridable name field/class
    val description: String = "" // description for field/class
)
