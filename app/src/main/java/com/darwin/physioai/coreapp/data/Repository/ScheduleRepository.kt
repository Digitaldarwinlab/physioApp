package com.example.physioai.data.Repository

import com.example.taskmotl.data.Network.ApiCall.SafeApiCall
import com.example.taskmotl.data.Network.ApiCall.UserApi
import com.google.gson.JsonObject
import javax.inject.Inject

class ScheduleRepository @Inject constructor(private val api: UserApi): SafeApiCall {

    suspend fun getScheduleRes(num: JsonObject) = safeApiCall {
        api.getScheduleRes(num)
    }

    suspend fun getVisit(user: JsonObject) = safeApiCall {
        api.getvisit(user)
    }

    suspend fun getPres(user: JsonObject) = safeApiCall {
        api.getpres(user)
    }
}