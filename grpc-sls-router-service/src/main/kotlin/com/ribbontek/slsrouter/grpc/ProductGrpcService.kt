package com.ribbontek.slsrouter.grpc

import com.google.protobuf.Empty
import com.ribbontek.slsrouter.context.RibbontekGrpcService
import com.ribbontek.slsrouter.documentation.R2GHttpMethod.GET
import com.ribbontek.slsrouter.documentation.Rest2GrpcEndpoint
import com.ribbontek.slsrouter.documentation.Rest2GrpcRequest
import com.ribbontek.slsrouter.documentation.Rest2GrpcResponse
import com.ribbontek.slsrouter.service.ProductService
import com.ribbontek.slsrouter.util.OK_STATUS_CODE
import com.ribbontek.stubs.slsrouter.Product
import com.ribbontek.stubs.slsrouter.product.ProductServiceGrpcKt.ProductServiceCoroutineImplBase
import kotlinx.coroutines.flow.Flow

@RibbontekGrpcService(
    summary = "The public product-api that provides access to all the available product",
    basePath = "/product-api/v1"
)
class ProductGrpcService(
    private val productService: ProductService
) : ProductServiceCoroutineImplBase() {
    @Rest2GrpcEndpoint(
        path = "/products",
        method = GET,
        description = "This public endpoint retrieves all available products",
        request = Rest2GrpcRequest(
            type = Empty::class
        ),
        responses = [
            Rest2GrpcResponse(
                code = OK_STATUS_CODE,
                description = "Flow of all available products",
                type = Flow::class,
                subTypes = [Product::class]
            )
        ]
    )
    override fun getProducts(request: Empty): Flow<Product> {
        return productService.getProducts()
    }
}
