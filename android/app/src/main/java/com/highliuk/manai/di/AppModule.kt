package com.highliuk.manai.di

import android.content.ContentResolver
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.highliuk.manai.data.pdf.PdfDocumentHandlerImpl
import com.highliuk.manai.data.repository.MangaRepositoryImpl
import com.highliuk.manai.data.settings.SettingsRepositoryImpl
import com.highliuk.manai.domain.repository.MangaRepository
import com.highliuk.manai.domain.repository.PdfDocumentHandler
import com.highliuk.manai.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindMangaRepository(impl: MangaRepositoryImpl): MangaRepository

    @Binds
    @Singleton
    abstract fun bindPdfDocumentHandler(impl: PdfDocumentHandlerImpl): PdfDocumentHandler

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    companion object {
        @Provides
        @Singleton
        fun provideContentResolver(@ApplicationContext context: Context): ContentResolver =
            context.contentResolver

        @Provides
        @Singleton
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            context.dataStore
    }
}
