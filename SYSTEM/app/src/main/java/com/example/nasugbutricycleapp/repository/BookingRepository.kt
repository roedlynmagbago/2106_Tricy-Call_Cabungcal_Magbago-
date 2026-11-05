package com.example.nasugbutricycleapp.repository
import com.example.nasugbutricycleapp.firebase.FirebaseManager
import com.example.nasugbutricycleapp.model.Booking
import com.example.nasugbutricycleapp.model.AvailableBooking
import com.example.nasugbutricycleapp.model.BookingContact
import com.example.nasugbutricycleapp.model.DriverBookingDetails
import com.google.firebase.firestore.ListenerRegistration


object BookingRepository {

    fun createBooking(
        booking: Booking,
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = FirebaseManager.auth.currentUser?.uid
        if (uid == null) {
            onResult(false, "Please log in first.")
            return
        }
        if (!booking.passengerPhone.trim()
                .matches(Regex("^09[0-9]{9}$"))) {
            onResult(false, "Invalid passenger phone number.")
            return
        }
        if (booking.pickupLocation.isBlank() ||
            booking.destination.isBlank()) {
            onResult(false, "Please select pickup and destination.")
            return
        }
        if (booking.pickupLatitude !in -90.0..90.0 ||
            booking.pickupLongitude !in -180.0..180.0 ||
            booking.destinationLatitude !in -90.0..90.0 ||
            booking.destinationLongitude !in -180.0..180.0 ||
            (booking.pickupLatitude == 0.0 &&
                    booking.pickupLongitude == 0.0) ||
            (booking.destinationLatitude == 0.0 &&
                    booking.destinationLongitude == 0.0)) {
            onResult(false, "Invalid location coordinates.")
            return
        }
        val db = FirebaseManager.db
        val userRef = db.collection("users").document(uid)
        userRef.get()
            .addOnSuccessListener { passenger ->
                if (passenger.getString("role") != "PASSENGER" ||
                    passenger.getString("status") != "ACTIVE") {
                    onResult(false, "Only active passengers can book.")
                    return@addOnSuccessListener
                }
                val passengerName =
                    passenger.getString("name")?.trim()
                if (passengerName.isNullOrBlank()) {
                    onResult(false, "Passenger name not found.")
                    return@addOnSuccessListener
                }
                val bookingRef =
                    db.collection("bookings").document()
                val contactRef =
                    db.collection("bookingContacts")
                        .document(bookingRef.id)
                val activeRef =
                    db.collection("passengerActiveBookings")
                        .document(uid)
                val availableBooking = AvailableBooking(
                    bookingId = bookingRef.id,
                    pickupLocation = booking.pickupLocation.trim(),
                    pickupLatitude = booking.pickupLatitude,
                    pickupLongitude = booking.pickupLongitude,
                    destination = booking.destination.trim(),
                    destinationLatitude = booking.destinationLatitude,
                    destinationLongitude = booking.destinationLongitude,
                    driverId = "",
                    status = "PENDING"
                )
                val bookingContact = BookingContact(
                    bookingId = bookingRef.id,
                    passengerId = uid,
                    passengerName = passengerName,
                    passengerPhone = booking.passengerPhone.trim()
                )
                // CHECK ACTIVE BOOKING AND CREATE ATOMICALLY
                db.runTransaction { transaction ->
                    val activeBooking = transaction.get(activeRef)
                    if (activeBooking.exists()) {
                        throw Exception(
                            "You already have an active booking."
                        )
                    }
                    transaction.set(
                        bookingRef,
                        availableBooking
                    )
                    transaction.set(
                        contactRef,
                        bookingContact
                    )
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
                        onResult(
                            true,
                            "Booking created successfully."
                        )
                    }
                    .addOnFailureListener { error ->
                        onResult(
                            false,
                            error.message ?: "Failed to create booking."
                        )
                    }
            }
            .addOnFailureListener {
                onResult(
                    false,
                    "Unable to load passenger information."
                )
            }
    }

    fun acceptBooking(
        bookingId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val driverId = FirebaseManager.auth.currentUser?.uid
        if (driverId == null) {
            onResult(false, "Please log in first.")
            return
        }
        val db = FirebaseManager.db
        val driverRef = db.collection("users").document(driverId)
        val bookingRef = db.collection("bookings").document(bookingId)
        val activeRef = db.collection("driverActiveBookings").document(driverId)

        driverRef.get()
            .addOnSuccessListener { driver ->
                if (driver.getString("role") != "DRIVER" ||
                    driver.getString("status") != "ACTIVE" ||
                    driver.getString("driverApplicationStatus") != "APPROVED") {
                    onResult(false, "Only approved drivers can accept bookings.")
                    return@addOnSuccessListener
                }

                db.runTransaction { transaction ->
                    val booking = transaction.get(bookingRef)
                    val activeBooking = transaction.get(activeRef)
                    if (!booking.exists() ||
                        booking.getString("status") != "PENDING" ||
                        booking.getString("driverId") != "") {
                        throw Exception("Booking is no longer available.")
                    }

                    if (activeBooking.exists() &&
                        activeBooking.getString("bookingId") != "") {
                        throw Exception("You already have an active booking.")
                    }

                    transaction.update(
                        bookingRef,
                        mapOf(
                            "driverId" to driverId,
                            "status" to "ACCEPTED"
                        )
                    )

                    transaction.set(
                        activeRef,
                        mapOf(
                            "bookingId" to bookingId,
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
                            error.message ?: "Unable to accept booking."
                        )
                    }
            }
            .addOnFailureListener {
                onResult(false, "Unable to verify driver account.")
            }
    }

    fun completeBooking(
        bookingId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val driverId = FirebaseManager.auth.currentUser?.uid
        if (driverId == null) {
            onResult(false, "Please log in first.")
            return
        }
        if (bookingId.isBlank()) {
            onResult(false, "Invalid booking ID.")
            return
        }

        val db = FirebaseManager.db
        val bookingRef = db.collection("bookings")
            .document(bookingId)
        val contactRef = db.collection("bookingContacts")
            .document(bookingId)
        val driverActiveRef = db.collection("driverActiveBookings")
            .document(driverId)

        db.runTransaction { transaction ->
            // READ BOOKING AND CONTACT FIRST
            val booking = transaction.get(bookingRef)
            val contact = transaction.get(contactRef)
            val driverActive = transaction.get(driverActiveRef)

            if (!booking.exists()) {
                throw Exception("Booking not found.")
            }
            if (booking.getString("driverId") != driverId) {
                throw Exception("You are not the assigned driver.")
            }
            if (booking.getString("status") != "ACCEPTED") {
                throw Exception("Booking must be accepted first.")
            }
            if (!driverActive.exists() ||
                driverActive.getString("bookingId") != bookingId ||
                driverActive.getString("driverId") != driverId) {
                throw Exception("Driver active booking record does not match.")
            }
            if (!contact.exists()) {
                throw Exception("Passenger contact not found.")
            }
            val passengerId = contact.getString("passengerId")

            if (passengerId.isNullOrBlank()) {
                throw Exception("Passenger ID not found.")
            }
            val passengerActiveRef =
                db.collection("passengerActiveBookings")
                    .document(passengerId)
            val passengerActive =
                transaction.get(passengerActiveRef)
            if (!passengerActive.exists() ||
                passengerActive.getString("bookingId") != bookingId ||
                passengerActive.getString("passengerId") != passengerId) {
                throw Exception("Passenger active booking record does not match.")
            }
            // COMPLETE BOOKING
            transaction.update(
                bookingRef,
                "status",
                "COMPLETED"
            )
            // RELEASE DRIVER AND PASSENGER
            transaction.delete(driverActiveRef)
            transaction.delete(passengerActiveRef)

            true
        }
            .addOnSuccessListener {
                onResult(
                    true,
                    "Booking completed successfully."
                )
            }
            .addOnFailureListener { error ->
                onResult(
                    false,
                    error.message ?: "Unable to complete booking."
                )
            }
    }

    fun getAcceptedBookingDetails(
        bookingId: String,
        onResult: (DriverBookingDetails?, String) -> Unit
    ) {
        val driverId = FirebaseManager.auth.currentUser?.uid
        if (driverId == null) {
            onResult(null, "Please log in first.")
            return
        }
        val db = FirebaseManager.db
        db.collection("bookings").document(bookingId).get()
            .addOnSuccessListener { booking ->
                if (!booking.exists() ||
                    booking.getString("driverId") != driverId ||
                    booking.getString("status") != "ACCEPTED") {
                    onResult(null, "Booking is not assigned to you.")
                    return@addOnSuccessListener
                }
                db.collection("bookingContacts").document(bookingId).get()
                    .addOnSuccessListener { contact ->
                        val name = contact.getString("passengerName")
                        val phone = contact.getString("passengerPhone")
                        if (name.isNullOrBlank() || phone.isNullOrBlank()) {
                            onResult(null, "Passenger details unavailable.")
                            return@addOnSuccessListener
                        }
                        val details = DriverBookingDetails(
                            bookingId = bookingId,
                            passengerName = name,
                            passengerPhone = phone,
                            pickupLocation = booking.getString("pickupLocation") ?: "",
                            destination = booking.getString("destination") ?: "",
                            status = booking.getString("status") ?: ""
                        )
                        onResult(details, "Booking details loaded.")
                    }
                    .addOnFailureListener {
                        onResult(null, "Unable to load passenger contact.")
                    }
            }
            .addOnFailureListener {
                onResult(null, "Unable to load booking.")
            }
    }
    fun getAvailableBookings(
        onResult: (List<AvailableBooking>, String) -> Unit
    ) {
        val driverId = FirebaseManager.auth.currentUser?.uid
        if (driverId == null) {
            onResult(emptyList(), "Please log in first.")
            return
        }
        val db = FirebaseManager.db
        db.collection("users").document(driverId).get()
            .addOnSuccessListener { driver ->
                if (driver.getString("role") != "DRIVER" ||
                    driver.getString("status") != "ACTIVE" ||
                    driver.getString("driverApplicationStatus") != "APPROVED") {

                    onResult(emptyList(), "Only approved drivers can view bookings.")
                    return@addOnSuccessListener
                }
                db.collection("bookings")
                    .whereEqualTo("status", "PENDING")
                    .get()
                    .addOnSuccessListener { result ->
                        val bookings = result.documents.mapNotNull {
                            it.toObject(AvailableBooking::class.java)
                        }
                        onResult(bookings, "Available bookings loaded.")
                    }
                    .addOnFailureListener { error ->
                        onResult(
                            emptyList(),
                            error.message ?: "Unable to load bookings."
                        )
                    }
            }
            .addOnFailureListener {
                onResult(emptyList(), "Unable to verify driver account.")
            }
    }
    fun listenToAvailableBookings(
        onResult: (List<AvailableBooking>, String) -> Unit
    ): ListenerRegistration? {
        val uid = FirebaseManager.auth.currentUser?.uid
        if (uid == null) {
            onResult(emptyList(), "Please log in first.")
            return null
        }
        val db = FirebaseManager.db
        val userRef = db.collection("users").document(uid)
        userRef.get()
            .addOnSuccessListener { driver ->

                if (!driver.exists() ||
                    driver.getString("role") != "DRIVER" ||
                    driver.getString("status") != "ACTIVE" ||
                    driver.getString("driverApplicationStatus") != "APPROVED"
                ) {
                    onResult(
                        emptyList(),
                        "Only approved drivers can view bookings."
                    )
                    return@addOnSuccessListener
                }

                db.collection("bookings")
                    .whereEqualTo("status", "PENDING")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            onResult(
                                emptyList(),
                                error.message ?: "Unable to load bookings."
                            )
                            return@addSnapshotListener
                        }
                        val bookings = snapshot?.documents?.mapNotNull {
                            it.toObject(AvailableBooking::class.java)
                        } ?: emptyList()

                        onResult(bookings, "Bookings updated.")
                    }
            }
            .addOnFailureListener { error ->
                onResult(
                    emptyList(),
                    error.message ?: "Unable to verify driver account."
                )
            }
        return null
    }

    fun cancelBooking(
        bookingId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val passengerId = FirebaseManager.auth.currentUser?.uid
        if (passengerId == null) {
            onResult(false, "Please log in first.")
            return
        }
        if (bookingId.isBlank()) {
            onResult(false, "Invalid booking ID.")
            return
        }
        val db = FirebaseManager.db
        val bookingRef = db.collection("bookings")
            .document(bookingId)
        val contactRef = db.collection("bookingContacts")
            .document(bookingId)
        val activeRef = db.collection("passengerActiveBookings")
            .document(passengerId)

        db.runTransaction { transaction ->
            // READ ALL DOCUMENTS FIRST
            val booking = transaction.get(bookingRef)
            val contact = transaction.get(contactRef)
            val activeBooking = transaction.get(activeRef)

            // CHECK BOOKING
            if (!booking.exists()) {
                throw Exception("Booking not found.")
            }
            // VERIFY PASSENGER OWNERSHIP
            if (!contact.exists() ||
                contact.getString("passengerId") != passengerId) {
                throw Exception("You cannot cancel this booking.")
            }
            // CHECK BOOKING STATUS
            if (booking.getString("status") != "PENDING") {
                throw Exception("Booking can no longer be cancelled.")
            }
            // CHECK IF DRIVER ALREADY ACCEPTED
            if (booking.getString("driverId") != "") {
                throw Exception("A driver has already accepted this booking.")
            }
            // VERIFY ACTIVE BOOKING REFERENCE
            if (!activeBooking.exists() ||
                activeBooking.getString("bookingId") != bookingId ||
                activeBooking.getString("passengerId") != passengerId) {
                throw Exception("Active booking record does not match.")
            }
            // UPDATE BOOKING STATUS
            transaction.update(
                bookingRef,
                "status",
                "CANCELLED"
            )
            // REMOVE PASSENGER ACTIVE BOOKING
            transaction.delete(activeRef)
            true
        }
            .addOnSuccessListener {
                onResult(
                    true,
                    "Booking cancelled successfully."
                )
            }
            .addOnFailureListener { error ->
                onResult(
                    false,
                    error.message ?: "Unable to cancel booking."
                )
            }
    }

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
        if (bookingId.isBlank()) {
            onResult(null, "Invalid booking ID.")
            onListenerReady(null)
            return
        }
        val db = FirebaseManager.db
        val bookingRef = db.collection("bookings")
            .document(bookingId)
        val contactRef = db.collection("bookingContacts")
            .document(bookingId)
        // VERIFY BOOKING OWNERSHIP FIRST
        contactRef.get()
            .addOnSuccessListener { contact ->
                if (!contact.exists() ||
                    contact.getString("passengerId") != passengerId) {

                    onResult(null, "You cannot view this booking.")
                    onListenerReady(null)
                    return@addOnSuccessListener
                }
                // START LISTENER AFTER VERIFICATION
                val listener = bookingRef.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onResult(
                            null,
                            error.message ?: "Unable to get booking status."
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
            .addOnFailureListener {
                onResult(null, "Unable to verify booking ownership.")
                onListenerReady(null)
            }
    }
}
