package com.highliuk.manai.data.ml

import android.content.Context

class OnnxSessionManager(private val context: Context) {

    private val filesDir: String = context.filesDir.absolutePath

    fun getModelPath(modelName: String): String {
        return "$filesDir/models/$modelName"
    }
}
