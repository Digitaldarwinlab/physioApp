package com.example.physioai.data.models

data class HomeResponseItem(
    val PP_Patient_Details: String,
    val PP_Patient_Details_mobile: PPPatientDetailsMobile,
    val end_date: String,
    val episode_number: String,
    val pp_ed_id: Double,
    val pp_pm_id: Double,
    val treating_doc_details: String,
    val treating_doc_details_mobile: TreatingDocDetailsMobile,
    val treating_doctor_detail: List<TreatingDoctorDetail>
)