package com.example.nasugbutricycleapp.repository

import com.example.nasugbutricycleapp.firebase.FirebaseManager
import com.example.nasugbutricycleapp.model.Booking
import com.example.nasugbutricycleapp.model.AvailableBooking
import com.example.nasugbutricycleapp.model.BookingContact
import com.example.nasugbutricycleapp.model.DriverBookingDetails
import com.google.firebase.firestore.ListenerRegistration

object BookingRepository {

    private const val USERS = "users"
    private const val BOOKINGS = "bookings"
    private const val CONTACTS = "bookingContacts"
    private const val DRIVER_ACTIVE = "driverActiveBookings"
    private const val PASSENGER_ACTIVE = "passengerActiveBookings"

    private const val PASSENGER = "PASSENGER"
    private const val DRIVER = "DRIVER"
    private const val ACTIVE = "ACTIVE"
    private const val APPROVED = "APPROVED"
    private const val PENDING = "PENDING"
    private const val ACCEPTED = "ACCEPTED"
    private const val COMPLETED = "COMPLETED"
    private const val CANCELLED = "CANCELLED"

    private val phonePattern = Regex("^09[0-9]{9}$")

    // CREATE BOOKING
    fun createBooking(
        booking: Booking,
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = FirebaseManager.auth.currentUser?.uid

        if (uid == null) {
            onResult(false, "Please log in first.")
            return
        }

        val passengerPhone = booking.passengerPhone.trim()
        val pickupLocation = booking.pickupLocation.trim()
        val destination = booking.destination.trim()

        if (!passengerPhone.matches(phonePattern)) {
            onResult(false, "Invalid passenger phone number.")
            return
        }

        if (pickupLocation.isBlank() || destination.isBlank()) {
            onResult(false, "Please select pickup and destination.")
            return
        }

        if (
            booking.pickupLatitude !in -90.0..90.0 ||
            booking.pickupLongitude !in -180.0..180.0 ||
            booking.destinationLatitude !in -90.0..90.0 ||
            booking.destinationLongitude !in -180.0..180.0 ||
            (booking.pickupLatitude == 0.0 &&
                    booking.pickupLongitude == 0.0) ||
            (booking.destinationLatitude == 0.0 &&
                    booking.destinationLongitude == 0.0)
        ) {
            onResult(false, "Invalid location coordinates.")
            return
        }

        val db = FirebaseManager.db

        val userRef = db.collection(USERS).document(uid)
        val bookingRef = db.collection(BOOKINGS).document()
        val contactRef = db.collection(CONTACTS).document(bookingRef.id)
        val activeRef = db.collection(PASSENGER_ACTIVE).document(uid)

        val availableBooking = AvailableBooking(
            bookingId = bookingRef.id,
            pickupLocation = pickupLocation,
            pickupLatitude = booking.pickupLatitude,
            pickupLongitude = booking.pickupLongitude,
            destination = destination,
            destinationLatitude = booking.destinationLatitude,
            destinationLongitude = booking.destinationLongitude,
            driverId = "",
            status = PENDING,
            createdAt = booking.createdAt
        )

        db.runTransaction { transaction ->

            // READ DOCUMENTS FIRST
            val passenger = transaction.get(userRef)
            val activeBooking = transaction.get(activeRef)

            if (
                !passenger.exists() ||
                passenger.getString("role") != PASSENGER ||
                passenger.getString("status") != ACTIVE
            ) {
                throw IllegalStateException(
                    "Only active passengers can book."
                )
            }

            val passengerName =
                passenger.getString("name")?.trim()

            if (passengerName.isNullOrBlank()) {
                throw IllegalStateException(
                    "Passenger name not found."
                )
            }

            if (activeBooking.exists()) {
                throw IllegalStateException(
                    "You already have an active booking."
                )
            }

            val bookingContact = BookingContact(
                bookingId = bookingRef.id,
                passengerId = uid,
                passengerName = passengerName,
                passengerPhone = passengerPhone
            )

            // WRITE DOCUMENTS ATOMICALLY
            transaction.set(bookingRef, availableBooking)
            transaction.set(contactRef, bookingContact)

            transaction.set(
                activeRef,
                mapOf(
                    "bookingId" to bookingRef.id,
                    "passengerId" to uid
                )
            )

            true
        }
            .addOnSuccessListener {
                onResult(true, "Booking created successfully.")
            }
            .addOnFailureListener { error ->
                onResult(
                    false,
                    error.localizedMessage
                        ?: "Failed to create booking."
                )
            }
    }

    // ACCEPT BOOKING
    fun acceptBooking(
        bookingId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val driverId = FirebaseManager.auth.currentUser?.uid

        if (driverId == null) {
            onResult(false, "Please log in first.")
            return
        }

        val id = bookingId.trim()

        if (id.isBlank()) {
            onResult(false, "Invalid booking ID.")
            return
        }

        val db = FirebaseManager.db

        val driverRef = db.collection(USERS).document(driverId)
        val bookingRef = db.collection(BOOKINGS).document(id)
        val activeRef = db.collection(DRIVER_ACTIVE).document(driverId)

        db.runTransaction { transaction ->

            // READ ALL DOCUMENTS FIRST
            val driver = transaction.get(driverRef)
            val booking = transaction.get(bookingRef)
            val activeBooking = transaction.get(activeRef)

            if (
                !driver.exists() ||
                driver.getString("role") != DRIVER ||
                driver.getString("status") != ACTIVE ||
                driver.getString("driverApplicationStatus") != APPROVED
            ) {
                throw IllegalStateException(
                    "Only approved drivers can accept bookings."
                )
            }

            if (
                !booking.exists() ||
                booking.getString("status") != PENDING ||
                booking.getString("driverId") != ""
            ) {
                throw IllegalStateException(
                    "Booking is no longer available."
                )
            }

            if (activeBooking.exists()) {
                throw IllegalStateException(
                    "You already have an active booking."
                )
            }

            // UPDATE BOOKING
            transaction.update(
                bookingRef,
                mapOf(
                    "driverId" to driverId,
                    "status" to ACCEPTED
                )
            )

            // CREATE DRIVER ACTIVE RECORD
            transaction.set(
                activeRef,
                mapOf(
                    "bookingId" to id,
                    "driverId" to driverId
                )
            )

            true
        }
            .addOnSuccessListener {
                onResult(true, "Booking accepted successfully.")
            }
            .addOnFailureListener { error ->
                onResult(
                    false,
                    error.localizedMessage
                        ?: "Unable to accept booking."
                )
            }
    }

    // COMPLETE BOOKING
    fun completeBooking(
        bookingId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val driverId = FirebaseManager.auth.currentUser?.uid

        if (driverId == null) {
            onResult(false, "Please log in first.")
            return
        }

        val id = bookingId.trim()

        if (id.isBlank()) {
            onResult(false, "Invalid booking ID.")
            return
        }

        val db = FirebaseManager.db

        val bookingRef = db.collection(BOOKINGS).document(id)
        val contactRef = db.collection(CONTACTS).document(id)
        val driverActiveRef =
            db.collection(DRIVER_ACTIVE).document(driverId)

        db.runTransaction { transaction ->

            // READ INITIAL DOCUMENTS
            val booking = transaction.get(bookingRef)
            val contact = transaction.get(contactRef)
            val driverActive = transaction.get(driverActiveRef)

            if (!booking.exists()) {
                throw IllegalStateException("Booking not found.")
            }

            if (booking.getString("driverId") != driverId) {
                throw IllegalStateException(
                    "You are not the assigned driver."
                )
            }

            if (booking.getString("status") != ACCEPTED) {
                throw IllegalStateException(
                    "Booking must be accepted first."
                )
            }

            if (
                !driverActive.exists() ||
                driverActive.getString("bookingId") != id ||
                driverActive.getString("driverId") != driverId
            ) {
                throw IllegalStateException(
                    "Driver active booking record does not match."
                )
            }

            if (!contact.exists()) {
                throw IllegalStateException(
                    "Passenger contact not found."
                )
            }

            val passengerId =
                contact.getString("passengerId")

            if (passengerId.isNullOrBlank()) {
                throw IllegalStateException(
                    "Passenger ID not found."
                )
            }

            val passengerActiveRef =
                db.collection(PASSENGER_ACTIVE)
                    .document(passengerId)

            val passengerActive =
                transaction.get(passengerActiveRef)

            if (
                !passengerActive.exists() ||
                passengerActive.getString("bookingId") != id ||
                passengerActive.getString("passengerId") != passengerId
            ) {
                throw IllegalStateException(
                    "Passenger active booking record does not match."
                )
            }

            // COMPLETE BOOKING
            transaction.update(
                bookingRef,
                "status",
                COMPLETED
            )

            // RELEASE ACTIVE RECORDS
            transaction.delete(driverActiveRef)
            transaction.delete(passengerActiveRef)

            true
        }
            .addOnSuccessListener {
                onResult(true, "Booking completed successfully.")
            }
            .addOnFailureListener { error ->
                onResult(
                    false,
                    error.localizedMessage
                        ?: "Unable to complete booking."
                )
            }
    }

    // GET ACCEPTED BOOKING DETAILS
    fun getAcceptedBookingDetails(
        bookingId: String,
        onResult: (DriverBookingDetails?, String) -> Unit
    ) {
        val driverId = FirebaseManager.auth.currentUser?.uid

        if (driverId == null) {
            onResult(null, "Please log in first.")
            return
        }

        val id = bookingId.trim()

        if (id.isBlank()) {
            onResult(null, "Invalid booking ID.")
            return
        }

        val db = FirebaseManager.db
        val bookingRef = db.collection(BOOKINGS).document(id)

        bookingRef.get()
            .addOnSuccessListener { booking ->

                if (
                    !booking.exists() ||
                    booking.getString("driverId") != driverId ||
                    booking.getString("status") != ACCEPTED
                ) {
                    onResult(
                        null,
                        "Booking is not assigned to you."
                    )
                    return@addOnSuccessListener
                }

                db.collection(CONTACTS)
                    .document(id)
                    .get()
                    .addOnSuccessListener { contact ->

                        if (!contact.exists()) {
                            onResult(
                                null,
                                "Passenger contact not found."
                            )
                            return@addOnSuccessListener
                        }

                        val name =
                            contact.getString("passengerName")

                        val phone =
                            contact.getString("passengerPhone")

                        if (
                            name.isNullOrBlank() ||
                            phone.isNullOrBlank()
                        ) {
                            onResult(
                                null,
                                "Passenger details unavailable."
                            )
                            return@addOnSuccessListener
                        }

                        // INCLUDE PICKUP AND DESTINATION COORDINATES
                        val details = DriverBookingDetails(
                            bookingId = id,
                            passengerName = name,
                            passengerPhone = phone,
                            pickupLocation =
                                booking.getString("pickupLocation") ?: "",
                            pickupLatitude =
                                booking.getDouble("pickupLatitude") ?: 0.0,
                            pickupLongitude =
                                booking.getDouble("pickupLongitude") ?: 0.0,
                            destination =
                                booking.getString("destination") ?: "",
                            destinationLatitude =
                                booking.getDouble("destinationLatitude") ?: 0.0,
                            destinationLongitude =
                                booking.getDouble("destinationLongitude") ?: 0.0,
                            status =
                                booking.getString("status") ?: ""
                        )

                        onResult(
                            details,
                            "Booking details loaded."
                        )
                    }
                    .addOnFailureListener { error ->
                        onResult(
                            null,
                            error.localizedMessage
                                ?: "Unable to load passenger contact."
                        )
                    }
            }
            .addOnFailureListener { error ->
                onResult(
                    null,
                    error.localizedMessage
                        ?: "Unable to load booking."
                )
            }
    }

    // GET AVAILABLE BOOKINGS
    fun getAvailableBookings(
        onResult: (List<AvailableBooking>, String) -> Unit
    ) {
        val driverId = FirebaseManager.auth.currentUser?.uid

        if (driverId == null) {
            onResult(emptyList(), "Please log in first.")
            return
        }

        val db = FirebaseManager.db

        db.collection(USERS)
            .document(driverId)
            .get()
            .addOnSuccessListener { driver ->

                if (
                    !driver.exists() ||
                    driver.getString("role") != DRIVER ||
                    driver.getString("status") != ACTIVE ||
                    driver.getString("driverApplicationStatus") != APPROVED
                ) {
                    onResult(
                        emptyList(),
                        "Only approved drivers can view bookings."
                    )
                    return@addOnSuccessListener
                }

                db.collection(BOOKINGS)
                    .whereEqualTo("status", PENDING)
                    .whereEqualTo("driverId", "")
                    .get()
                    .addOnSuccessListener { result ->

                        val bookings = result.documents.mapNotNull {
                            it.toObject(AvailableBooking::class.java)
                        }

                        onResult(
                            bookings,
                            "Available bookings loaded."
                        )
                    }
                    .addOnFailureListener { error ->
                        onResult(
                            emptyList(),
                            error.localizedMessage
                                ?: "Unable to load bookings."
                        )
                    }
            }
            .addOnFailureListener { error ->
                onResult(
                    emptyList(),
                    error.localizedMessage
                        ?: "Unable to verify driver account."
                )
            }
    }

    // LISTEN TO AVAILABLE BOOKINGS
    fun listenToAvailableBookings(
        onResult: (List<AvailableBooking>, String) -> Unit,
        onListenerReady: (ListenerRegistration?) -> Unit
    ) {
        val uid = FirebaseManager.auth.currentUser?.uid

        if (uid == null) {
            onResult(emptyList(), "Please log in first.")
            onListenerReady(null)
            return
        }

        val db = FirebaseManager.db
        val userRef = db.collection(USERS).document(uid)

        userRef.get()
            .addOnSuccessListener { driver ->

                if (
                    !driver.exists() ||
                    driver.getString("role") != DRIVER ||
                    driver.getString("status") != ACTIVE ||
                    driver.getString("driverApplicationStatus") != APPROVED
                ) {
                    onResult(
                        emptyList(),
                        "Only approved drivers can view bookings."
                    )
                    onListenerReady(null)
                    return@addOnSuccessListener
                }

                val listener = db.collection(BOOKINGS)
                    .whereEqualTo("status", PENDING)
                    .whereEqualTo("driverId", "")
                    .addSnapshotListener { snapshot, error ->

                        if (error != null) {
                            onResult(
                                emptyList(),
                                error.localizedMessage
                                    ?: "Unable to load bookings."
                            )
                            return@addSnapshotListener
                        }

                        val bookings =
                            snapshot?.documents?.mapNotNull {
                                it.toObject(AvailableBooking::class.java)
                            } ?: emptyList()

                        onResult(
                            bookings,
                            "Bookings updated."
                        )
                    }

                onListenerReady(listener)
            }
            .addOnFailureListener { error ->
                onResult(
                    emptyList(),
                    error.localizedMessage
                        ?: "Unable to verify driver account."
                )
                onListenerReady(null)
            }
    }

    // CANCEL BOOKING
    fun cancelBooking(
        bookingId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val passengerId = FirebaseManager.auth.currentUser?.uid

        if (passengerId == null) {
            onResult(false, "Please log in first.")
            return
        }

        val id = bookingId.trim()

        if (id.isBlank()) {
            onResult(false, "Invalid booking ID.")
            return
        }

        val db = FirebaseManager.db

        val bookingRef = db.collection(BOOKINGS).document(id)
        val contactRef = db.collection(CONTACTS).document(id)

        val activeRef =
            db.collection(PASSENGER_ACTIVE).document(passengerId)

        db.runTransaction { transaction ->

            // READ ALL DOCUMENTS FIRST
            val booking = transaction.get(bookingRef)
            val contact = transaction.get(contactRef)
            val activeBooking = transaction.get(activeRef)

            if (!booking.exists()) {
                throw IllegalStateException("Booking not found.")
            }

            // VERIFY PASSENGER OWNERSHIP
            if (
                !contact.exists() ||
                contact.getString("passengerId") != passengerId
            ) {
                throw IllegalStateException(
                    "You cannot cancel this booking."
                )
            }

            // CHECK BOOKING STATUS
            if (booking.getString("status") != PENDING) {
                throw IllegalStateException(
                    "Booking can no longer be cancelled."
                )
            }

            if (booking.getString("driverId") != "") {
                throw IllegalStateException(
                    "A driver has already accepted this booking."
                )
            }

            // VERIFY ACTIVE BOOKING REFERENCE
            if (
                !activeBooking.exists() ||
                activeBooking.getString("bookingId") != id ||
                activeBooking.getString("passengerId") != passengerId
            ) {
                throw IllegalStateException(
                    "Active booking record does not match."
                )
            }

            // UPDATE BOOKING STATUS
            transaction.update(
                bookingRef,
                "status",
                CANCELLED
            )

            // REMOVE PASSENGER ACTIVE BOOKING
            transaction.delete(activeRef)

            true
        }
            .addOnSuccessListener {
                onResult(true, "Booking cancelled successfully.")
            }
            .addOnFailureListener { error ->
                onResult(
                    false,
                    error.localizedMessage
                        ?: "Unable to cancel booking."
                )
            }
    }

    // LISTEN TO PASSENGER'S ASSIGNED DRIVER
    fun listenToPassengerAssignedDriver(
        bookingId: String,
        onResult: (String?, String?, String?) -> Unit,
        onListenerReady: (ListenerRegistration?) -> Unit
    ) {
        val passengerId = FirebaseManager.auth.currentUser?.uid
        if (passengerId == null) {
            onResult(null, null, null)
            onListenerReady(null)
            return
        }
        val id = bookingId.trim()
        if (id.isBlank()) {
            onResult(null, null, null)
            onListenerReady(null)
            return
        }

        val db = FirebaseManager.db
        db.collection(CONTACTS)
            .document(id)
            .get()
            .addOnSuccessListener { contact ->
                if (
                    !contact.exists() ||
                    contact.getString("passengerId") != passengerId
                ) {
                    onResult(null, null, null)
                    onListenerReady(null)
                    return@addOnSuccessListener
                }

                var currentDriverId: String? = null
                val listener = db.collection(BOOKINGS)
                    .document(id)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            onResult(null, null, null)
                            return@addSnapshotListener
                        }
                        if (snapshot == null || !snapshot.exists()) {
                            currentDriverId = null
                            onResult(null, null, null)
                            return@addSnapshotListener
                        }

                        val status = snapshot.getString("status")
                        val driverId = snapshot.getString("driverId")
                        if (
                            status != ACCEPTED ||
                            driverId.isNullOrBlank()
                        ) {
                            currentDriverId = null
                            onResult(null, null, null)
                            return@addSnapshotListener
                        }
                        // AVOID REPEATED PROFILE REQUESTS
                        if (currentDriverId == driverId) {
                            return@addSnapshotListener
                        }
                        currentDriverId = driverId
                        // GET ASSIGNED DRIVER PROFILE
                        db.collection(USERS)
                            .document(driverId)
                            .get()
                            .addOnSuccessListener { driver ->
                                // CHECK IF DRIVER IS STILL ASSIGNED
                                if (currentDriverId != driverId) {
                                    return@addOnSuccessListener
                                }
                                if (!driver.exists()) {
                                    onResult(driverId, null, null)
                                    return@addOnSuccessListener
                                }
                                val driverName =
                                    driver.getString("name")

                                val driverPhone =
                                    driver.getString("phoneNumber")
                                onResult(
                                    driverId,
                                    driverName,
                                    driverPhone
                                )
                            }
                            .addOnFailureListener {
                                if (currentDriverId == driverId) {
                                    onResult(driverId, null, null)
                                }
                            }
                    }

                onListenerReady(listener)
            }
            .addOnFailureListener {
                onResult(null, null, null)
                onListenerReady(null)
            }
    }


    // LISTEN TO PASSENGER BOOKING STATUS
    fun listenToPassengerBookingStatus(
        bookingId: String,
        onResult: (String?, String?) -> Unit,
        onListenerReady: (ListenerRegistration?) -> Unit
    ) {
        val passengerId = FirebaseManager.auth.currentUser?.uid
        if (passengerId == null) {
            onResult(null, "Please log in first.")
            onListenerReady(null)
            return
        }

        val id = bookingId.trim()
        if (id.isBlank()) {
            onResult(null, "Invalid booking ID.")
            onListenerReady(null)
            return
        }

        val db = FirebaseManager.db
        val bookingRef = db.collection(BOOKINGS).document(id)
        val contactRef = db.collection(CONTACTS).document(id)

        // VERIFY BOOKING OWNERSHIP FIRST
        contactRef.get()
            .addOnSuccessListener { contact ->

                if (
                    !contact.exists() ||
                    contact.getString("passengerId") != passengerId
                ) {
                    onResult(
                        null,
                        "You cannot view this booking."
                    )
                    onListenerReady(null)
                    return@addOnSuccessListener
                }

                // START LISTENER AFTER VERIFICATION
                val listener = bookingRef.addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        onResult(
                            null,
                            error.localizedMessage
                                ?: "Unable to get booking status."
                        )
                        return@addSnapshotListener
                    }
                    if (snapshot == null || !snapshot.exists()) {
                        onResult(null, "Booking not found.")
                        return@addSnapshotListener
                    }
                    val status = snapshot.getString("status")
                    onResult(status, null)
                }
                onListenerReady(listener)
            }
            .addOnFailureListener { error ->
                onResult(
                    null,
                    error.localizedMessage
                        ?: "Unable to verify booking ownership."
                )
                onListenerReady(null)
            }
    }
}
