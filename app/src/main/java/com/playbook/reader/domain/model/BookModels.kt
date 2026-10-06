package com.playbook.reader.domain.model

import androidx.compose.ui.graphics.Color

enum class BookFileType {
    EPUB,
    TXT,
    PDF
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
    val description: String? = null,
    val categories: String? = null
)

data class Chapter(
    val id: String,
    val bookId: String,
    val title: String,
    val content: String,
    val chapterIndex: Int,
    val href: String? = null
)

data class Bookmark(
    val id: String = java.util.UUID.randomUUID().toString(),
    val bookId: String,
    val chapterIndex: Int,
    val chapterTitle: String,
    val scrollOffset: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)

enum class ReadingMode {
    VERTICAL_SCROLL,
    HORIZONTAL_PAGED
}

enum class FontStyleOption(val displayName: String) {
    SANS_SERIF("San Francisco"),
    SERIF("New York Serif"),
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
            background = Color(0xFFF2F2F7),
            surface = Color(0xFFFFFFFF),
            text = Color(0xFF000000),
            secondaryText = Color(0xFF8E8E93),
            accent = Color(0xFFFA2D48) // Apple Books Accent
        )

        val SEPIA = ReaderThemeColors(
            name = "Sepia",
            background = Color(0xFFFAF4E8),
            surface = Color(0xFFF3EAD8),
            text = Color(0xFF3C3122),
            secondaryText = Color(0xFF8E7D6B),
            accent = Color(0xFFD93B2B)
        )

        val DARK = ReaderThemeColors(
            name = "Dark",
            background = Color(0xFF1C1C1E),
            surface = Color(0xFF2C2C2E),
            text = Color(0xFFFFFFFF),
            secondaryText = Color(0xFF8E8E93),
            accent = Color(0xFFFA2D48)
        )

        val NIGHT = ReaderThemeColors(
            name = "Amoled Night",
            background = Color(0xFF000000),
            surface = Color(0xFF1C1C1E),
            text = Color(0xFFE5E5EA),
            secondaryText = Color(0xFF8E8E93),
            accent = Color(0xFFFF3B30)
        )
    }
}

data class ReaderSettings(
    val fontSizeSp: Float = 18f,
    val lineSpacingMultiplier: Float = 1.4f,
    val fontStyle: FontStyleOption = FontStyleOption.SERIF,
    val readingMode: ReadingMode = ReadingMode.HORIZONTAL_PAGED,
    val themeName: String = "Light",
    val keepScreenOn: Boolean = true,
    val textMarginDp: Int = 20
)
