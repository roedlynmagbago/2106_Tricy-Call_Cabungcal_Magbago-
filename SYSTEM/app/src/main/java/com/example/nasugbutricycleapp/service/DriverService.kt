package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.repository.DriverApplicationRepository
import com.google.firebase.firestore.DocumentSnapshot

object DriverService {

    fun getPendingDrivers(
        onResult: (List<DocumentSnapshot>, String?) -> Unit
    ) {
        DriverApplicationRepository.getPendingDrivers(onResult)
    }

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