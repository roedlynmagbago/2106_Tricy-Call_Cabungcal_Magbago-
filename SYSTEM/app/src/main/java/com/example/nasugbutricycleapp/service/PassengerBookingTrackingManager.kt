package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.model.DriverAvailability
import com.example.nasugbutricycleapp.model.DriverLocation
import com.google.firebase.firestore.ListenerRegistration

object PassengerBookingTrackingManager {

    private var bookingListener: ListenerRegistration? = null
    private var currentDriverId: String? = null
    private var trackingSession = 0

    // START TRACKING THE ASSIGNED DRIVER
    fun startTracking(
        bookingId: String,
        onDriverAssigned: (String?, String?, String?) -> Unit,
        onLocationUpdate: (DriverLocation?, String?) -> Unit,
        onAvailabilityUpdate: (DriverAvailability?, String?) -> Unit
    ) {
        stopTracking()

        val session = trackingSession

        if (bookingId.isBlank()) {
            onDriverAssigned(null, null, null)
            return
        }

        BookingService.listenToPassengerAssignedDriver(
            bookingId = bookingId,

            onResult = { driverId, driverName, driverPhone ->

                // IGNORE CALLBACKS FROM OLD TRACKING SESSIONS
                if (session != trackingSession) return@listenToPassengerAssignedDriver

                // RETURN ASSIGNED DRIVER DETAILS
                onDriverAssigned(driverId, driverName, driverPhone)

                // STOP DRIVER TRACKING IF NO DRIVER IS ASSIGNED
                if (driverId.isNullOrBlank()) {
                    PassengerTrackingManager.stopTracking()
                    currentDriverId = null
                    return@listenToPassengerAssignedDriver
                }

                // AVOID RESTARTING THE SAME DRIVER LISTENERS
                if (currentDriverId == driverId) {
                    return@listenToPassengerAssignedDriver
                }

                PassengerTrackingManager.stopTracking()

                currentDriverId = driverId

                PassengerTrackingManager.startTracking(
                    driverId,
                    onLocationUpdate,
                    onAvailabilityUpdate
                )
            },

            onListenerReady = { listener ->

                // REMOVE LISTENER IF THIS SESSION IS ALREADY OUTDATED
                if (session != trackingSession) {
                    listener?.remove()
                } else {
                    bookingListener = listener
                }
            }
        )
    }

    // STOP ALL TRACKING LISTENERS
    fun stopTracking() {
        trackingSession++

        bookingListener?.remove()
        bookingListener = null

        PassengerTrackingManager.stopTracking()
        currentDriverId = null
    }
}
