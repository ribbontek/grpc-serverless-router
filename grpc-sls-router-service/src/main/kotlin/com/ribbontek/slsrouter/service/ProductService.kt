package com.ribbontek.slsrouter.service

import com.ribbontek.slsrouter.mapping.toProduct
import com.ribbontek.stubs.slsrouter.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID
import kotlin.random.Random

interface ProductService {
    fun getProducts(): Flow<Product>
}

@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository
) : ProductService {
    override fun getProducts(): Flow<Product> {
        return productRepository.findAll().map { it.toProduct() }.asFlow()
    }
}

@Component
class ProductRepository {
    fun findAll() = (0..99).map { productEntity() }

    private fun productEntity(): ProductEntity {
        return ProductEntity(
            requestId = UUID.randomUUID(),
            quantity = Random.nextInt(1, 1000).toLong(),
            price = Random.nextDouble(1.0, 1000.0).toBigDecimal(),
            sku = UUID.randomUUID().toString(),
            title = alphanumeric(),
            description = alphanumeric(1000)
        )
    }

    private fun alphanumeric(length: Int = 255): String {
        val alphanumeric = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return buildString { repeat(length) { append(alphanumeric.random()) } }
    }
}

class ProductEntity(
    val requestId: UUID,
    var title: String,
    var description: String,
    var quantity: Long,
    var price: BigDecimal,
    var sku: String? = null
)
