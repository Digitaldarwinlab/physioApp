package com.darwin.physioai.posenet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.physioai.data.Repository.HomeRepository
import com.example.physioai.data.Repository.StatsRepository
import com.example.physioai.data.models.HomeResponse
import com.example.physioai.data.network.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class StatsViewModel @Inject constructor(private val repository: StatsRepository) : ViewModel() {
    private val _stats: MutableLiveData<Resource<HomeResponse>> = MutableLiveData()
    val stats: LiveData<Resource<HomeResponse>>
        get() = _stats

    fun getstatsres(num: JsonObject) = viewModelScope.launch {
        Log.d("LogLoginViewModel", num.toString())
        _stats.value = Resource.Loading
        _stats.value = repository.getstats(num)
    }
}