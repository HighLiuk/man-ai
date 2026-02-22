package com.highliuk.manai.domain.ml

import android.graphics.Bitmap

interface TextDetector {
    suspend fun detect(bitmap: Bitmap): List<TextRegion>
}
