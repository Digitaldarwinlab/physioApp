package com.example.physioai.data.models

data class loginResponse(
    val basic_info: BasicInfo,
    val jwt: String,
    val role: String,
    val user_id: Int
)