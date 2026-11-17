package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.model.DriverRating
import com.example.nasugbutricycleapp.repository.DriverRatingRepository

object DriverRatingService {

    // SUBMIT DRIVER RATING
    fun submitRating(
        rating: DriverRating,
        onResult: (Boolean, String) -> Unit
    ) {
        if (rating.bookingId.isBlank()) {
            onResult(false, "Invalid booking ID.")
            return
        }

        if (rating.driverId.isBlank()) {
            onResult(false, "Invalid driver ID.")
            return
        }

        if (rating.rating !in 1..5) {
            onResult(false, "Rating must be between 1 and 5.")
            return
        }

        DriverRatingRepository.submitRating(
            rating,
            onResult
        )
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

        DriverRatingRepository.getDriverRatings(
            driverId,
            onResult
        )
    }
}