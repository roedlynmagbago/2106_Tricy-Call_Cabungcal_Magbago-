package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.model.Booking
import com.example.nasugbutricycleapp.model.AvailableBooking
import com.example.nasugbutricycleapp.model.DriverBookingDetails
import com.example.nasugbutricycleapp.repository.BookingRepository
import com.google.firebase.firestore.ListenerRegistration

object BookingService {

    fun createBooking(
        booking: Booking,
        onResult: (Boolean, String) -> Unit
    ) {
        BookingRepository.createBooking(booking, onResult)
    }

    fun acceptBooking(
        bookingId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (bookingId.isBlank()) {
            onResult(false, "Invalid booking ID.")
            return
        }

        BookingRepository.acceptBooking(bookingId, onResult)
    }

    fun completeBooking(
        bookingId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (bookingId.isBlank()) {
            onResult(false, "Invalid booking ID.")
            return
        }

        BookingRepository.completeBooking(bookingId, onResult)
    }

    fun getAcceptedBookingDetails(
        bookingId: String,
        onResult: (DriverBookingDetails?, String) -> Unit
    ) {
        BookingRepository.getAcceptedBookingDetails(bookingId, onResult)
    }

    fun getAvailableBookings(
        onResult: (List<AvailableBooking>, String) -> Unit
    ) {
        BookingRepository.getAvailableBookings(onResult)
    }

    fun listenToAvailableBookings(
        onResult: (List<AvailableBooking>, String) -> Unit
    ): ListenerRegistration? {
        return BookingRepository.listenToAvailableBookings(onResult)
    }

    fun cancelBooking(
        bookingId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (bookingId.isBlank()) {
            onResult(false, "Invalid booking ID.")
            return
        }

        BookingRepository.cancelBooking(bookingId, onResult)
    }

    fun listenToPassengerBookingStatus(
        bookingId: String,
        onResult: (String?, String?) -> Unit,
        onListenerReady: (ListenerRegistration?) -> Unit
    ) {
        BookingRepository.listenToPassengerBookingStatus(
            bookingId,
            onResult,
            onListenerReady
        )
    }
}