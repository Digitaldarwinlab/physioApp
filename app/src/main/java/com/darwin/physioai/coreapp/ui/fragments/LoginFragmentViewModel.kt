package com.darwin.physioai.coreapp.ui.fragments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.physioai.data.Repository.UserRepository
import com.example.physioai.data.models.loginResponse
import com.example.physioai.data.network.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginFragmentViewModel @Inject constructor(private val repository: UserRepository) : ViewModel() {

    private val _loginRes: MutableLiveData<Resource<loginResponse>> = MutableLiveData()
    val loginRes: LiveData<Resource<loginResponse>>
        get() = _loginRes

    fun getUser(num: JsonObject) = viewModelScope.launch {
        Log.d("LogLoginViewModel", num.toString())
        _loginRes.value = Resource.Loading
        _loginRes.value = repository.getUser(num)
    }
}