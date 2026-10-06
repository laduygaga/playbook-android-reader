package com.playbook.reader.domain.model

data class SupportedLanguage(
    val code: String,
    val name: String,
    val flagEmoji: String
) {
    companion object {
        val DEFAULT_LANGUAGES = listOf(
            SupportedLanguage("es", "Spanish", "🇪🇸"),
            SupportedLanguage("fr", "French", "🇫🇷"),
            SupportedLanguage("de", "German", "🇩🇪"),
            SupportedLanguage("vi", "Vietnamese", "🇻🇳"),
            SupportedLanguage("ja", "Japanese", "🇯🇵"),
            SupportedLanguage("zh-CN", "Chinese", "🇨🇳"),
            SupportedLanguage("ko", "Korean", "🇰🇷"),
            SupportedLanguage("it", "Italian", "🇮🇹"),
            SupportedLanguage("pt", "Portuguese", "🇵🇹"),
            SupportedLanguage("ru", "Russian", "🇷🇺"),
            SupportedLanguage("hi", "Hindi", "🇮🇳"),
            SupportedLanguage("en", "English", "🇺🇸")
        )
    }
}

sealed class TranslationState {
    object Idle : TranslationState()
    object Loading : TranslationState()
    data class Success(
        val chapterId: String,
        val translatedText: String,
        val targetLanguage: SupportedLanguage
    ) : TranslationState()
    data class Error(val message: String) : TranslationState()
}
