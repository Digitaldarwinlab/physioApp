package com.darwin.physioai.coreapp.ui.fragments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.physioai.data.Repository.ProfileRepository
import com.example.physioai.data.models.profileResponse
import com.example.physioai.data.network.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(private val repository: ProfileRepository): ViewModel() {

    private val _ProfileRes: MutableLiveData<Resource<profileResponse>> = MutableLiveData()
    val ProfileRes: LiveData<Resource<profileResponse>>
        get() = _ProfileRes

    fun getprofileRes(num: JsonObject) = viewModelScope.launch {
        Log.d("LogLoginViewModel", num.toString())
        _ProfileRes.value = Resource.Loading
        _ProfileRes.value = repository.getProfile(num)
    }
}