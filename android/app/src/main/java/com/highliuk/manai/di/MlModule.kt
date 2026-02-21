package com.highliuk.manai.di

import android.content.Context
import com.highliuk.manai.data.ml.OnnxSessionManager
import com.highliuk.manai.data.ml.OnnxTextDetector
import com.highliuk.manai.data.ml.OnnxTextRecognizer
import com.highliuk.manai.domain.ml.TextDetector
import com.highliuk.manai.domain.ml.TextRecognizer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MlModule {

    @Binds
    @Singleton
    abstract fun bindTextDetector(impl: OnnxTextDetector): TextDetector

    @Binds
    @Singleton
    abstract fun bindTextRecognizer(impl: OnnxTextRecognizer): TextRecognizer

    companion object {
        @Provides
        @Singleton
        fun provideOnnxSessionManager(
            @ApplicationContext context: Context,
        ): OnnxSessionManager = OnnxSessionManager(context)
    }
}
