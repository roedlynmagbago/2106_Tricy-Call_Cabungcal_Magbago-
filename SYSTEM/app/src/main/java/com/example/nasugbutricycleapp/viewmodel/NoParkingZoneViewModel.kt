package com.example.nasugbutricycleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nasugbutricycleapp.model.NoParkingZone
import com.example.nasugbutricycleapp.service.NoParkingZoneService

class NoParkingZoneViewModel : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _zones = MutableLiveData<List<NoParkingZone>>(emptyList())
    val zones: LiveData<List<NoParkingZone>> = _zones

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isSuccessful = MutableLiveData<Boolean?>()
    val isSuccessful: LiveData<Boolean?> = _isSuccessful

    // ADD NO-PARKING ZONE
    fun addZone(zone: NoParkingZone) {
        _isLoading.value = true

        NoParkingZoneService.addZone(zone) { success, resultMessage ->
            _isLoading.postValue(false)
            _isSuccessful.postValue(success)
            _message.postValue(resultMessage)

            if (success) {
                getActiveZones()
            }
        }
    }

    // GET ACTIVE NO-PARKING ZONES
    fun getActiveZones() {
        _isLoading.value = true

        NoParkingZoneService.getActiveZones { result, error ->
            _isLoading.postValue(false)
            _zones.postValue(result)

            if (error != null) {
                _message.postValue(error)
                _isSuccessful.postValue(false)
            } else {
                _isSuccessful.postValue(true)
            }
        }
    }

    // CLEAR MESSAGE
    fun clearMessage() {
        _message.value = ""
        _isSuccessful.value = null
    }
}
