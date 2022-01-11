package com.example.physioai.data.models

import com.darwin.physioai.coreapp.data.models.DataX

data class DataLine(
    val color: String?,
    val `data`: List<DataX>?,
    val id: String?
)