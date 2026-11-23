package com.example.nasugbutricycleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nasugbutricycleapp.model.ParadaSchedule
import com.example.nasugbutricycleapp.service.ParadaScheduleService

class ParadaScheduleViewModel : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _schedules =
        MutableLiveData<List<ParadaSchedule>>(emptyList())
    val schedules: LiveData<List<ParadaSchedule>> = _schedules

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isSuccessful = MutableLiveData<Boolean?>()
    val isSuccessful: LiveData<Boolean?> = _isSuccessful

    // ADD PARADA SCHEDULE
    fun addSchedule(schedule: ParadaSchedule) {
        _isLoading.value = true

        ParadaScheduleService.addSchedule(schedule) { success, resultMessage ->
            _isLoading.postValue(false)
            _isSuccessful.postValue(success)
            _message.postValue(resultMessage)

            if (success) {
                getActiveSchedules()
            }
        }
    }

    // GET ACTIVE PARADA SCHEDULES
    fun getActiveSchedules() {
        _isLoading.value = true

        ParadaScheduleService.getActiveSchedules { result, error ->
            _isLoading.postValue(false)
            _schedules.postValue(result)

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
