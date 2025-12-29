package com.triappdriver.data


import com.triappdriver.data.dtos.CardBody
import com.triappdriver.data.dtos.CardDto
import com.triappdriver.data.dtos.LocationRequest
import com.triappdriver.data.dtos.ProductDto
import com.triappdriver.data.dtos.RatingBody
import com.triappdriver.data.dtos.RideStatusRequestAcceptedDTO
import com.triappdriver.data.dtos.RideCancelRequestDTO
import com.triappdriver.data.dtos.RideChooseRequestDTO
import com.triappdriver.data.dtos.RideDTO
import com.triappdriver.data.dtos.RideRequestDTO
import com.triappdriver.data.dtos.RideStatusRequestCompletedDTO
import com.triappdriver.data.dtos.RideStatusRequestOnGoingDTO
import com.triappdriver.data.dtos.RideStatusRequestRejectedDTO
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

    @POST("/api/v1/trips")
    suspend fun rideRequest(
        @Body body: RideRequestDTO
    ): RideDTO

    @POST("/api/v1/trips/{tripId}/status/accepted")
    suspend fun rideAccepted(
        @Path("tripId") tripId: String,
        @Body body: RideStatusRequestAcceptedDTO
    )

    @POST("/api/v1/trips/{tripId}/status/rejected")
    suspend fun rideRejected(
        @Path("tripId") tripId: String,
        @Body body: RideStatusRequestRejectedDTO
    )

    @POST("/api/v1/trips/{tripId}/status/on-going")
    suspend fun rideOnGoing(
        @Path("tripId") tripId: String,
        @Body body: RideStatusRequestOnGoingDTO
    )


    @POST("/api/v1/trips/{tripId}/status/completed")
    suspend fun rideCompleted(
        @Path("tripId") tripId: String,
        @Body body: RideStatusRequestCompletedDTO
    )

    @POST("/api/v1/trips/{tripId}/cancel")
    suspend fun rideCancel(
        @Path("tripId") tripId: String,
        @Body body: RideCancelRequestDTO
    )

    @POST("/api/v1/trips/{tripId}/choose-category")
    suspend fun rideChooseCategory(
        @Path("tripId") tripId: String,
        @Body body: RideChooseRequestDTO
    )
}