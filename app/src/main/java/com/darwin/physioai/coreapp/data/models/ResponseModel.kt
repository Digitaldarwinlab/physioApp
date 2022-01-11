package com.darwin.physioai.coreapp.data.models

data class ResponseModel(
    val error: Boolean,
    val message: String,
    val time_slot_mobile: List<TimeSlotMobile>
)