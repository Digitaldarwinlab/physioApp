package com.darwin.physioai.coreapp.data.models

data class VisitResponse(
    val `data`: List<Data>,
    val error: Boolean,
    val message: String
)