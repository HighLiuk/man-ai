package com.highliuk.manai.data.ml

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NmsProcessorTest {

    @Test
    fun `computeIoU returns 0 for non-overlapping boxes`() {
        val a = floatArrayOf(0f, 0f, 10f, 10f)
        val b = floatArrayOf(20f, 20f, 30f, 30f)
        assertEquals(0f, NmsProcessor.computeIoU(a, b), 1e-6f)
    }

    @Test
    fun `computeIoU returns 1 for identical boxes`() {
        val a = floatArrayOf(0f, 0f, 10f, 10f)
        assertEquals(1f, NmsProcessor.computeIoU(a, a), 1e-6f)
    }

    @Test
    fun `computeIoU returns correct value for partial overlap`() {
        // Box A: (0,0)-(10,10) area=100
        // Box B: (5,5)-(15,15) area=100
        // Intersection: (5,5)-(10,10) area=25
        // Union: 100+100-25=175
        // IoU: 25/175 = 1/7
        val a = floatArrayOf(0f, 0f, 10f, 10f)
        val b = floatArrayOf(5f, 5f, 15f, 15f)
        assertEquals(1f / 7f, NmsProcessor.computeIoU(a, b), 1e-6f)
    }

    @Test
    fun `applyNms returns empty list for empty input`() {
        val result = NmsProcessor.applyNms(emptyList(), iouThreshold = 0.5f)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `applyNms keeps single box unchanged`() {
        val box = floatArrayOf(0f, 0f, 10f, 10f, 0.9f)
        val result = NmsProcessor.applyNms(listOf(box), iouThreshold = 0.5f)
        assertEquals(1, result.size)
    }

    @Test
    fun `applyNms suppresses overlapping box with lower score`() {
        val high = floatArrayOf(0f, 0f, 10f, 10f, 0.9f)
        val low = floatArrayOf(1f, 1f, 11f, 11f, 0.5f)
        val result = NmsProcessor.applyNms(listOf(low, high), iouThreshold = 0.5f)
        assertEquals(1, result.size)
        assertEquals(0.9f, result[0][4], 1e-6f)
    }

    @Test
    fun `applyNms keeps non-overlapping boxes`() {
        val a = floatArrayOf(0f, 0f, 10f, 10f, 0.9f)
        val b = floatArrayOf(50f, 50f, 60f, 60f, 0.8f)
        val result = NmsProcessor.applyNms(listOf(a, b), iouThreshold = 0.5f)
        assertEquals(2, result.size)
    }
}
