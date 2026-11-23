package com.example.nasugbutricycleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nasugbutricycleapp.model.DriverLocation
import com.example.nasugbutricycleapp.service.DriverLocationService
import com.google.firebase.firestore.ListenerRegistration

class DriverLocationViewModel : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _location = MutableLiveData<DriverLocation?>()
    val location: LiveData<DriverLocation?> = _location

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isSuccessful = MutableLiveData<Boolean?>()
    val isSuccessful: LiveData<Boolean?> = _isSuccessful

    private var locationListener: ListenerRegistration? = null
    private var requestSession = 0L

    // UPDATE CURRENT DRIVER LOCATION
    fun updateLocation(
        latitude: Double,
        longitude: Double,
        accuracy: Float
    ) {
        _isLoading.value = true

        DriverLocationService.updateLocation(
            latitude,
            longitude,
            accuracy
        ) { success, resultMessage ->

            _isLoading.postValue(false)
            _isSuccessful.postValue(success)
            _message.postValue(resultMessage)
        }
    }

    // GET ONE DRIVER'S LOCATION
    fun getDriverLocation(driverId: String) {
        requestSession++
        val session = requestSession

        stopLocationListener()
        _location.value = null

        if (driverId.isBlank()) {
            _isLoading.value = false
            _message.value = "Invalid driver ID."
            return
        }

        _isLoading.value = true

        DriverLocationService.getDriverLocation(driverId) { result, error ->

            if (session != requestSession) return@getDriverLocation

            _isLoading.postValue(false)
            _location.postValue(result)

            if (error != null) {
                _message.postValue(error)
            }
        }
    }

    // LISTEN TO DRIVER LOCATION
    fun listenToDriverLocation(driverId: String) {
        stopLocationListener()

        _location.value = null

        if (driverId.isBlank()) {
            _message.value = "Invalid driver ID."
            return
        }

        val session = requestSession

        locationListener =
            DriverLocationService.listenToDriverLocation(driverId) { result, error ->

                if (session != requestSession) return@listenToDriverLocation

                _location.postValue(result)

                if (error != null) {
                    _message.postValue(error)
                }
            }
    }

    // STOP LOCATION LISTENER
    fun stopLocationListener() {
        requestSession++

        locationListener?.remove()
        locationListener = null
    }

    // CLEAR MESSAGE
    fun clearMessage() {
        _message.value = ""
        _isSuccessful.value = null
    }

    override fun onCleared() {
        stopLocationListener()
        super.onCleared()
    }
}
