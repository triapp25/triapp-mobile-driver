package com.triapp.data


import com.triapp.data.dtos.CardBody
import com.triapp.data.dtos.CardDto
import com.triapp.data.dtos.ProductDto
import com.triapp.data.dtos.RatingBody
import com.triapp.data.dtos.StartTaxiBody
import com.triapp.domain.model.WalletDomainModel
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT

interface ApiService {

    @GET("products")
    suspend fun getProducts(): List<ProductDto>

    @POST("rating")
    suspend fun sendRating(
        @Body body: RatingBody
    )

    @GET("cards")
    suspend fun getCards(): List<CardDto>

    @POST("cards/add")
    suspend fun createCard(
        @Body body: CardBody
    )

    @PUT("cards/update")
    suspend fun updateCard(
        @Body body: CardBody
    )

    @PUT("cards/delete")
    suspend fun deleteCard(
        @Body body: CardBody
    )

    @POST("taxi")
    suspend fun startTaxi(
        @Body body: StartTaxiBody
    ): String
}