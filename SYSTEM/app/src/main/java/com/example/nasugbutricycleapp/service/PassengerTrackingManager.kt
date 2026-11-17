package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.model.DriverAvailability
import com.example.nasugbutricycleapp.model.DriverLocation

object PassengerTrackingManager {

    // START LISTENING TO SELECTED DRIVER
    fun startTracking(
        driverId: String,
        onLocationUpdate: (DriverLocation?, String?) -> Unit,
        onAvailabilityUpdate: (DriverAvailability?, String?) -> Unit
    ) {
        if (driverId.isBlank()) {
            onLocationUpdate(null, "Invalid driver ID.")
            onAvailabilityUpdate(null, "Invalid driver ID.")
            return
        }

        // LISTEN TO DRIVER LOCATION
        DriverLocationListenerManager.startListening(
            driverId,
            onLocationUpdate
        )

        // LISTEN TO DRIVER AVAILABILITY
        DriverAvailabilityListenerManager.startListening(
            driverId,
            onAvailabilityUpdate
        )
    }

    // STOP LISTENING TO SELECTED DRIVER
    fun stopTracking() {
        DriverLocationListenerManager.stopListening()
        DriverAvailabilityListenerManager.stopListening()
    }
}
