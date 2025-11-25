package com.triapp.data


import com.triapp.data.dtos.ProductDto
import com.triapp.data.dtos.StartTaxiBody
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST

interface ApiService {

    @GET("products")
    suspend fun getProducts(): List<ProductDto>

    @POST("taxi")
    suspend fun startTaxi(
        @Body body: StartTaxiBody
    ): String
}