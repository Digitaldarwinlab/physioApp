package com.example.physioai.data.models

data class ScheduleResponseX(
    val error: Boolean,
    val message: String,
    val time_slot_mobile: List<TimeSlotMobileX>
)