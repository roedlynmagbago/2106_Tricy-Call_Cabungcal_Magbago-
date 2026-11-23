package com.example.nasugbutricycleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nasugbutricycleapp.model.DriverAvailability
import com.example.nasugbutricycleapp.model.DriverLocation
import com.example.nasugbutricycleapp.service.PassengerTrackingManager

class PassengerTrackingViewModel : ViewModel() {

    private val _driverLocation = MutableLiveData<DriverLocation?>()
    val driverLocation: LiveData<DriverLocation?> = _driverLocation

    private val _driverAvailability = MutableLiveData<DriverAvailability?>()
    val driverAvailability: LiveData<DriverAvailability?> = _driverAvailability

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    // START TRACKING ASSIGNED DRIVER
    fun startTracking(driverId: String) {
        if (driverId.isBlank()) {
            _message.value = "Invalid driver ID."
            return
        }

        PassengerTrackingManager.startTracking(
            driverId = driverId,
            onLocationUpdate = { location, error ->
                _driverLocation.postValue(location)

                if (error != null) {
                    _message.postValue(error)
                }
            },
            onAvailabilityUpdate = { availability, error ->
                _driverAvailability.postValue(availability)

                if (error != null) {
                    _message.postValue(error)
                }
            }
        )
    }

    // STOP TRACKING
    fun stopTracking() {
        PassengerTrackingManager.stopTracking()
        _driverLocation.value = null
        _driverAvailability.value = null
    }

    // CLEAR MESSAGE
    fun clearMessage() {
        _message.value = ""
    }

    override fun onCleared() {
        stopTracking()
        super.onCleared()
    }
}
