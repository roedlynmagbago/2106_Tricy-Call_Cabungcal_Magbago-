package com.example.nasugbutricycleapp.repository

import com.example.nasugbutricycleapp.firebase.FirebaseManager
import com.example.nasugbutricycleapp.model.DriverAvailability
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

object DriverAvailabilityRepository {

    private const val COLLECTION = "driverAvailability"

    fun updateAvailability(
        availability: DriverAvailability,
        onResult: (Boolean, String) -> Unit
    ) {
        val currentUser = FirebaseManager.auth.currentUser

        if (currentUser == null) {
            onResult(false, "User is not logged in.")
            return
        }

        if (currentUser.uid != availability.driverId) {
            onResult(false, "Unauthorized availability update.")
            return
        }

        val data = mapOf(
            "driverId" to currentUser.uid,
            "isOnline" to availability.isOnline,
            "updatedAt" to System.currentTimeMillis()
        )

        FirebaseManager.db
            .collection(COLLECTION)
            .document(currentUser.uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                onResult(true, "Driver availability updated.")
            }
            .addOnFailureListener { exception ->
                onResult(
                    false,
                    exception.message ?: "Failed to update availability."
                )
            }
    }

    fun getAvailability(
        driverId: String,
        onResult: (DriverAvailability?, String?) -> Unit
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
                    onResult(null, "Driver availability not found.")
                    return@addOnSuccessListener
                }

                val availability =
                    document.toObject(DriverAvailability::class.java)

                onResult(availability, null)
            }
            .addOnFailureListener { exception ->
                onResult(
                    null,
                    exception.message ?: "Failed to get availability."
                )
            }
    }

    fun listenToAvailability(
        driverId: String,
        onResult: (DriverAvailability?, String?) -> Unit
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
                        exception.message ?: "Failed to listen to availability."
                    )
                    return@addSnapshotListener
                }

                if (document == null || !document.exists()) {
                    onResult(null, "Driver availability not found.")
                    return@addSnapshotListener
                }

                val availability =
                    document.toObject(DriverAvailability::class.java)

                onResult(availability, null)
            }
    }

    // GET ALL ONLINE DRIVERS
    fun getAvailableDrivers(
        onResult: (List<DriverAvailability>, String?) -> Unit
    ) {
        FirebaseManager.db
            .collection(COLLECTION)
            .whereEqualTo("isOnline", true)
            .get()
            .addOnSuccessListener { documents ->

                val drivers = documents.documents.mapNotNull { document ->
                    document.toObject(DriverAvailability::class.java)
                }

                onResult(drivers, null)
            }
            .addOnFailureListener { exception ->
                onResult(
                    emptyList(),
                    exception.message ?: "Failed to get available drivers."
                )
            }
    }

    // REAL-TIME LISTENER FOR ALL ONLINE DRIVERS
    fun listenToAvailableDrivers(
        onResult: (List<DriverAvailability>, String?) -> Unit
    ): ListenerRegistration {

        return FirebaseManager.db
            .collection(COLLECTION)
            .whereEqualTo("isOnline", true)
            .addSnapshotListener { documents, exception ->

                if (exception != null) {
                    onResult(
                        emptyList(),
                        exception.message
                            ?: "Failed to listen to available drivers."
                    )
                    return@addSnapshotListener
                }

                if (documents == null) {
                    onResult(
                        emptyList(),
                        "No driver availability data found."
                    )
                    return@addSnapshotListener
                }

                val drivers = documents.documents.mapNotNull { document ->
                    document.toObject(DriverAvailability::class.java)
                }

                onResult(drivers, null)
            }
    }
}