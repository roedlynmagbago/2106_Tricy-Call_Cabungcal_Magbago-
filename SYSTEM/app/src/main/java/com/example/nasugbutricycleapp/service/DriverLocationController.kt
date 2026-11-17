package com.example.nasugbutricycleapp.service

import android.content.Context
import android.location.Location

object DriverLocationController {

    private var isTracking = false
    private var isStarting = false
    private var trackingSession = 0L

    // START DRIVER LOCATION TRACKING
    fun startTracking(
        context: Context,
        onStatus: (String) -> Unit
    ) {
        if (isTracking || isStarting) {
            onStatus("Location tracking is already active or starting.")
            return
        }

        if (!LocationPermissionHelper.hasLocationPermission(context)) {
            onStatus("Location permission is required.")
            return
        }

        trackingSession++
        val currentSession = trackingSession

        isStarting = true

        DriverLocationManager.startLocationUpdates(
            context = context,

            onLocationReceived = { location: Location ->
                if (currentSession != trackingSession) return@startLocationUpdates

                DriverLocationService.updateLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy
                ) { success, message ->

                    if (currentSession != trackingSession) return@updateLocation

                    if (!success) {
                        onStatus(message)
                    }
                }
            },

            onError = { error ->
                if (currentSession != trackingSession) return@startLocationUpdates

                trackingSession++
                isStarting = false
                isTracking = false

                DriverLocationManager.stopLocationUpdates()
                DriverAvailabilityHeartbeat.stop()

                DriverAvailabilityService.setOnline(false) { _, _ -> }

                onStatus(error)
            }
        )

        if (currentSession != trackingSession) return

        DriverAvailabilityService.setOnline(true) { success, message ->

            if (currentSession != trackingSession) {
                return@setOnline
            }

            isStarting = false

            if (success) {
                isTracking = true
                DriverAvailabilityHeartbeat.start()

                onStatus(
                    "Location tracking started. Driver is now online."
                )
            } else {
                isTracking = false

                trackingSession++

                DriverLocationManager.stopLocationUpdates()
                DriverAvailabilityHeartbeat.stop()

                onStatus(
                    "Unable to set driver online: $message"
                )
            }
        }
    }

    // STOP DRIVER LOCATION TRACKING
    fun stopTracking(
        onStatus: (String) -> Unit
    ) {
        trackingSession++

        isStarting = false
        isTracking = false

        DriverLocationManager.stopLocationUpdates()
        DriverAvailabilityHeartbeat.stop()

        DriverAvailabilityService.setOnline(false) { success, message ->

            if (success) {
                onStatus(
                    "Location tracking stopped. Driver is now offline."
                )
            } else {
                onStatus(
                    "Tracking stopped, but offline update failed: $message"
                )
            }
        }
    }

    // CHECK TRACKING STATUS
    fun isTracking(): Boolean {
        return isTracking
    }
}
