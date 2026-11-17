package com.example.nasugbutricycleapp.model

data class DriverReport(
    val reportId: String = "",
    val bookingId: String = "",
    val passengerId: String = "",
    val driverId: String = "",
    val category: String = "",
    val description: String = "",
    val status: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis()
)