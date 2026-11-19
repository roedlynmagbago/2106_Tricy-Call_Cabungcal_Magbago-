package com.example.nasugbutricycleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nasugbutricycleapp.model.DriverAvailability
import com.example.nasugbutricycleapp.service.DriverAvailabilityService
import com.google.firebase.firestore.ListenerRegistration

class DriverAvailabilityViewModel : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isOnline = MutableLiveData(false)
    val isOnline: LiveData<Boolean> = _isOnline

    private val _availability = MutableLiveData<DriverAvailability?>()
    val availability: LiveData<DriverAvailability?> = _availability

    private val _availableDrivers =
        MutableLiveData<List<DriverAvailability>>(emptyList())
    val availableDrivers: LiveData<List<DriverAvailability>> = _availableDrivers

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isSuccessful = MutableLiveData<Boolean?>()
    val isSuccessful: LiveData<Boolean?> = _isSuccessful

    private var availabilityListener: ListenerRegistration? = null
    private var availableDriversListener: ListenerRegistration? = null

    // SET DRIVER ONLINE OR OFFLINE
    fun setOnline(isOnline: Boolean) {
        _isLoading.value = true

        DriverAvailabilityService.setOnline(isOnline) { success, resultMessage ->
            _isLoading.postValue(false)
            _isSuccessful.postValue(success)
            _message.postValue(resultMessage)

            if (success) {
                _isOnline.postValue(isOnline)
            }
        }
    }

    // GET ONE DRIVER'S AVAILABILITY
    fun getAvailability(driverId: String) {
        if (driverId.isBlank()) {
            _availability.value = null
            _isOnline.value = false
            _message.value = "Invalid driver ID."
            return
        }

        _isLoading.value = true
        _availability.value = null
        _isOnline.value = false

        DriverAvailabilityService.getAvailability(driverId) { result, error ->
            _isLoading.postValue(false)
            _availability.postValue(result)
            _isOnline.postValue(result?.isOnline ?: false)

            if (error != null) {
                _message.postValue(error)
            }
        }
    }

    // LISTEN TO ONE DRIVER'S AVAILABILITY
    fun listenToAvailability(driverId: String) {
        stopAvailabilityListener()

        _availability.value = null
        _isOnline.value = false

        if (driverId.isBlank()) {
            _message.value = "Invalid driver ID."
            return
        }

        availabilityListener =
            DriverAvailabilityService.listenToAvailability(driverId) { result, error ->

                _availability.postValue(result)
                _isOnline.postValue(result?.isOnline ?: false)

                if (error != null) {
                    _message.postValue(error)
                }
            }
    }

    // GET ALL ONLINE DRIVERS
    fun getAvailableDrivers() {
        _isLoading.value = true
        _availableDrivers.value = emptyList()

        DriverAvailabilityService.getAvailableDrivers { drivers, error ->
            _isLoading.postValue(false)
            _availableDrivers.postValue(drivers)

            if (error != null) {
                _message.postValue(error)
            }
        }
    }

    // LISTEN TO ALL ONLINE DRIVERS
    fun listenToAvailableDrivers() {
        stopAvailableDriversListener()
        _availableDrivers.value = emptyList()

        availableDriversListener =
            DriverAvailabilityService.listenToAvailableDrivers { drivers, error ->

                _availableDrivers.postValue(drivers)

                if (error != null) {
                    _message.postValue(error)
                }
            }
    }

    // REFRESH DRIVER ONLINE STATUS
    fun refreshOnlineStatus() {
        _isLoading.value = true

        DriverAvailabilityService.refreshOnlineStatus { success, resultMessage ->
            _isLoading.postValue(false)
            _isSuccessful.postValue(success)
            _message.postValue(resultMessage)

            if (success) {
                _isOnline.postValue(true)
            }
        }
    }

    // STOP ONE DRIVER'S LISTENER
    fun stopAvailabilityListener() {
        availabilityListener?.remove()
        availabilityListener = null
    }

    // STOP ALL ONLINE DRIVERS LISTENER
    fun stopAvailableDriversListener() {
        availableDriversListener?.remove()
        availableDriversListener = null
    }

    // CLEAR MESSAGE
    fun clearMessage() {
        _message.value = ""
        _isSuccessful.value = null
    }

    override fun onCleared() {
        stopAvailabilityListener()
        stopAvailableDriversListener()
        super.onCleared()
    }
}
