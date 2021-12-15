package com.example.taskmotl.data.Network.ApiCall


import com.example.physioai.data.models.*
import com.google.gson.JsonObject
import retrofit2.http.*

interface UserApi {

    @POST("api/login/")
    suspend fun login(@Body user: JsonObject?): loginResponse

    @POST("api/get_episode/")
    suspend fun getUserData(@Body user: JsonObject?): HomeResponse

    @POST("api/password_reset/")
    suspend fun getforgetpass(@Body user: JsonObject?): forgetpassResponse

    @POST("api/get-care-plan_mobile/")
    suspend fun getScheduleRes(@Body user: JsonObject?): ScheduleResponseX

    @POST("api/patient-profile/")
    suspend fun getprofile(@Body user: JsonObject?): profileResponse

    @POST("api/exercise_detail/")
    suspend fun getinstructions(@Body user: JsonObject?): InstructionRes

    @POST("api/update-patient/")
    suspend fun updateprofile(@Body user: JsonObject?): EditResponse

    @POST("api/patient-progress/")
    suspend fun getdata(@Body user: JsonObject?): AchievementResponse

    @POST("api/patient_visit/")
    suspend fun getvisit(@Body user: JsonObject?): VisitResponse

    @POST("api/get_pres/")
    suspend fun getpres(@Body user: JsonObject?): PrescriptionResponse
}