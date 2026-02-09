package com.highliuk.manai.domain.model

enum class ReadingMode { SINGLE_PAGE, DOUBLE_PAGE, LONG_STRIP }

enum class ReadingDirection { LTR, RTL }

data class ReaderSettings(
    val readingMode: ReadingMode = ReadingMode.SINGLE_PAGE,
    val readingDirection: ReadingDirection = ReadingDirection.RTL,
    val tapNavigationEnabled: Boolean = true,
    val coverAlone: Boolean = true,
)
