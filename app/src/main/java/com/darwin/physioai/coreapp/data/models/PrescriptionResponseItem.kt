package com.example.physioai.data.models

data class PrescriptionResponseItem(
    val date: String?,
    val lab_tests: List<LabTest>?,
    val medication_detail: List<MedicationDetail>?,
    val notes: String?,
    val pp_ed_id: Int?,
    val pp_pres_id: Double?
)