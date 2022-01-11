package com.darwin.physioai.coreapp.data.models

data class DataX(
    val Rep: Rep,
    val Rom: Rom,
    val angle: List<Angle>,
    val ex_em_id: Int,
    val image_url: String,
    val name: String,
    val pp_cp_id: Int,
    val time_gap: String,
    val video_url: String
)