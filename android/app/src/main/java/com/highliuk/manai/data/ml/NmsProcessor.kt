package com.highliuk.manai.data.ml

internal object NmsProcessor {

    fun computeIoU(a: FloatArray, b: FloatArray): Float {
        val x1 = maxOf(a[0], b[0])
        val y1 = maxOf(a[1], b[1])
        val x2 = minOf(a[2], b[2])
        val y2 = minOf(a[3], b[3])
        if (x2 <= x1 || y2 <= y1) return 0f
        val intersection = (x2 - x1) * (y2 - y1)
        val areaA = (a[2] - a[0]) * (a[3] - a[1])
        val areaB = (b[2] - b[0]) * (b[3] - b[1])
        return intersection / (areaA + areaB - intersection)
    }

    fun applyNms(boxes: List<FloatArray>, iouThreshold: Float): List<FloatArray> {
        if (boxes.isEmpty()) return emptyList()
        val sorted = boxes.sortedByDescending { it[4] }
        val kept = mutableListOf<FloatArray>()
        val suppressed = BooleanArray(sorted.size)

        for (i in sorted.indices) {
            if (suppressed[i]) continue
            kept.add(sorted[i])
            for (j in i + 1 until sorted.size) {
                if (!suppressed[j] && computeIoU(sorted[i], sorted[j]) > iouThreshold) {
                    suppressed[j] = true
                }
            }
        }
        return kept
    }
}
