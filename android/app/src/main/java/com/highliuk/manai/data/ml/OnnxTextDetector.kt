package com.highliuk.manai.data.ml

import android.graphics.Bitmap
import com.highliuk.manai.domain.ml.TextDetector
import com.highliuk.manai.domain.ml.TextRegion
import javax.inject.Inject
import kotlin.math.min

@Suppress("UnusedPrivateProperty") // sessionManager used in Macro Task 5 inference
class OnnxTextDetector @Inject constructor(
    private val sessionManager: OnnxSessionManager,
) : TextDetector {

    override suspend fun detect(bitmap: Bitmap): List<TextRegion> {
        TODO("Inference will be implemented in Macro Task 5")
    }

    data class LetterboxParams(
        val origWidth: Int,
        val origHeight: Int,
        val scale: Float,
        val newWidth: Int,
        val newHeight: Int,
        val padLeft: Int,
        val padTop: Int,
    )

    companion object {
        fun computeLetterboxParams(
            origWidth: Int,
            origHeight: Int,
            inputSize: Int,
        ): LetterboxParams {
            val scale = min(inputSize.toFloat() / origWidth, inputSize.toFloat() / origHeight)
            val newWidth = (origWidth * scale).toInt()
            val newHeight = (origHeight * scale).toInt()
            val padLeft = (inputSize - newWidth) / 2
            val padTop = (inputSize - newHeight) / 2
            return LetterboxParams(origWidth, origHeight, scale, newWidth, newHeight, padLeft, padTop)
        }

        fun postprocessYoloOutput(
            output: Array<FloatArray>,
            letterbox: LetterboxParams,
            confThreshold: Float,
            iouThreshold: Float,
        ): List<TextRegion> {
            val numPredictions = output[0].size
            val proposals = mutableListOf<FloatArray>()
            val invScale = 1f / letterbox.scale

            for (i in 0 until numPredictions) {
                val score = output[4][i]
                if (score > confThreshold) {
                    val xCenter = output[0][i]
                    val yCenter = output[1][i]
                    val width = output[2][i]
                    val height = output[3][i]

                    var x1 = (xCenter - width / 2 - letterbox.padLeft) * invScale
                    var y1 = (yCenter - height / 2 - letterbox.padTop) * invScale
                    var x2 = (xCenter + width / 2 - letterbox.padLeft) * invScale
                    var y2 = (yCenter + height / 2 - letterbox.padTop) * invScale

                    x1 = x1.coerceIn(0f, letterbox.origWidth.toFloat())
                    y1 = y1.coerceIn(0f, letterbox.origHeight.toFloat())
                    x2 = x2.coerceIn(0f, letterbox.origWidth.toFloat())
                    y2 = y2.coerceIn(0f, letterbox.origHeight.toFloat())

                    proposals.add(floatArrayOf(x1, y1, x2, y2, score))
                }
            }

            return NmsProcessor.applyNms(proposals, iouThreshold).map { box ->
                TextRegion(
                    x1 = box[0],
                    y1 = box[1],
                    x2 = box[2],
                    y2 = box[3],
                    confidence = box[4],
                )
            }
        }
    }
}
