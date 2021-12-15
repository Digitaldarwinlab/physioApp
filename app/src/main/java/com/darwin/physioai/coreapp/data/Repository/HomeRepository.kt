package com.example.physioai.data.Repository



import com.example.taskmotl.data.Network.ApiCall.SafeApiCall
import com.example.taskmotl.data.Network.ApiCall.UserApi
import com.google.gson.JsonObject
import javax.inject.Inject


class HomeRepository @Inject constructor(private val api: UserApi): SafeApiCall {

    suspend fun getUserData(num: JsonObject) = safeApiCall {
        api.getUserData(num)
    }
}