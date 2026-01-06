package com.triappdriver.data


import com.triappdriver.data.dtos.CreateUserRequestDTO
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST

interface ApiProfileService {

    @POST("/drivers")
    suspend fun createUser(
        @Body body: CreateUserRequestDTO
    )
}