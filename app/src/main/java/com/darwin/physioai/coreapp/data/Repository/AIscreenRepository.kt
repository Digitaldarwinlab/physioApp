package com.darwin.physioai.coreapp.data.Repository

import com.example.taskmotl.data.Network.ApiCall.SafeApiCall
import com.example.taskmotl.data.Network.ApiCall.UserApi
import com.google.gson.JsonObject
import javax.inject.Inject

class AIscreenRepository @Inject constructor(private val api: UserApi): SafeApiCall {

    suspend fun updateCP(user: JsonObject) = safeApiCall {
        api.updatecp(user)
    }
}