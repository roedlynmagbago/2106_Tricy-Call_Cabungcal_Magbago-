package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.model.Booking
import com.example.nasugbutricycleapp.model.AvailableBooking
import com.example.nasugbutricycleapp.model.DriverBookingDetails
import com.example.nasugbutricycleapp.repository.BookingRepository
import com.google.firebase.firestore.ListenerRegistration

object BookingService {

    // CREATE BOOKING
    fun createBooking(
        booking: Booking,
        onResult: (Boolean, String) -> Unit
    ) {
        BookingRepository.createBooking(booking, onResult)
    }

    // ACCEPT BOOKING
    fun acceptBooking(
        bookingId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val id = bookingId.trim()

        if (id.isBlank()) {
            onResult(false, "Invalid booking ID.")
            return
        }

        BookingRepository.acceptBooking(id, onResult)
    }

    // COMPLETE BOOKING
    fun completeBooking(
        bookingId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val id = bookingId.trim()

        if (id.isBlank()) {
            onResult(false, "Invalid booking ID.")
            return
        }

        BookingRepository.completeBooking(id, onResult)
    }

    // GET ACCEPTED BOOKING DETAILS
    fun getAcceptedBookingDetails(
        bookingId: String,
        onResult: (DriverBookingDetails?, String) -> Unit
    ) {
        val id = bookingId.trim()

        if (id.isBlank()) {
            onResult(null, "Invalid booking ID.")
            return
        }

        BookingRepository.getAcceptedBookingDetails(id, onResult)
    }

    // GET AVAILABLE BOOKINGS
    fun getAvailableBookings(
        onResult: (List<AvailableBooking>, String) -> Unit
    ) {
        BookingRepository.getAvailableBookings(onResult)
    }

    // LISTEN TO AVAILABLE BOOKINGS
    fun listenToAvailableBookings(
        onResult: (List<AvailableBooking>, String) -> Unit,
        onListenerReady: (ListenerRegistration?) -> Unit
    ) {
        BookingRepository.listenToAvailableBookings(
            onResult,
            onListenerReady
        )
    }

    // CANCEL BOOKING
    fun cancelBooking(
        bookingId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val id = bookingId.trim()

        if (id.isBlank()) {
            onResult(false, "Invalid booking ID.")
            return
        }

        BookingRepository.cancelBooking(id, onResult)
    }

    // LISTEN TO PASSENGER BOOKING STATUS
    fun listenToPassengerBookingStatus(
        bookingId: String,
        onResult: (String?, String?) -> Unit,
        onListenerReady: (ListenerRegistration?) -> Unit
    ) {
        val id = bookingId.trim()

        if (id.isBlank()) {
            onResult(null, "Invalid booking ID.")
            onListenerReady(null)
            return
        }

        BookingRepository.listenToPassengerBookingStatus(
            id,
            onResult,
            onListenerReady
        )
    }

    // LISTEN TO PASSENGER'S ASSIGNED DRIVER
    fun listenToPassengerAssignedDriver(
        bookingId: String,
        onResult: (String?, String?, String?) -> Unit,
        onListenerReady: (ListenerRegistration?) -> Unit
    ) {
        val id = bookingId.trim()
        if (id.isBlank()) {
            onResult(null, null, null)
            onListenerReady(null)
            return
        }
        BookingRepository.listenToPassengerAssignedDriver(
            id,
            onResult,
            onListenerReady
        )
    }
}