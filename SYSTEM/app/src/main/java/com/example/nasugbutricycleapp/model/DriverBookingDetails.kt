package com.example.nasugbutricycleapp.model

data class DriverBookingDetails(
    val bookingId: String = "",
    val passengerName: String = "",
    val passengerPhone: String = "",
    val pickupLocation: String = "",
    val pickupLatitude: Double = 0.0,
    val pickupLongitude: Double = 0.0,
    val destination: String = "",
    val destinationLatitude: Double = 0.0,
    val destinationLongitude: Double = 0.0,
    val status: String = ""
)
