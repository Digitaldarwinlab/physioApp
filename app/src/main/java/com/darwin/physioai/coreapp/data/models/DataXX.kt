package com.example.physioai.data.models

data class DataXX(
    val Rep: RepX,
    val Rom: RomX,
    val angle_data: List<AngleData>,
    val ex_em_id: Int,
    val image_url: String,
    val name: String,
    val video_url: String
)