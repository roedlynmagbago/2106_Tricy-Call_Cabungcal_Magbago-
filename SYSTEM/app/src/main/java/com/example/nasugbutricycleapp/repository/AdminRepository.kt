package com.example.nasugbutricycleapp.repository

import com.example.nasugbutricycleapp.firebase.FirebaseManager

object AdminRepository {
    private const val USERS_COLLECTION = "users"
    private const val ADMIN = "ADMIN"
    private const val SUPER_ADMIN = "SUPER_ADMIN"
    private const val ACTIVE = "ACTIVE"

    fun checkAdmin(
        onResult: (Boolean) -> Unit
    ) {
        val currentUser = FirebaseManager.auth.currentUser

        if (currentUser == null) {
            onResult(false)
            return
        }

        FirebaseManager.db.collection(USERS_COLLECTION)
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    onResult(false)
                    return@addOnSuccessListener
                }

                val role = document.getString("role")
                val status = document.getString("status")

                val isAdmin =
                    status == ACTIVE &&
                            (role == ADMIN || role == SUPER_ADMIN)

                onResult(isAdmin)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
}
