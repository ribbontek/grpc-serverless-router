package com.ribbontek.slsrouter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GrpcSlsRouterApplication

fun main(args: Array<String>) {
    runApplication<GrpcSlsRouterApplication>(*args)
}
