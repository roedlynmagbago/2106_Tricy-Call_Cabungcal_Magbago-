package com.example.nasugbutricycleapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.nasugbutricycleapp.gis.GISManager
import com.example.nasugbutricycleapp.model.AvailableBooking
import com.example.nasugbutricycleapp.model.Booking
import com.example.nasugbutricycleapp.model.DriverBookingDetails
import com.example.nasugbutricycleapp.service.AuthService
import com.example.nasugbutricycleapp.service.BookingService
import com.example.nasugbutricycleapp.service.DriverLocationController
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.ListenerRegistration
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

class DriverActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var mapLibreMap: MapLibreMap? = null

    private lateinit var tvDriverName: TextView
    private lateinit var tvDriverToda: TextView
    private lateinit var tvDriverStatusBadge: TextView
    private lateinit var tvGpsStatus: TextView

    private lateinit var tvPassengerName: TextView
    private lateinit var tvPassengerStatus: TextView
    private lateinit var tvPickupLocation: TextView
    private lateinit var tvDestination: TextView
    private lateinit var tvFareAmount: TextView
    private lateinit var tvRequestStatusBadge: TextView
    private lateinit var btnAcceptRide: MaterialButton
    private lateinit var btnDeclineRide: MaterialButton

    private var bookingListener: ListenerRegistration? = null
    private var activeBookingId: String? = null
    private var currentAvailableBookings: List<AvailableBooking> = emptyList()

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                startDriverTracking()
            } else {
                Toast.makeText(this, "Location permission is required to go online.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(this)
        setContentView(R.layout.activity_rider_dashboard)

        // Bind Views
        tvDriverName = findViewById(R.id.tvDriverName)
        tvDriverToda = findViewById(R.id.tvDriverToda)
        tvDriverStatusBadge = findViewById(R.id.tvDriverStatusBadge)
        tvGpsStatus = findViewById(R.id.tvGpsStatus)

        tvPassengerName = findViewById(R.id.tvPassengerName)
        tvPassengerStatus = findViewById(R.id.tvPassengerStatus)
        tvPickupLocation = findViewById(R.id.tvPickupLocation)
        tvDestination = findViewById(R.id.tvDestination)
        tvFareAmount = findViewById(R.id.tvFareAmount)
        tvRequestStatusBadge = findViewById(R.id.tvRequestStatusBadge)
        btnAcceptRide = findViewById(R.id.btnAcceptRide)
        btnDeclineRide = findViewById(R.id.btnDeclineRide)

        val ivLogo = findViewById<ImageView>(R.id.ivLogo)
        val ivHeaderProfile = findViewById<ImageView>(R.id.ivHeaderProfile)

        // Map Setup
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync { map ->
            mapLibreMap = map

            val nasugbu = LatLng(14.0669, 120.6318)
            map.cameraPosition = CameraPosition.Builder()
                .target(nasugbu)
                .zoom(13.5)
                .build()

            map.setStyle("https://tiles.openfreemap.org/styles/liberty") {
                GISManager.loadGISLayers(map)
            }
        }

        // Setup Header & Profile Clicks
        ivLogo?.setOnClickListener { finish() }
        ivHeaderProfile?.setOnClickListener {
            startActivity(Intent(this, DriverProfileActivity::class.java))
        }

        // Setup Online/Offline Toggle
        tvDriverStatusBadge.setOnClickListener {
            toggleTracking()
        }

        // Setup Decline/Refresh Ride
        btnDeclineRide.setOnClickListener {
            listenForAvailableBookings()
            Toast.makeText(this, "Refreshed dispatch list", Toast.LENGTH_SHORT).show()
        }

        updateTrackingUI()
        loadDriverInformation()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        listenForAvailableBookings()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        bookingListener?.remove()
        bookingListener = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        bookingListener?.remove()
        bookingListener = null
        super.onDestroy()
    }

    private fun loadDriverInformation() {
        AuthService.getCurrentUser { user, _ ->
            runOnUiThread {
                if (user != null) {
                    val displayName = user.name.ifBlank {
                        user.username.ifBlank {
                            user.email.substringBefore("@")
                        }
                    }
                    tvDriverName.text = displayName.ifBlank { "Driver Profile" }
                    tvDriverToda.text = if (user.toda.isNotBlank()) "TODA #${user.toda}" else "Verified Driver"
                }
            }
        }
    }

    private fun toggleTracking() {
        if (DriverLocationController.isTracking()) {
            DriverLocationController.stopTracking { message ->
                runOnUiThread {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    updateTrackingUI()
                }
            }
        } else {
            val fineGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val coarseGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (fineGranted || coarseGranted) {
                startDriverTracking()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun startDriverTracking() {
        DriverLocationController.startTracking(this) { message ->
            runOnUiThread {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                updateTrackingUI()
            }
        }
        updateTrackingUI()
    }

    private fun updateTrackingUI() {
        val isOnline = DriverLocationController.isTracking()

        if (isOnline) {
            tvDriverStatusBadge.text = "● ONLINE"
            tvDriverStatusBadge.setBackgroundColor(Color.parseColor("#00E5CC"))
            tvDriverStatusBadge.setTextColor(Color.parseColor("#0B1B2B"))
            tvGpsStatus.text = "GPS Active"
        } else {
            tvDriverStatusBadge.text = "● GO ONLINE"
            tvDriverStatusBadge.setBackgroundColor(Color.parseColor("#1E293B"))
            tvDriverStatusBadge.setTextColor(Color.parseColor("#FFFFFF"))
            tvGpsStatus.text = "GPS Offline"
        }
    }

    private fun listenForAvailableBookings() {
        bookingListener?.remove()

        BookingService.listenToAvailableBookings(
            onResult = { bookings, error ->
                runOnUiThread {
                    if (error.isBlank()) {
                        currentAvailableBookings = bookings
                        displayAvailableBookings(bookings)
                    }
                }
            },
            onListenerReady = { registration ->
                bookingListener = registration
            }
        )
    }

    private fun displayAvailableBookings(bookings: List<AvailableBooking>) {
        if (activeBookingId != null) return

        if (bookings.isEmpty()) {
            tvPassengerName.text = "No active requests"
            tvPassengerStatus.text = "Tap 'Simulate Request' to test ride dispatch"
            tvPickupLocation.text = "Nasugbu Public Market (Sakayan 1)"
            tvDestination.text = "Barangay Wawa Beachfront (Babaan)"
            tvFareAmount.text = "₱45.00"
            tvRequestStatusBadge.text = "Standby Mode"

            btnAcceptRide.isEnabled = true
            btnAcceptRide.text = "Simulate Request"
            btnAcceptRide.setOnClickListener {
                createSimulatedTestBooking()
            }
            return
        }

        val firstBooking = bookings.first()
        tvPassengerName.text = "Passenger ${firstBooking.bookingId.takeLast(4)}"
        tvPassengerStatus.text = "Status: ${firstBooking.status}"
        tvPickupLocation.text = firstBooking.pickupLocation
        tvDestination.text = firstBooking.destination
        tvFareAmount.text = "₱40.00 - ₱70.00"
        tvRequestStatusBadge.text = "Pending Accept"

        btnAcceptRide.isEnabled = true
        btnAcceptRide.text = "Accept Ride"
        btnAcceptRide.setOnClickListener {
            acceptBooking(firstBooking.bookingId)
        }
    }

    private fun createSimulatedTestBooking() {
        Toast.makeText(this, "Simulating passenger request...", Toast.LENGTH_SHORT).show()
        val testBooking = Booking(
            passengerPhone = "09987654321",
            pickupLocation = "Nasugbu Public Market (Sakayan 1)",
            pickupLatitude = 14.072,
            pickupLongitude = 120.633,
            destination = "Barangay Wawa Beachfront (Babaan)",
            destinationLatitude = 14.074,
            destinationLongitude = 120.632
        )
        BookingService.createBooking(testBooking) { success, message ->
            runOnUiThread {
                Toast.makeText(this, "Simulated Request: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun acceptBooking(bookingId: String) {
        Toast.makeText(this, "Accepting ride...", Toast.LENGTH_SHORT).show()

        BookingService.acceptBooking(bookingId) { success, message ->
            runOnUiThread {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                if (success) {
                    activeBookingId = bookingId
                    loadAcceptedBooking(bookingId)
                }
            }
        }
    }

    private fun loadAcceptedBooking(bookingId: String) {
        BookingService.getAcceptedBookingDetails(bookingId) { details, error ->
            runOnUiThread {
                if (details != null) {
                    displayAcceptedBooking(details)
                } else if (error.isNotBlank()) {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayAcceptedBooking(booking: DriverBookingDetails) {
        tvPassengerName.text = booking.passengerName
        tvPassengerStatus.text = "Phone: ${booking.passengerPhone}"
        tvPickupLocation.text = booking.pickupLocation
        tvDestination.text = booking.destination
        tvFareAmount.text = "₱45.00"
        tvRequestStatusBadge.text = "Accepted - In Progress"

        btnAcceptRide.isEnabled = true
        btnAcceptRide.text = "Complete Booking"
        btnAcceptRide.setOnClickListener {
            completeBooking(booking.bookingId)
        }
    }

    private fun completeBooking(bookingId: String) {
        Toast.makeText(this, "Completing booking...", Toast.LENGTH_SHORT).show()

        BookingService.completeBooking(bookingId) { success, message ->
            runOnUiThread {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                if (success) {
                    activeBookingId = null
                    listenForAvailableBookings()
                }
            }
        }
    }
}
