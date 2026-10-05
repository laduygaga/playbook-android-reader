package com.playbook.reader.domain.model

import androidx.compose.ui.graphics.Color

enum class BookFileType {
    EPUB,
    TXT,
    PDF,
    GOOGLE_PLAY_BOOK
}

data class Book(
    val id: String,
    val title: String,
    val author: String = "Unknown Author",
    val coverPath: String? = null,
    val filePath: String,
    val fileType: BookFileType = BookFileType.EPUB,
    val totalChapters: Int = 1,
    val progress: Float = 0f,
    val lastReadTimestamp: Long = System.currentTimeMillis(),
    val currentChapterIndex: Int = 0,
    val currentScrollOffset: Int = 0,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val googleVolumeId: String? = null,
    val description: String? = null,
    val categories: String? = null,
    val previewLink: String? = null,
    val isSyncedFromGoogle: Boolean = false
)

data class Chapter(
    val id: String,
    val bookId: String,
    val title: String,
    val content: String,
    val chapterIndex: Int,
    val href: String? = null
)

enum class ReadingMode {
    VERTICAL_SCROLL, // User's requested Google Play Books vertical scrolling mode
    HORIZONTAL_PAGED  // Classic Google Play Books horizontal paging mode
}

enum class FontStyleOption(val displayName: String) {
    SANS_SERIF("Sans Serif"),
    SERIF("Serif"),
    MONOSPACE("Monospace")
}

data class ReaderThemeColors(
    val name: String,
    val background: Color,
    val surface: Color,
    val text: Color,
    val secondaryText: Color,
    val accent: Color
) {
    companion object {
        val LIGHT = ReaderThemeColors(
            name = "Light",
            background = Color(0xFFFAFAFA),
            surface = Color(0xFFFFFFFF),
            text = Color(0xFF212121),
            secondaryText = Color(0xFF757575),
            accent = Color(0xFF1A73E8) // Google Blue
        )

        val SEPIA = ReaderThemeColors(
            name = "Sepia",
            background = Color(0xFFFBF0D9),
            surface = Color(0xFFF4E8C1),
            text = Color(0xFF5F4B32),
            secondaryText = Color(0xFF8C7355),
            accent = Color(0xFF8D5B28)
        )

        val DARK = ReaderThemeColors(
            name = "Dark",
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            text = Color(0xFFE0E0E0),
            secondaryText = Color(0xFFA0A0A0),
            accent = Color(0xFF8AB4F8) // Light Google Blue
        )

        val NIGHT = ReaderThemeColors(
            name = "Amoled Night",
            background = Color(0xFF000000),
            surface = Color(0xFF121212),
            text = Color(0xFFD4D4D4),
            secondaryText = Color(0xFF808080),
            accent = Color(0xFF669DF6)
        )
    }
}

data class ReaderSettings(
    val fontSizeSp: Float = 18f,
    val lineSpacingMultiplier: Float = 1.4f,
    val fontStyle: FontStyleOption = FontStyleOption.SERIF,
    val readingMode: ReadingMode = ReadingMode.VERTICAL_SCROLL,
    val themeName: String = "Light",
    val keepScreenOn: Boolean = true,
    val textMarginDp: Int = 16
)
