package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.firebase.FirebaseManager
import com.example.nasugbutricycleapp.model.DriverAvailability
import com.example.nasugbutricycleapp.repository.DriverAvailabilityRepository
import com.google.firebase.firestore.ListenerRegistration

object DriverAvailabilityService {

    // SET DRIVER ONLINE OR OFFLINE
    fun setOnline(
        isOnline: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        val currentUser = FirebaseManager.auth.currentUser

        if (currentUser == null) {
            onResult(false, "User is not logged in.")
            return
        }

        val availability = DriverAvailability(
            driverId = currentUser.uid,
            isOnline = isOnline,
            updatedAt = System.currentTimeMillis()
        )

        DriverAvailabilityRepository.updateAvailability(
            availability,
            onResult
        )
    }

    // GET ONE DRIVER'S AVAILABILITY
    fun getAvailability(
        driverId: String,
        onResult: (DriverAvailability?, String?) -> Unit
    ) {
        if (driverId.isBlank()) {
            onResult(null, "Invalid driver ID.")
            return
        }

        DriverAvailabilityRepository.getAvailability(
            driverId,
            onResult
        )
    }

    // LISTEN TO ONE DRIVER'S AVAILABILITY
    fun listenToAvailability(
        driverId: String,
        onResult: (DriverAvailability?, String?) -> Unit
    ): ListenerRegistration? {

        if (driverId.isBlank()) {
            onResult(null, "Invalid driver ID.")
            return null
        }

        return DriverAvailabilityRepository.listenToAvailability(
            driverId,
            onResult
        )
    }

    // GET ALL ONLINE DRIVERS
    fun getAvailableDrivers(
        onResult: (List<DriverAvailability>, String?) -> Unit
    ) {
        DriverAvailabilityRepository.getAvailableDrivers(onResult)
    }

    // REAL-TIME LISTENER FOR ALL ONLINE DRIVERS
    fun listenToAvailableDrivers(
        onResult: (List<DriverAvailability>, String?) -> Unit
    ): ListenerRegistration {

        return DriverAvailabilityRepository.listenToAvailableDrivers(
            onResult
        )
    }

    // REFRESH DRIVER ONLINE STATUS
    fun refreshOnlineStatus(
        onResult: (Boolean, String) -> Unit
    ) {
        setOnline(true, onResult)
    }
}
