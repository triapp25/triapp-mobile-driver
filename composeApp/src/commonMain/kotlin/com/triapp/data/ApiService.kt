package com.triapp.data


import com.triapp.data.dtos.ProductDto
import de.jensklingenberg.ktorfit.http.GET

interface ApiService {

    @GET("products")
    suspend fun getProducts(): List<ProductDto>
}