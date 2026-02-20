package com.highliuk.manai.data.ml

import org.junit.Assert.assertEquals
import org.junit.Test

class OnnxTextRecognizerTest {

    @Test
    fun `preprocessOcrPixels normalizes pixel values correctly`() {
        // Pure red pixel: 0xFFFF0000 → R=255, G=0, B=0
        // Normalization: (x/255 - 0.5) / 0.5
        // R: (1.0 - 0.5)/0.5 = 1.0, G: (0.0 - 0.5)/0.5 = -1.0, B: -1.0
        val pixels = intArrayOf(0xFFFF0000.toInt())
        val width = 1
        val height = 1

        val result = OnnxTextRecognizer.preprocessOcrPixels(pixels, width, height)

        assertEquals(3, result.size) // 3 channels * 1 pixel
        assertEquals(1.0f, result[0], 1e-4f)  // R
        assertEquals(-1.0f, result[1], 1e-4f) // G
        assertEquals(-1.0f, result[2], 1e-4f) // B
    }

    @Test
    fun `preprocessOcrPixels produces CHW layout`() {
        // 2x2 image: red, green, blue, white
        val pixels = intArrayOf(
            0xFFFF0000.toInt(), // red
            0xFF00FF00.toInt(), // green
            0xFF0000FF.toInt(), // blue
            0xFFFFFFFF.toInt(), // white
        )
        val width = 2
        val height = 2

        val result = OnnxTextRecognizer.preprocessOcrPixels(pixels, width, height)

        assertEquals(12, result.size) // 3 channels * 4 pixels
        // First 4 floats = R channel (all 4 pixels)
        assertEquals(1.0f, result[0], 1e-4f)   // red pixel R
        assertEquals(-1.0f, result[1], 1e-4f)  // green pixel R
        assertEquals(-1.0f, result[2], 1e-4f)  // blue pixel R
        assertEquals(1.0f, result[3], 1e-4f)   // white pixel R
        // Next 4 floats = G channel
        assertEquals(-1.0f, result[4], 1e-4f)  // red pixel G
        assertEquals(1.0f, result[5], 1e-4f)   // green pixel G
        assertEquals(-1.0f, result[6], 1e-4f)  // blue pixel G
        assertEquals(1.0f, result[7], 1e-4f)   // white pixel G
        // Last 4 floats = B channel
        assertEquals(-1.0f, result[8], 1e-4f)  // red pixel B
        assertEquals(-1.0f, result[9], 1e-4f)  // green pixel B
        assertEquals(1.0f, result[10], 1e-4f)  // blue pixel B
        assertEquals(1.0f, result[11], 1e-4f)  // white pixel B
    }

    @Test
    fun `decodeTokens filters special tokens and maps via vocab`() {
        // Vocab: indices 0-4 are special tokens, 5+ are real
        val vocab = listOf("PAD", "UNK", "BOS", "EOS", "SEP", "A", "B", "C", "D", "E", "F")
        // tokens [2, 5, 10, 3] → skip 2 (BOS, <5), keep 5 (A), keep 10 (F), skip 3 (EOS, <5)
        val tokens = listOf(2, 5, 10, 3)

        val result = OnnxTextRecognizer.decodeTokens(tokens, vocab)

        assertEquals("AF", result)
    }

    @Test
    fun `decodeTokens returns empty string for only special tokens`() {
        val vocab = listOf("PAD", "UNK", "BOS", "EOS", "SEP", "A")
        val tokens = listOf(2, 3) // BOS, EOS — both < 5

        val result = OnnxTextRecognizer.decodeTokens(tokens, vocab)

        assertEquals("", result)
    }

    @Test
    fun `greedyDecode stops at EOS token`() {
        // vocab: 0-4 special, 5=A, 6=B
        val vocab = listOf("PAD", "UNK", "BOS", "EOS", "SEP", "A", "B")
        var stepCount = 0
        // Mock decoder: step 0 → token 5 (A), step 1 → token 6 (B), step 2 → token 3 (EOS)
        val mockDecoder: (Int, Int) -> FloatArray = { _, step ->
            stepCount++
            when (step) {
                0 -> logitsForToken(5, vocabSize = 7)
                1 -> logitsForToken(6, vocabSize = 7)
                else -> logitsForToken(3, vocabSize = 7)
            }
        }

        val result = OnnxTextRecognizer.greedyDecode(mockDecoder, vocab)

        assertEquals("AB", result)
        assertEquals(3, stepCount) // stopped at step 2 (EOS), not 40
    }

    @Test
    fun `greedyDecode stops at max length`() {
        // vocab: 0-4 special, 5=X
        val vocab = listOf("PAD", "UNK", "BOS", "EOS", "SEP", "X")
        var stepCount = 0
        // Mock decoder: always returns token 5 (X), never EOS
        val mockDecoder: (Int, Int) -> FloatArray = { _, _ ->
            stepCount++
            logitsForToken(5, vocabSize = 6)
        }

        val result = OnnxTextRecognizer.greedyDecode(mockDecoder, vocab)

        // Should stop at 40 tokens max
        assertEquals(40, stepCount)
        assertEquals("X".repeat(40), result)
    }

    @Test
    fun `greedyDecode feeds previous token to next step`() {
        // vocab: 0-4 special, 5=A, 6=B
        val vocab = listOf("PAD", "UNK", "BOS", "EOS", "SEP", "A", "B")
        val receivedTokenIds = mutableListOf<Int>()
        // Mock: records tokenId received, then:
        //   step 0 (receives BOS=2) → produces token 5 (A)
        //   step 1 (receives 5)     → produces token 6 (B)
        //   step 2 (receives 6)     → produces EOS(3)
        val mockDecoder: (Int, Int) -> FloatArray = { tokenId, step ->
            receivedTokenIds.add(tokenId)
            when (step) {
                0 -> logitsForToken(5, vocabSize = 7)
                1 -> logitsForToken(6, vocabSize = 7)
                else -> logitsForToken(3, vocabSize = 7)
            }
        }

        OnnxTextRecognizer.greedyDecode(mockDecoder, vocab)

        assertEquals(listOf(2, 5, 6), receivedTokenIds)
    }

    private fun logitsForToken(targetToken: Int, vocabSize: Int): FloatArray {
        return FloatArray(vocabSize) { if (it == targetToken) 10.0f else -10.0f }
    }
}
