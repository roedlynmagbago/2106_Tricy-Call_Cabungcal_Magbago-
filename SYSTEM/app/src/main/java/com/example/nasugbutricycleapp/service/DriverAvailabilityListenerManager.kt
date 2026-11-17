package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.model.DriverAvailability
import com.google.firebase.firestore.ListenerRegistration

object DriverAvailabilityListenerManager {

    private var availabilityListener: ListenerRegistration? = null

    // LISTEN TO ONE DRIVER
    fun startListening(
        driverId: String,
        onResult: (DriverAvailability?, String?) -> Unit
    ) {
        stopListening()

        if (driverId.isBlank()) {
            onResult(null, "Invalid driver ID.")
            return
        }

        availabilityListener =
            DriverAvailabilityService.listenToAvailability(
                driverId,
                onResult
            )
    }

    // LISTEN TO ALL ONLINE DRIVERS
    fun startListeningToAvailableDrivers(
        onResult: (List<DriverAvailability>, String?) -> Unit
    ) {
        stopListening()

        availabilityListener =
            DriverAvailabilityService.listenToAvailableDrivers(
                onResult
            )
    }

    // STOP LISTENER
    fun stopListening() {
        availabilityListener?.remove()
        availabilityListener = null
    }

    // CHECK LISTENER STATUS
    fun isListening(): Boolean {
        return availabilityListener != null
    }
}
