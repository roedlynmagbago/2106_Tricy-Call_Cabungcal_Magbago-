package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.repository.AdminRepository
import com.example.nasugbutricycleapp.repository.DriverApplicationRepository
import com.google.firebase.firestore.DocumentSnapshot

object AdminService {

    // CHECK IF USER IS ADMIN
    fun checkAdmin(
        onResult: (Boolean) -> Unit
    ) {
        AdminRepository.checkAdmin(onResult)
    }

    // GET PENDING DRIVER APPLICATIONS
    fun getPendingDrivers(
        onResult: (List<DocumentSnapshot>, String?) -> Unit
    ) {
        DriverApplicationRepository.getPendingDrivers(onResult)
    }

    // APPROVE DRIVER APPLICATION
    fun approveDriver(
        userId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (userId.isBlank()) {
            onResult(false, "Invalid user ID.")
            return
        }

        DriverApplicationRepository.approveDriver(
            userId,
            onResult
        )
    }

    // REJECT DRIVER APPLICATION
    fun rejectDriver(
        userId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (userId.isBlank()) {
            onResult(false, "Invalid user ID.")
            return
        }

        DriverApplicationRepository.rejectDriver(
            userId,
            onResult
        )
    }
}
