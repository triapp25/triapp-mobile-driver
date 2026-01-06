package com.triappdriver.data.repository

import com.triappdriver.data.ApiProfileService
import com.triappdriver.data.dtos.CreateUserRequestDTO
import com.triappdriver.data.dtos.LicenseDTO
import com.triappdriver.data.dtos.UserDTO
import com.triappdriver.data.dtos.VehicleDTO
import com.triappdriver.domain.model.SignUpDomainModel
import com.triappdriver.utils.FirebaseAuthManager

interface ProfileRepository {
    suspend fun createUser(model: SignUpDomainModel)
}

class ProfileRepositoryImpl(
    private val apiService: ApiProfileService,
    private val authManager: FirebaseAuthManager
) : ProfileRepository {


    override suspend fun createUser(
        model: SignUpDomainModel
    ) {
        return apiService.createUser(
            CreateUserRequestDTO(
                driverId = authManager.getCurrentUser()?.userId.orEmpty(),
                license = LicenseDTO(),
                vehicle = VehicleDTO(
                    driverId = authManager.getCurrentUser()?.userId.orEmpty(),
                ),
                user = UserDTO(
                    name = model.fullName,
                    email = "",
                    phone = model.phone,
                    passwordHash = "",
                    documentIdUrl = model.idDocumentUrl.orEmpty(),
                    selfieIdUrl = model.profilePhotoUrl.orEmpty()
                )
            )
        )
    }

}