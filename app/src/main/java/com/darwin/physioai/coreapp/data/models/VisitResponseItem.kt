package com.darwin.physioai.coreapp.data.models

data class VisitResponseItem(
    val appointment_detail: AppointmentDetail?,
    val created_by: Int?,
    val error: Boolean?,
    val location: String?,
    val message: String?,
    val notes: String?,
    val pp_ed_id: Int?,
    val pp_vd_id: Int?,
    val status: String?,
    val video_link: String?,
    val visit_number: Int?,
    val visit_type: String?
)