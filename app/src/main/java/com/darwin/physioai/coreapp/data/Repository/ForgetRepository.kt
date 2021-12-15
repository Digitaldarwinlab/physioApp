package com.example.physioai.data.Repository



import com.example.taskmotl.data.Network.ApiCall.SafeApiCall
import com.example.taskmotl.data.Network.ApiCall.UserApi
import com.google.gson.JsonObject
import javax.inject.Inject


class ForgetRepository @Inject constructor(private val api: UserApi): SafeApiCall {

    suspend fun getforgetpass(num: JsonObject) = safeApiCall {
        api.getforgetpass(num)
    }
}