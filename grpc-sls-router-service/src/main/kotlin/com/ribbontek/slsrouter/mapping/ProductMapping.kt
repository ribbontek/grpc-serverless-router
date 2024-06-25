package com.ribbontek.slsrouter.mapping

import com.ribbontek.slsrouter.service.ProductEntity
import com.ribbontek.stubs.slsrouter.Product
import com.ribbontek.stubs.slsrouter.product

/**
 * Reusable global mapping functions
 */
fun ProductEntity.toProduct(): Product {
    val source = this
    return product {
        this.requestId = source.requestId.toString()
        this.title = source.title
        this.description = source.description
        this.quantity = source.quantity
        this.price = source.price.toFloat()
        source.sku?.let { this.sku = it }
    }
}
