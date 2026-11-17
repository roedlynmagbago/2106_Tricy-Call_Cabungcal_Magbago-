package com.example.nasugbutricycleapp.repository

import com.example.nasugbutricycleapp.firebase.FirebaseManager
import com.example.nasugbutricycleapp.model.DriverLocation
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

object DriverLocationRepository {

    private const val COLLECTION = "driverLocations"
    fun updateLocation(
        location: DriverLocation,
        onResult: (Boolean, String) -> Unit
    ) {
        val currentUser = FirebaseManager.auth.currentUser
        if (currentUser == null) {
            onResult(false, "User is not logged in.")
            return
        }
        if (currentUser.uid != location.driverId) {
            onResult(false, "Unauthorized location update.")
            return
        }

        if (location.latitude !in -90.0..90.0 ||
            location.longitude !in -180.0..180.0
        ) {
            onResult(false, "Invalid coordinates.")
            return
        }
        if (!location.accuracy.isFinite() || location.accuracy < 0) {
            onResult(false, "Invalid location accuracy.")
            return
        }
        val data = mapOf(
            "driverId" to location.driverId,
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "accuracy" to location.accuracy,
            "updatedAt" to System.currentTimeMillis()
        )
        FirebaseManager.db
            .collection(COLLECTION)
            .document(currentUser.uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                onResult(true, "Driver location updated.")
            }
            .addOnFailureListener { exception ->
                onResult(
                    false,
                    exception.message ?: "Failed to update driver location."
                )
            }
    }

    // ONE-TIME LOCATION RETRIEVAL
    fun getDriverLocation(
        driverId: String,
        onResult: (DriverLocation?, String?) -> Unit
    ) {
        if (driverId.isBlank()) {
            onResult(null, "Invalid driver ID.")
            return
        }
        FirebaseManager.db
            .collection(COLLECTION)
            .document(driverId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onResult(null, "Driver location not found.")
                    return@addOnSuccessListener
                }
                val location =
                    document.toObject(DriverLocation::class.java)

                onResult(location, null)
            }
            .addOnFailureListener { exception ->
                onResult(
                    null,
                    exception.message ?: "Failed to get driver location."
                )
            }
    }

    // REAL-TIME LOCATION LISTENER
    fun listenToDriverLocation(
        driverId: String,
        onResult: (DriverLocation?, String?) -> Unit
    ): ListenerRegistration? {
        if (driverId.isBlank()) {
            onResult(null, "Invalid driver ID.")
            return null
        }
        return FirebaseManager.db
            .collection(COLLECTION)
            .document(driverId)
            .addSnapshotListener { document, exception ->
                if (exception != null) {
                    onResult(
                        null,
                        exception.message
                            ?: "Failed to listen to driver location."
                    )
                    return@addSnapshotListener
                }
                if (document == null || !document.exists()) {
                    onResult(null, "Driver location not found.")
                    return@addSnapshotListener
                }
                val location =
                    document.toObject(DriverLocation::class.java)

                onResult(location, null)
            }
    }
}
