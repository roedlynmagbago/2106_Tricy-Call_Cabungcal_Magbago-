
package com.example.nasugbutricycleapp

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.example.nasugbutricycleapp.firebase.FirebaseManager
import com.example.nasugbutricycleapp.model.Booking
import com.example.nasugbutricycleapp.repository.BookingRepository

class BookingTestActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        showLogin()
    }

    private fun showLogin() {
        val emailInput = EditText(this).apply {
            hint = "Email"
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

        val passwordInput = EditText(this).apply {
            hint = "Password"
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
            addView(emailInput)
            addView(passwordInput)
        }

        AlertDialog.Builder(this)
            .setTitle("Tricy-Call Test Login")
            .setView(layout)
            .setCancelable(false)
            .setPositiveButton("Login") { _, _ ->
                val email = emailInput.text.toString().trim()
                val password = passwordInput.text.toString()

                FirebaseManager.auth.signOut()

                FirebaseManager.auth
                    .signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        checkRole()
                    }
                    .addOnFailureListener { error ->
                        showMessage("Login failed: ${error.message}")
                        showLogin()
                    }
            }
            .setNegativeButton("Cancel") { _, _ ->
                finish()
            }
            .show()
    }

    private fun checkRole() {
        val uid = FirebaseManager.auth.currentUser?.uid ?: return

        FirebaseManager.db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { user ->

                if (!user.exists() ||
                    user.getString("status") != "ACTIVE"
                ) {
                    FirebaseManager.auth.signOut()
                    showMessage("Account is not active.")
                    showLogin()
                    return@addOnSuccessListener
                }

                when (user.getString("role")) {
                    "PASSENGER" -> createTestBooking()

                    "DRIVER" -> {
                        if (user.getString("driverApplicationStatus") == "APPROVED") {
                            showDriverBookings()
                        } else {
                            showMessage("Driver is not approved.")
                        }
                    }

                    else -> {
                        showMessage("This role is not used for booking tests.")
                    }
                }
            }
            .addOnFailureListener { error ->
                showMessage("Unable to check user role: ${error.message}")
            }
    }

    private fun createTestBooking() {
        val testBooking = Booking(
            passengerPhone = "09123456789",
            pickupLocation = "Nasugbu Town Proper",
            pickupLatitude = 14.072,
            pickupLongitude = 120.633,
            destination = "Nasugbu Public Market",
            destinationLatitude = 14.074,
            destinationLongitude = 120.632
        )

        BookingRepository.createBooking(testBooking) { success, message ->
            showMessage(message)
            Log.d(
                "BOOKING_TEST",
                "Create booking: $success - $message"
            )
        }
    }

    private fun showDriverBookings() {
        val driverId = FirebaseManager.auth.currentUser?.uid ?: return

        FirebaseManager.db.collection("bookings")
            .whereIn("status", listOf("PENDING", "ACCEPTED"))
            .get()
            .addOnSuccessListener { result ->

                val bookings = result.documents.filter { document ->
                    val status = document.getString("status")
                    val assignedDriver = document.getString("driverId")

                    status == "PENDING" ||
                            (status == "ACCEPTED" &&
                                    assignedDriver == driverId)
                }

                if (bookings.isEmpty()) {
                    showMessage("No available bookings.")
                    return@addOnSuccessListener
                }

                val choices = bookings.map { document ->
                    val pickup =
                        document.getString("pickupLocation") ?: ""
                    val destination =
                        document.getString("destination") ?: ""
                    val status =
                        document.getString("status") ?: ""

                    "[$status] $pickup → $destination"
                }.toTypedArray()

                AlertDialog.Builder(this)
                    .setTitle("Driver Bookings")
                    .setItems(choices) { _, selected ->
                        val booking = bookings[selected]
                        val bookingId = booking.id

                        if (booking.getString("status") == "ACCEPTED") {
                            AlertDialog.Builder(this)
                                .setTitle("Complete Booking?")
                                .setMessage("Has the ride been completed?")
                                .setPositiveButton("Complete") { _, _ ->
                                    BookingRepository.completeBooking(bookingId) {
                                            _, message ->
                                        showMessage(message)
                                    }
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                        } else {
                            confirmBooking(bookingId)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .addOnFailureListener { error ->
                showMessage("Failed to load bookings: ${error.message}")
            }
    }

    private fun confirmBooking(bookingId: String) {
        AlertDialog.Builder(this)
            .setTitle("Booking Actions")
            .setMessage("What would you like to do?")
            .setPositiveButton("Accept") { _, _ ->

                BookingRepository.acceptBooking(bookingId) { success, message ->
                    showMessage(message)

                    if (success) {
                        FirebaseManager.db
                            .collection("bookingContacts")
                            .document(bookingId)
                            .get()
                            .addOnSuccessListener { contact ->
                                val phone =
                                    contact.getString("passengerPhone")

                                showMessage(
                                    "Passenger phone: ${phone ?: "Unavailable"}"
                                )
                            }
                            .addOnFailureListener {
                                showMessage(
                                    "Unable to retrieve passenger contact."
                                )
                            }
                    }
                }
            }
            .setNeutralButton("Complete") { _, _ ->
                BookingRepository.completeBooking(bookingId) { _, message ->
                    showMessage(message)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.d("BOOKING_TEST", message)
    }
}
