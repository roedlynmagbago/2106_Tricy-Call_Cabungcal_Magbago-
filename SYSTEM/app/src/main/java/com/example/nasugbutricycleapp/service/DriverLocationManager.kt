package com.example.nasugbutricycleapp.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*

object DriverLocationManager {

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var requestSession = 0L

    fun initialize(context: Context) {
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(
                context.applicationContext
            )
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(
        context: Context,
        onLocationReceived: (Location) -> Unit,
        onError: (String) -> Unit
    ) {
        if (fusedLocationClient == null) {
            initialize(context)
        }
        if (
            context.checkSelfPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            context.checkSelfPermission(
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onError("Location permission is required.")
            return
        }
        stopLocationUpdates()
        val currentSession = requestSession
        val client = fusedLocationClient ?: run {
            onError("Location client is unavailable.")
            return
        }
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        ).setMinUpdateIntervalMillis(3000L).build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (currentSession != requestSession) return
                result.lastLocation?.let { location ->
                    onLocationReceived(location)
                }
            }
        }

        locationCallback = callback
        client.requestLocationUpdates(
            request,
            callback,
            Looper.getMainLooper()
        ).addOnFailureListener { exception ->
            if (currentSession != requestSession) return@addOnFailureListener
            stopLocationUpdates()
            onError(
                exception.message ?: "Unable to get device location."
            )
        }
    }

    // STOP GPS UPDATES
    fun stopLocationUpdates() {
        requestSession++

        val callback = locationCallback
        locationCallback = null

        if (callback != null) {
            fusedLocationClient?.removeLocationUpdates(callback)
        }
    }

    // CHECK IF LOCATION UPDATES ARE ACTIVE
    fun isLocationUpdating(): Boolean {
        return locationCallback != null
    }

    // RELEASE LOCATION CLIENT
    fun release() {
        stopLocationUpdates()
        fusedLocationClient = null
    }
}
