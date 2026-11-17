package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.firebase.FirebaseManager
import com.example.nasugbutricycleapp.model.DriverLocation
import com.example.nasugbutricycleapp.repository.DriverLocationRepository
import com.google.firebase.firestore.ListenerRegistration

object DriverLocationService {

    fun updateLocation(
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        onResult: (Boolean, String) -> Unit
    ) {
        if (latitude !in -90.0..90.0 ||
            longitude !in -180.0..180.0
        ) {
            onResult(false, "Invalid coordinates.")
            return
        }
        if (!accuracy.isFinite() || accuracy < 0f) {
            onResult(false, "Invalid location accuracy.")
            return
        }
        val currentUser = FirebaseManager.auth.currentUser
        if (currentUser == null) {
            onResult(false, "User is not logged in.")
            return
        }
        val location = DriverLocation(
            driverId = currentUser.uid,
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            updatedAt = System.currentTimeMillis()
        )
        DriverLocationRepository.updateLocation(location, onResult)
    }

    fun getDriverLocation(
        driverId: String,
        onResult: (DriverLocation?, String?) -> Unit
    ) {
        if (driverId.isBlank()) {
            onResult(null, "Invalid driver ID.")
            return
        }

        DriverLocationRepository.getDriverLocation(driverId, onResult)
    }

    // REAL-TIME DRIVER LOCATION
    fun listenToDriverLocation(
        driverId: String,
        onResult: (DriverLocation?, String?) -> Unit
    ): ListenerRegistration? {
        if (driverId.isBlank()) {
            onResult(null, "Invalid driver ID.")
            return null
        }
        return DriverLocationRepository.listenToDriverLocation(
            driverId,
            onResult
        )
    }
}
