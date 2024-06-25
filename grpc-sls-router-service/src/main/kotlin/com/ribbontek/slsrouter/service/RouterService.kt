package com.ribbontek.slsrouter.service

import com.google.protobuf.Empty
import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import com.google.protobuf.util.JsonFormat.TypeRegistry
import com.ribbontek.slsrouter.context.RibbontekGrpcService
import com.ribbontek.slsrouter.documentation.Rest2GrpcEndpoint
import com.ribbontek.slsrouter.exception.ApiException
import com.ribbontek.slsrouter.exception.AuthenticationException
import com.ribbontek.slsrouter.exception.BadRequestException
import com.ribbontek.slsrouter.exception.ConflictException
import com.ribbontek.slsrouter.exception.NotFoundException
import com.ribbontek.slsrouter.exception.ValidationException
import com.ribbontek.slsrouter.util.OK_STATUS_CODE
import com.ribbontek.slsrouter.util.TryResult
import com.ribbontek.slsrouter.util.TryResultFailure
import com.ribbontek.slsrouter.util.TryResultSuccess
import com.ribbontek.slsrouter.util.logger
import com.ribbontek.slsrouter.util.toJson
import com.ribbontek.slsrouter.util.tryRun
import com.ribbontek.stubs.slsrouter.router.RouteCommand
import com.ribbontek.stubs.slsrouter.router.RouteResponse
import com.ribbontek.stubs.slsrouter.router.routeResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.reflections.Reflections
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.io.IOException
import java.lang.reflect.Method
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

interface RouterService {
    fun route(request: RouteCommand): RouteResponse
}

@Service
class RouterServiceImpl(
    private val applicationContext: ApplicationContext
) : RouterService {
    private val log = logger()

    private val services: Map<String, Rest2GrpcModel> = Reflections("com.ribbontek").getTypesAnnotatedWith(RibbontekGrpcService::class.java)
        .mapNotNull { clazz ->
            clazz.getAnnotation(RibbontekGrpcService::class.java)?.let { ribbontekGrpcService ->
                clazz.methods.mapNotNull { method ->
                    method.getAnnotation(Rest2GrpcEndpoint::class.java)?.let { endpoint ->
                        generateKey(ribbontekGrpcService, endpoint) to Rest2GrpcModel(
                            clazz,
                            method,
                            ribbontekGrpcService,
                            endpoint
                        )
                    }
                }
            }
        }.flatten().toMap()

    override fun route(request: RouteCommand): RouteResponse {
        return when (val result = tryRunRouteCommand(request)) {
            is TryResultSuccess -> result.handleSuccess()
            is TryResultFailure -> {
                log.error("Handle route exception", result.exception)
                result.handleException()
            }
        }
    }

    private fun tryRunRouteCommand(request: RouteCommand): TryResult<RouteResponseResult> =
        tryRun {
            val route = request.generateKey()
            log.info("Searching for route: $route")
            val rest2GrpcModel = services[route] ?: throw InvalidRouteException("Invalid Path / Method")
            log.info("Found route: $route")
            // TODO: Run any authentication here if the function is authenticated
            val result = rest2GrpcModel.clazzMethod.invoke(
                applicationContext.getBean(rest2GrpcModel.clazz),
                grpcFromJson(request.body, rest2GrpcModel.rest2GrpcEndpoint),
                object : Continuation<Any> {
                    override val context: CoroutineContext = Dispatchers.Default // + SecurityCoroutineContext()
                    override fun resumeWith(result: Result<Any>) = Unit
                }
            ).let {
                if (it is Flow<*>) runBlocking { it.toList() } else it
            }
            // return model and result from calling method
            RouteResponseResult(rest2GrpcModel, grpcToJson(result, rest2GrpcModel.rest2GrpcEndpoint))
        }

    private fun TryResultSuccess<RouteResponseResult>.handleSuccess(): RouteResponse =
        routeResponse {
            statusCode = 200
            body = value.json
            headers.putAll(mapOf("Content-Type" to "application/json"))
        }

    private fun TryResultFailure.handleException(): RouteResponse = when (exception) {
        is InvalidRouteException, is NotFoundException -> {
            routeResponse {
                statusCode = 404
                body = exception.toExceptionMessageResponse().toJson()
                headers.putAll(mapOf("Content-Type" to "application/json"))
            }
        }

        is AuthenticationException -> {
            routeResponse {
                statusCode = 401
                body = exception.toExceptionMessageResponse().toJson()
                headers.putAll(mapOf("Content-Type" to "application/json"))
            }
        }

        is BadRequestException, is ValidationException, is InvalidProtocolBufferException -> {
            routeResponse {
                statusCode = 400
                body = exception.toExceptionMessageResponse().toJson()
                headers.putAll(mapOf("Content-Type" to "application/json"))
            }
        }

        is ConflictException -> {
            routeResponse {
                statusCode = 409
                body = exception.toExceptionMessageResponse().toJson()
                headers.putAll(mapOf("Content-Type" to "application/json"))
            }
        }

        else -> {
            routeResponse {
                statusCode = 500
                body = exception.toExceptionMessageResponse().toJson()
                headers.putAll(mapOf("Content-Type" to "application/json"))
            }
        }
    }

    private fun Throwable.toExceptionMessageResponse(): ExceptionMessageResponse {
        return ExceptionMessageResponse(
            description = message ?: localizedMessage,
            cause = cause?.stackTraceToString()?.take(1000)
        )
    }

    private fun ApiException.toExceptionMessageResponse(): ExceptionMessageResponse {
        return ExceptionMessageResponse(
            description = message ?: localizedMessage,
            cause = cause?.stackTraceToString()?.take(1000)
        )
    }

    private data class Rest2GrpcModel(
        val clazz: Class<*>,
        val clazzMethod: Method,
        val ribbontekGrpcService: RibbontekGrpcService,
        val rest2GrpcEndpoint: Rest2GrpcEndpoint
    )

    private class InvalidRouteException(message: String) : ApiException(message)

    private data class ExceptionMessageResponse(
        val description: String,
        val cause: String?
    )

    private data class RouteResponseResult(
        val rest2GrpcModel: Rest2GrpcModel,
        val json: String
    )

    private fun getMessageBuilder(clazz: Class<out Message>): Message.Builder {
        try {
            return clazz.getMethod("newBuilder").invoke(clazz) as Message.Builder
        } catch (ex: Exception) {
            throw BadRequestException(
                "Invalid Protobuf Message type: no invocable newBuilder() method on $clazz"
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class)
    fun grpcFromJson(json: String, endpoint: Rest2GrpcEndpoint): Message {
        if (endpoint.request.type == Empty::class) return Empty.getDefaultInstance()
        val builder = TypeRegistry.newBuilder()
        val message = when (endpoint.request.type) {
            !is Flow<*> -> getMessageBuilder(endpoint.request.type.java as Class<out Message>)
            else -> getMessageBuilder(Message::class.java)
        }
        builder.add(message.descriptorForType)
        endpoint.request.subTypes.forEach { builder.add(getMessageBuilder(it.java as Class<out Message>).descriptorForType) }
        JsonFormat.parser()
            .usingTypeRegistry(builder.build())
            .merge(json, message)
        return message.build()
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class)
    fun grpcToJson(result: Any, endpoint: Rest2GrpcEndpoint): String {
        val response = endpoint.responses.find { it.code == OK_STATUS_CODE } ?: throw NotFoundException("Endpoint Response Configuration Not Found")
        if (response.type == Empty::class) return "{}"
        val message = when (response.type) {
            !is Flow<*> -> getMessageBuilder(response.type.java as Class<out Message>)
            else -> getMessageBuilder(Message::class.java)
        }
        val builder = TypeRegistry.newBuilder().add(message.descriptorForType)
        response.subTypes.forEach { builder.add(getMessageBuilder(it.java as Class<out Message>).descriptorForType) }
        return JsonFormat.printer()
            .usingTypeRegistry(builder.build())
            .print(
                getMessageBuilder(response.type.java as Class<out Message>)
                    .mergeFrom((result as GeneratedMessageV3).toByteArray())
                    .build()
            )
    }

    private fun generateKey(ribbontekGrpcService: RibbontekGrpcService, endpoint: Rest2GrpcEndpoint) =
        endpoint.method.name + ":" + ribbontekGrpcService.basePath + endpoint.path

    private fun RouteCommand.generateKey() = "$method:$path"
}
