package com.example.nasugbutricycleapp.model

data class ParadaSchedule(
    val scheduleId: String = "",
    val todaName: String = "",
    val standId: String = "",
    val openingTime: String = "",
    val closingTime: String = "",
    val cutOffTime: String = "",
    val isActive: Boolean = true
)
