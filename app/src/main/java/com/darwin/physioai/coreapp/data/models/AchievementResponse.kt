package com.example.physioai.data.models

data class AchievementResponse(
    val DailyLiving_koos_score: Double?,
    val Life_koos_score: Int?,
    val RecentHistory: String?,
    val Sports_koos_score: Double?,
    val Stiffness_koos_score: Double?,
    val Symptoms_koos_score: Int?,
    val data_vertical_bar: List<DataVerticalBar>?,
    val data_vertical_bar2: List<DataVerticalBar2>?,
    val pain_koos_score: Double?,
    val pain_meter: Int?,
    val scars: String?,
    val score: Double
)