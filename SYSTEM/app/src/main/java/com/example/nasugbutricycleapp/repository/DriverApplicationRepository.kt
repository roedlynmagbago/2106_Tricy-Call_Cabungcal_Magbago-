package com.example.nasugbutricycleapp.repository

import com.example.nasugbutricycleapp.firebase.FirebaseManager
import com.google.firebase.firestore.DocumentSnapshot

object DriverApplicationRepository {

    private const val USERS_COLLECTION = "users"

    private const val NONE = "NONE"
    private const val PENDING = "PENDING"
    private const val APPROVED = "APPROVED"
    private const val REJECTED = "REJECTED"

    private const val PASSENGER = "PASSENGER"
    private const val DRIVER = "DRIVER"
    private const val ACTIVE = "ACTIVE"

    // PASSENGER APPLIES TO BECOME A DRIVER
    fun applyAsDriver(
        onResult: (Boolean, String) -> Unit
    ) {
        val currentUser = FirebaseManager.auth.currentUser

        if (currentUser == null) {
            onResult(false, "Please log in first.")
            return
        }

        val userRef = FirebaseManager.db
            .collection(USERS_COLLECTION)
            .document(currentUser.uid)

        FirebaseManager.db.runTransaction<Void?> { transaction ->

            val document = transaction.get(userRef)

            if (!document.exists()) {
                throw IllegalStateException("User account not found.")
            }

            if (document.getString("status") != ACTIVE) {
                throw IllegalStateException("Your account is not active.")
            }

            if (document.getString("role") != PASSENGER) {
                throw IllegalStateException("Only passengers can apply.")
            }

            if (document.getString("driverApplicationStatus") != NONE) {
                throw IllegalStateException(
                    "You already have a driver application."
                )
            }

            transaction.update(
                userRef,
                "driverApplicationStatus",
                PENDING
            )

            null
        }
            .addOnSuccessListener {
                onResult(true, "Driver application submitted successfully.")
            }
            .addOnFailureListener { error ->
                onResult(
                    false,
                    error.localizedMessage ?: "Unable to submit application."
                )
            }
    }

    // GET PENDING DRIVER APPLICATIONS
    fun getPendingDrivers(
        onResult: (List<DocumentSnapshot>, String?) -> Unit
    ) {
        FirebaseManager.db.collection(USERS_COLLECTION)
            .whereEqualTo("driverApplicationStatus", PENDING)
            .get()
            .addOnSuccessListener { documents ->
                onResult(documents.documents, null)
            }
            .addOnFailureListener { error ->
                onResult(
                    emptyList(),
                    error.localizedMessage ?: "Unable to load applications."
                )
            }
    }

    // APPROVE DRIVER APPLICATION
    fun approveDriver(
        userId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (userId.isBlank()) {
            onResult(false, "Invalid user ID.")
            return
        }

        val userRef = FirebaseManager.db
            .collection(USERS_COLLECTION)
            .document(userId)

        FirebaseManager.db.runTransaction<Void?> { transaction ->

            val document = transaction.get(userRef)

            if (!document.exists()) {
                throw IllegalStateException("User application not found.")
            }

            if (document.getString("status") != ACTIVE) {
                throw IllegalStateException("User account is not active.")
            }

            if (document.getString("role") != PASSENGER) {
                throw IllegalStateException("User is not a passenger.")
            }

            if (document.getString("driverApplicationStatus") != PENDING) {
                throw IllegalStateException("Application is no longer pending.")
            }

            transaction.update(
                userRef,
                mapOf(
                    "role" to DRIVER,
                    "driverApplicationStatus" to APPROVED
                )
            )

            null
        }
            .addOnSuccessListener {
                onResult(true, "Driver approved successfully.")
            }
            .addOnFailureListener { error ->
                onResult(
                    false,
                    error.localizedMessage ?: "Driver approval failed."
                )
            }
    }

    // REJECT DRIVER APPLICATION
    fun rejectDriver(
        userId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (userId.isBlank()) {
            onResult(false, "Invalid user ID.")
            return
        }

        val userRef = FirebaseManager.db
            .collection(USERS_COLLECTION)
            .document(userId)

        FirebaseManager.db.runTransaction<Void?> { transaction ->

            val document = transaction.get(userRef)

            if (!document.exists()) {
                throw IllegalStateException("User application not found.")
            }

            if (document.getString("status") != ACTIVE) {
                throw IllegalStateException("User account is not active.")
            }

            if (document.getString("role") != PASSENGER) {
                throw IllegalStateException("User is not a passenger.")
            }

            if (document.getString("driverApplicationStatus") != PENDING) {
                throw IllegalStateException("Application is no longer pending.")
            }

            transaction.update(
                userRef,
                "driverApplicationStatus",
                REJECTED
            )

            null
        }
            .addOnSuccessListener {
                onResult(true, "Driver application rejected.")
            }
            .addOnFailureListener { error ->
                onResult(
                    false,
                    error.localizedMessage ?: "Driver rejection failed."
                )
            }
    }
}
