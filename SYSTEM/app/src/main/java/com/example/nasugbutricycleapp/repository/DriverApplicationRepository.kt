package com.example.nasugbutricycleapp.repository

import com.example.nasugbutricycleapp.firebase.FirebaseManager
import com.google.firebase.firestore.DocumentSnapshot

object DriverApplicationRepository {

    // Get pending driver applications
    fun getPendingDrivers(
        onResult: (List<DocumentSnapshot>, String?) -> Unit
    ) {
        FirebaseManager.db.collection("users")
            .whereEqualTo("driverApplicationStatus", "PENDING")
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

    // Approve driver application
    fun approveDriver(
        userId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val userRef = FirebaseManager.db.collection("users")
            .document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onResult(false, "User application not found.")
                    return@addOnSuccessListener
                }
                if (document.getString("driverApplicationStatus") != "PENDING") {
                    onResult(false, "Application is no longer pending.")
                    return@addOnSuccessListener
                }
                val updates = mapOf(
                    "role" to "DRIVER",
                    "driverApplicationStatus" to "APPROVED"
                )
                userRef.update(updates)
                    .addOnSuccessListener {
                        onResult(true, "Driver approved successfully.")
                    }
                    .addOnFailureListener { error ->
                        onResult(
                            false,
                            error.localizedMessage ?: "Approval failed."
                        )
                    }
            }
            .addOnFailureListener { error ->
                onResult(
                    false,
                    error.localizedMessage ?: "Unable to check application."
                )
            }
    }

    // Reject driver application
    fun rejectDriver(
        userId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val userRef = FirebaseManager.db.collection("users")
            .document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onResult(false, "User application not found.")
                    return@addOnSuccessListener
                }
                if (document.getString("driverApplicationStatus") != "PENDING") {
                    onResult(false, "Application is no longer pending.")
                    return@addOnSuccessListener
                }
                userRef.update(
                    "driverApplicationStatus",
                    "REJECTED"
                )
                    .addOnSuccessListener {
                        onResult(true, "Driver application rejected.")
                    }
                    .addOnFailureListener { error ->
                        onResult(
                            false,
                            error.localizedMessage ?: "Rejection failed."
                        )
                    }
            }
            .addOnFailureListener { error ->
                onResult(
                    false,
                    error.localizedMessage ?: "Unable to check application."
                )
            }
    }
}