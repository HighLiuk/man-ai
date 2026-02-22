package com.highliuk.manai.data.ml

import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class OnnxSessionManagerInstrumentedTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var sessionManager: OnnxSessionManager

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun detectorSessionLoadsWithoutCrash() {
        val session = sessionManager.detectorSession
        assertNotNull(session)
        assertTrue(session.inputNames.isNotEmpty())
    }

    @Test
    fun encoderSessionLoadsWithoutCrash() {
        val session = sessionManager.encoderSession
        assertNotNull(session)
        assertTrue(session.inputNames.isNotEmpty())
    }

    @Test
    fun decoderFirstSessionLoadsWithoutCrash() {
        val session = sessionManager.decoderFirstSession
        assertNotNull(session)
        assertTrue(session.inputNames.isNotEmpty())
    }

    @Test
    fun decoderWithPastSessionLoadsWithoutCrash() {
        val session = sessionManager.decoderWithPastSession
        assertNotNull(session)
        assertTrue(session.inputNames.isNotEmpty())
    }

    @Test
    fun vocabLoadsWithExpectedSize() {
        val vocab = sessionManager.vocab
        assertTrue("Vocab should have entries", vocab.isNotEmpty())
        assertTrue("Vocab should have at least special tokens", vocab.size > 5)
    }

    @Test
    fun getModelPathReturnsCorrectPath() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expected = "${context.filesDir.absolutePath}/models/encoder.onnx"
        val actual = sessionManager.getModelPath("encoder.onnx")
        assertTrue("Path should end with models/encoder.onnx", actual == expected)
    }
}
