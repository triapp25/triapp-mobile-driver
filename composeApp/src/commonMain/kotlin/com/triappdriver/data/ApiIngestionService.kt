package com.triappdriver.data


import com.triappdriver.data.dtos.LocationRequest
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST

interface ApiIngestionService {

    @POST("/api/v1/locations")
    suspend fun sendLocation(
        @Body body: LocationRequest
    )
}