package com.example.nasugbutricycleapp.model

data class DriverBookingDetails(
    val bookingId: String = "",
    val passengerName: String = "",
    val passengerPhone: String = "",
    val pickupLocation: String = "",
    val destination: String = "",
    val status: String = ""
)
