package com.example.nasugbutricycleapp.repository

import com.example.nasugbutricycleapp.firebase.FirebaseManager
import com.example.nasugbutricycleapp.model.DriverRatingSummary

object DriverRatingSummaryRepository {

    private const val COLLECTION = "driverRatings"

    // GET DRIVER RATING SUMMARY
    fun getRatingSummary(
        driverId: String,
        onResult: (DriverRatingSummary?, String?) -> Unit
    ) {
        if (driverId.isBlank()) {
            onResult(null, "Invalid driver ID.")
            return
        }

        FirebaseManager.db
            .collection(COLLECTION)
            .whereEqualTo("driverId", driverId)
            .get()
            .addOnSuccessListener { documents ->

                val ratings = documents.documents.mapNotNull { document ->
                    document.getLong("rating")?.toInt()
                }.filter { it in 1..5 }

                val totalRatings = ratings.size

                val averageRating = if (totalRatings > 0) {
                    ratings.average()
                } else {
                    0.0
                }

                val summary = DriverRatingSummary(
                    driverId = driverId,
                    averageRating = averageRating,
                    totalRatings = totalRatings
                )

                onResult(summary, null)
            }
            .addOnFailureListener { error ->
                onResult(
                    null,
                    error.localizedMessage
                        ?: "Failed to get driver rating summary."
                )
            }
    }
}
