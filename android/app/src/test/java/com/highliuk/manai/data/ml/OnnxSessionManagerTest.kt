package com.highliuk.manai.data.ml

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class OnnxSessionManagerTest {

    private val context = mockk<Context>().also {
        every { it.filesDir } returns File("/data/app")
    }

    @Test
    fun `getModelPath returns correct path in models directory`() {
        val manager = OnnxSessionManager(context)

        assertEquals("/data/app/models/encoder.onnx", manager.getModelPath("encoder.onnx"))
    }

    @Test
    fun `getModelPath works for different model names`() {
        val manager = OnnxSessionManager(context)

        assertEquals("/data/app/models/decoder_first.onnx", manager.getModelPath("decoder_first.onnx"))
    }
}
