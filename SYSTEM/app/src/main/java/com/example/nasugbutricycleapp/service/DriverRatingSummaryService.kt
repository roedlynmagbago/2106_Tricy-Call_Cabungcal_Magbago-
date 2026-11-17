package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.model.DriverRatingSummary
import com.example.nasugbutricycleapp.repository.DriverRatingSummaryRepository

object DriverRatingSummaryService {

    // GET DRIVER RATING SUMMARY
    fun getRatingSummary(
        driverId: String,
        onResult: (DriverRatingSummary?, String?) -> Unit
    ) {
        if (driverId.isBlank()) {
            onResult(null, "Invalid driver ID.")
            return
        }

        DriverRatingSummaryRepository.getRatingSummary(
            driverId,
            onResult
        )
    }
}
