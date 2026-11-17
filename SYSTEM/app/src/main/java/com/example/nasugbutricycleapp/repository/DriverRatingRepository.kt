package com.example.nasugbutricycleapp.repository

import com.example.nasugbutricycleapp.firebase.FirebaseManager
import com.example.nasugbutricycleapp.model.DriverRating

object DriverRatingRepository {

    private const val COLLECTION = "driverRatings"

    // SUBMIT DRIVER RATING
    fun submitRating(
        rating: DriverRating,
        onResult: (Boolean, String) -> Unit
    ) {
        val currentUser = FirebaseManager.auth.currentUser

        if (currentUser == null) {
            onResult(false, "Please log in first.")
            return
        }

        val bookingId = rating.bookingId.trim()
        val driverId = rating.driverId.trim()

        if (bookingId.isBlank() || driverId.isBlank()) {
            onResult(false, "Invalid booking or driver ID.")
            return
        }

        if (rating.rating !in 1..5) {
            onResult(false, "Rating must be between 1 and 5.")
            return
        }

        if (rating.comment.length > 500) {
            onResult(false, "Comment must not exceed 500 characters.")
            return
        }

        val newRating = rating.copy(
            ratingId = bookingId,
            bookingId = bookingId,
            passengerId = currentUser.uid,
            driverId = driverId,
            comment = rating.comment.trim(),
            createdAt = System.currentTimeMillis()
        )

        FirebaseManager.db
            .collection(COLLECTION)
            .document(bookingId)
            .get()
            .addOnSuccessListener { existingRating ->

                if (existingRating.exists()) {
                    onResult(false, "You have already rated this ride.")
                    return@addOnSuccessListener
                }

                FirebaseManager.db
                    .collection(COLLECTION)
                    .document(bookingId)
                    .set(newRating)
                    .addOnSuccessListener {
                        onResult(true, "Rating submitted successfully.")
                    }
                    .addOnFailureListener { error ->
                        onResult(
                            false,
                            error.localizedMessage
                                ?: "Failed to submit rating."
                        )
                    }
            }
            .addOnFailureListener { error ->
                onResult(
                    false,
                    error.localizedMessage
                        ?: "Unable to check existing rating."
                )
            }
    }

    // GET RATINGS FOR ONE DRIVER
    fun getDriverRatings(
        driverId: String,
        onResult: (List<DriverRating>, String?) -> Unit
    ) {
        if (driverId.isBlank()) {
            onResult(emptyList(), "Invalid driver ID.")
            return
        }

        FirebaseManager.db
            .collection(COLLECTION)
            .whereEqualTo("driverId", driverId)
            .get()
            .addOnSuccessListener { documents ->

                val ratings = documents.documents.mapNotNull { document ->
                    document.toObject(DriverRating::class.java)
                }

                onResult(
                    ratings.sortedByDescending { it.createdAt },
                    null
                )
            }
            .addOnFailureListener { error ->
                onResult(
                    emptyList(),
                    error.localizedMessage
                        ?: "Failed to get driver ratings."
                )
            }
    }
}
