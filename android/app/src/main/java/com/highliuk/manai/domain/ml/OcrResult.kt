package com.highliuk.manai.domain.ml

data class OcrResult(
    val text: String,
    val region: TextRegion,
)
