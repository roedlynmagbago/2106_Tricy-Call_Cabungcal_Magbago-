package com.example.nasugbutricycleapp.model

class Passenger(
    userId: String = "",
    name: String = "",
    email: String = "",
    phoneNumber: String = ""
) : User(
    userId = userId,
    name = name,
    email = email,
    phoneNumber = phoneNumber,
    role = "PASSENGER",
    status = "ACTIVE",
    driverApplicationStatus = "NONE"
) {

    fun bookRide() {
        println("$name is booking a ride.")
    }
    fun setPickup(pickupLocation: String) {
        println("Pickup location: $pickupLocation")
    }
    fun setDestination(destination: String) {
        println("Destination: $destination")
    }
    fun trackDriver() {
        println("Tracking driver...")
    }
    fun cancelBooking() {
        println("Booking cancelled.")
    }
    fun rateDriver(rating: Int) {
        println("Driver rated: $rating")
    }
    fun reportDriver(reason: String) {
        println("Driver reported: $reason")
    }
    fun viewTripHistory() {
        println("Viewing trip history.")
    }
}
