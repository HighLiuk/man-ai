package com.highliuk.manai.data.ml

import android.graphics.Bitmap
import com.highliuk.manai.domain.ml.OcrResult
import com.highliuk.manai.domain.ml.TextRecognizer
import com.highliuk.manai.domain.ml.TextRegion
import javax.inject.Inject

@Suppress("UnusedPrivateProperty") // sessionManager used in Macro Task 5 inference
class OnnxTextRecognizer @Inject constructor(
    private val sessionManager: OnnxSessionManager,
) : TextRecognizer {

    override suspend fun recognize(bitmap: Bitmap, region: TextRegion): OcrResult {
        TODO("Inference will be implemented in Macro Task 5")
    }

    companion object {
        private const val BOS_TOKEN = 2
        private const val EOS_TOKEN = 3
        private const val FIRST_REAL_TOKEN = 5
        private const val MAX_DECODE_LENGTH = 40

        fun preprocessOcrPixels(pixels: IntArray, width: Int, height: Int): FloatArray {
            val planeSize = width * height
            val floats = FloatArray(3 * planeSize)

            for (i in pixels.indices) {
                val p = pixels[i]
                val r = (p shr 16 and 0xFF) / 255f
                val g = (p shr 8 and 0xFF) / 255f
                val b = (p and 0xFF) / 255f
                floats[i] = (r - 0.5f) / 0.5f
                floats[planeSize + i] = (g - 0.5f) / 0.5f
                floats[2 * planeSize + i] = (b - 0.5f) / 0.5f
            }

            return floats
        }

        fun decodeTokens(tokens: List<Int>, vocab: List<String>): String {
            return tokens
                .filter { it >= FIRST_REAL_TOKEN }
                .map { vocab.getOrElse(it) { "" } }
                .joinToString("")
        }

        fun greedyDecode(
            decoderStep: (tokenId: Int, step: Int) -> FloatArray,
            vocab: List<String>,
        ): String {
            val tokens = mutableListOf<Int>()
            var tokenId = BOS_TOKEN

            for (step in 0 until MAX_DECODE_LENGTH) {
                val logits = decoderStep(tokenId, step)
                tokenId = logits.indices.maxBy { logits[it] }
                if (tokenId == EOS_TOKEN) break
                tokens.add(tokenId)
            }

            return decodeTokens(tokens, vocab)
        }
    }
}
