package com.triappdriver.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequestDTO(
    val user: UserDTO,
    val rating: Double = 5.0,
    val driverId: String,
    val status: String = "OFFLINE",
    val license: LicenseDTO,
    val vehicle: VehicleDTO
)

@Serializable
data class UserDTO(
    val role: String = "DRIVER",
    val name: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val documentIdUrl: String,
    val selfieIdUrl: String
)

@Serializable
data class LicenseDTO(
    val cnhNumber: String = "12345678900",
    val category: String = "B",
    val issueDate: String = "2018-01-10",
    val expirationDate: String = "2030-01-10",
    val issuingState: String = "PT"
)

@Serializable
data class VehicleDTO(
    val driverId: String,
    val type: String = "CAR",
    val plate: String = "CO-18-AB",
    val make: String = "Nissan",
    val model: String = "X-Trail",
    val color: String = "Prata",
    val seats: Int = 6,
    val year: Int = 2022,
    val status: String = "AVAILABLE"
)