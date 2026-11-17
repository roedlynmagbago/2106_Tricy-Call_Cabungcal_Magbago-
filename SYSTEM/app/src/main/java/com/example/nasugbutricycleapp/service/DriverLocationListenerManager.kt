package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.model.DriverLocation
import com.google.firebase.firestore.ListenerRegistration

object DriverLocationListenerManager {

    private var locationListener: ListenerRegistration? = null

    fun startListening(
        driverId: String,
        onResult: (DriverLocation?, String?) -> Unit
    ) {
        // Stop the previous listener before starting another.
        stopListening()

        if (driverId.isBlank()) {
            onResult(null, "Invalid driver ID.")
            return
        }

        locationListener =
            DriverLocationService.listenToDriverLocation(
                driverId,
                onResult
            )
    }

    fun stopListening() {
        locationListener?.remove()
        locationListener = null
    }

    fun isListening(): Boolean {
        return locationListener != null
    }
}
