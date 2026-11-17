package com.example.nasugbutricycleapp.model

data class DriverRating(
    val ratingId: String = "",
    val bookingId: String = "",
    val passengerId: String = "",
    val driverId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)