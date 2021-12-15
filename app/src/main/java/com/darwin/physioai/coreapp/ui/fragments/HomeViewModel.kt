package com.darwin.physioai.coreapp.ui.fragments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.physioai.data.Repository.HomeRepository
import com.example.physioai.data.models.HomeResponse
import com.example.physioai.data.network.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: HomeRepository) : ViewModel() {
    private val _homeRes: MutableLiveData<Resource<HomeResponse>> = MutableLiveData()
    val homeRes: LiveData<Resource<HomeResponse>>
        get() = _homeRes

    fun getUserData(num: JsonObject) = viewModelScope.launch {
        Log.d("LogLoginViewModel", num.toString())
        _homeRes.value = Resource.Loading
        _homeRes.value = repository.getUserData(num)
    }
}