package com.darwin.physioai.coreapp.ui.fragments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.physioai.data.Repository.AchievementRepository
import com.example.physioai.data.models.AchievementResponse
import com.example.physioai.data.network.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AcheivementViewModel @Inject constructor(private val repository: AchievementRepository) : ViewModel() {

    private val _AchievementRes: MutableLiveData<Resource<AchievementResponse>> = MutableLiveData()
    val AchievementRes: LiveData<Resource<AchievementResponse>>
        get() = _AchievementRes

    fun getData(user: JsonObject) = viewModelScope.launch {
        Log.d("LogAchievementViewModel", user.toString())
        _AchievementRes.value = Resource.Loading
        _AchievementRes.value = repository.getData(user)
    }
}