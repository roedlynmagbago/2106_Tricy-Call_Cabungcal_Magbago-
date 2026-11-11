package com.example.nasugbutricycleapp.repository

import com.example.nasugbutricycleapp.firebase.FirebaseManager

object FirestoreUserRepository {

    fun saveUser(
        userId: String,
        name: String,
        email: String,
        applyAsDriver: Boolean,
        username: String = "",
        phoneNumber: String = "",
        toda: String = "",
        address: String = "",
        onResult: (Boolean) -> Unit
    ) {
        val user = hashMapOf(
            "userId" to userId,
            "name" to name.trim(),
            "username" to username.trim(),
            "email" to email.trim(),
            "phoneNumber" to phoneNumber.trim(),
            "role" to "PASSENGER",
            "status" to "ACTIVE",
            "driverApplicationStatus" to
                    if (applyAsDriver) "PENDING" else "NONE",
            "toda" to toda.trim(),
            "address" to address.trim()
        )

        FirebaseManager.db.collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
}