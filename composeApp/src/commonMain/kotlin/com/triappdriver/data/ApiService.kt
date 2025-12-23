package com.triappdriver.data


import com.triappdriver.data.dtos.CardBody
import com.triappdriver.data.dtos.CardDto
import com.triappdriver.data.dtos.LocationRequest
import com.triappdriver.data.dtos.ProductDto
import com.triappdriver.data.dtos.RatingBody
import com.triappdriver.data.dtos.RideStatusRequestDTO
import com.triappdriver.data.dtos.RideCancelRequestDTO
import com.triappdriver.data.dtos.RideChooseRequestDTO
import com.triappdriver.data.dtos.RideDTO
import com.triappdriver.data.dtos.RideRequestDTO
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path

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

    @POST("/trips")
    suspend fun rideRequest(
        @Body body: RideRequestDTO
    ): RideDTO

    @POST("/trips/{tripId}/status/accepted")
    suspend fun rideAccepted(
        @Path("tripId") tripId: String,
        @Body body: RideStatusRequestDTO
    )

    @POST("/trips/{tripId}/status/rejected")
    suspend fun rideRejected(
        @Path("tripId") tripId: String,
        @Body body: RideStatusRequestDTO
    )

    @POST("/trips/{tripId}/status/on-going")
    suspend fun rideOnGoing(
        @Path("tripId") tripId: String,
    )

    @POST("/trips/{tripId}/cancel")
    suspend fun rideCancel(
        @Path("tripId") tripId: String,
        @Body body: RideCancelRequestDTO
    )

    @POST("/trips/{tripId}/choose-category")
    suspend fun rideChooseCategory(
        @Path("tripId") tripId: String,
        @Body body: RideChooseRequestDTO
    )

    @POST("/api/v1/locations")
    suspend fun sendLocation(
        @Body body: LocationRequest
    )
}