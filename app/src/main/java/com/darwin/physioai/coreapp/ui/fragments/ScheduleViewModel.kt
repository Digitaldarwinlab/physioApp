package com.darwin.physioai.coreapp.ui.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.physioai.data.Repository.ScheduleRepository
import com.example.physioai.data.models.PrescriptionResponse
import com.example.physioai.data.models.ScheduleResponseX
import com.example.physioai.data.models.VisitResponse
import com.example.physioai.data.network.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ScheduleViewModel @Inject constructor (private val repository:ScheduleRepository) : ViewModel() {

    private val _ScheduleRes: MutableLiveData<Resource<ScheduleResponseX>> = MutableLiveData()
    val ScheduleRes: LiveData<Resource<ScheduleResponseX>>
        get() = _ScheduleRes

    private val _VisitRes: MutableLiveData<Resource<VisitResponse>> = MutableLiveData()
    val VisitRes: LiveData<Resource<VisitResponse>>
        get() = _VisitRes

    private val _PresRes: MutableLiveData<Resource<PrescriptionResponse>> = MutableLiveData()
    val PresRes: LiveData<Resource<PrescriptionResponse>>
        get() = _PresRes

    fun getScheduleRes(num: JsonObject) = viewModelScope.launch {
        _ScheduleRes.value = Resource.Loading
        _ScheduleRes.value = repository.getScheduleRes(num)
    }

    fun getVisit(user: JsonObject) = viewModelScope.launch {
        _VisitRes.value = Resource.Loading
        _VisitRes.value = repository.getVisit(user)
    }

    fun getPres(user: JsonObject) = viewModelScope.launch {
        _PresRes.value = Resource.Loading
        _PresRes.value = repository.getPres(user)
    }
}