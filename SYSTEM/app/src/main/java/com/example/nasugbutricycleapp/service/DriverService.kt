package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.repository.DriverApplicationRepository
import com.google.firebase.firestore.DocumentSnapshot

object DriverService {

    // PASSENGER APPLIES TO BECOME A DRIVER
    fun applyAsDriver(
        onResult: (Boolean, String) -> Unit
    ) {
        DriverApplicationRepository.applyAsDriver(onResult)
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

        DriverApplicationRepository.approveDriver(userId, onResult)
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

        DriverApplicationRepository.rejectDriver(userId, onResult)
    }
}
