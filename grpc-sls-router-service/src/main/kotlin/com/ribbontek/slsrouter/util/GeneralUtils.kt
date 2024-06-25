package com.ribbontek.slsrouter.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

fun String.toUUID(): UUID = UUID.fromString(this)

@Suppress("UNCHECKED_CAST")
fun <T> Class<T>.newInstanceOf(): T? {
    return constructors.singleOrNull { it.parameterCount == 0 }?.newInstance() as T?
}

fun <T : Any> T.logger(): Logger = LoggerFactory.getLogger(this::class.java)
