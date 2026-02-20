package com.highliuk.manai.domain.ml

import org.junit.Assert.assertEquals
import org.junit.Test

class TextRegionTest {

    @Test
    fun `TextRegion stores coordinates and confidence`() {
        val region = TextRegion(
            x1 = 10f, y1 = 20f,
            x2 = 100f, y2 = 200f,
            confidence = 0.95f
        )
        assertEquals(10f, region.x1, 1e-6f)
        assertEquals(20f, region.y1, 1e-6f)
        assertEquals(100f, region.x2, 1e-6f)
        assertEquals(200f, region.y2, 1e-6f)
        assertEquals(0.95f, region.confidence, 1e-6f)
    }

    @Test
    fun `OcrResult stores text and region`() {
        val region = TextRegion(0f, 0f, 50f, 50f, 0.9f)
        val result = OcrResult(text = "こんにちは", region = region)
        assertEquals("こんにちは", result.text)
        assertEquals(region, result.region)
    }
}
