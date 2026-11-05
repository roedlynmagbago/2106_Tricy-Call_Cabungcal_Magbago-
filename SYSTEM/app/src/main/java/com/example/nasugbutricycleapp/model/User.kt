package com.example.nasugbutricycleapp.model

open class User(
    val userId: String = "",
    var name: String = "",
    var username: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var role: String = "PASSENGER",
    var status: String = "ACTIVE",
    var driverApplicationStatus: String = "NONE",
    var toda: String = "",
    var address: String = ""
)