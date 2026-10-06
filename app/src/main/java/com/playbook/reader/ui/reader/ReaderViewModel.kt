package com.playbook.reader.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.playbook.reader.data.repository.BookRepository
import com.playbook.reader.data.translation.TranslationService
import com.playbook.reader.domain.model.Book
import com.playbook.reader.domain.model.Bookmark
import com.playbook.reader.domain.model.Chapter
import com.playbook.reader.domain.model.ReaderSettings
import com.playbook.reader.domain.model.SupportedLanguage
import com.playbook.reader.domain.model.TranslationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val repository: BookRepository,
    val bookId: String,
    private val translationService: TranslationService = TranslationService()
) : ViewModel() {

    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()

    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()

    val settings: StateFlow<ReaderSettings> = repository.readerSettings

    private val _isSearchOpen = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> = _isSearchOpen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    val bookmarks: StateFlow<List<Bookmark>> = _bookmarks.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(SupportedLanguage.DEFAULT_LANGUAGES.first())
    val selectedLanguage: StateFlow<SupportedLanguage> = _selectedLanguage.asStateFlow()

    private val _isTranslationActive = MutableStateFlow(false)
    val isTranslationActive: StateFlow<Boolean> = _isTranslationActive.asStateFlow()

    private val _translationState = MutableStateFlow<TranslationState>(TranslationState.Idle)
    val translationState: StateFlow<TranslationState> = _translationState.asStateFlow()

    private val _translatedChapters = MutableStateFlow<Map<String, String>>(emptyMap())
    val translatedChapters: StateFlow<Map<String, String>> = _translatedChapters.asStateFlow()

    init {
        loadBookData()
        loadBookmarks()
    }

    private fun loadBookmarks() {
        _bookmarks.value = repository.getBookmarksForBook(bookId)
    }

    private fun loadBookData() {
        viewModelScope.launch {
            repository.books.collect { bookList ->
                val foundBook = bookList.find { it.id == bookId }
                if (foundBook != null) {
                    _book.value = foundBook
                    _chapters.value = repository.getChaptersForBook(bookId)
                }
            }
        }
    }

    fun saveProgress(chapterIndex: Int, scrollOffset: Int, progressPercentage: Float) {
        repository.updateReadingProgress(bookId, chapterIndex, scrollOffset, progressPercentage)
    }

    fun updateSettings(newSettings: ReaderSettings) {
        repository.updateSettings(newSettings)
    }

    fun toggleSearch() {
        _isSearchOpen.value = !_isSearchOpen.value
        if (!_isSearchOpen.value) {
            _searchQuery.value = ""
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleBookmark(chapterIndex: Int, chapterTitle: String, scrollOffset: Int = 0) {
        val existing = _bookmarks.value.find { it.chapterIndex == chapterIndex && kotlin.math.abs(it.scrollOffset - scrollOffset) < 100 }
        if (existing != null) {
            _bookmarks.value = repository.removeBookmark(bookId, existing.id)
        } else {
            val newBookmark = Bookmark(
                bookId = bookId,
                chapterIndex = chapterIndex,
                chapterTitle = chapterTitle,
                scrollOffset = scrollOffset
            )
            _bookmarks.value = repository.addBookmark(newBookmark)
        }
    }

    fun removeBookmark(bookmarkId: String) {
        _bookmarks.value = repository.removeBookmark(bookId, bookmarkId)
    }

    fun setSelectedLanguage(language: SupportedLanguage) {
        _selectedLanguage.value = language
    }

    fun translateCurrentChapter(chapter: Chapter, language: SupportedLanguage = selectedLanguage.value) {
        _selectedLanguage.value = language
        val cacheKey = "${chapter.id}-${language.code}"

        val cachedTranslation = _translatedChapters.value[cacheKey]
        if (cachedTranslation != null) {
            _translationState.value = TranslationState.Success(
                chapterId = chapter.id,
                translatedText = cachedTranslation,
                targetLanguage = language
            )
            _isTranslationActive.value = true
            return
        }

        viewModelScope.launch {
            _translationState.value = TranslationState.Loading
            val result = translationService.translateText(
                chapterId = chapter.id,
                text = chapter.content,
                targetLanguage = language
            )

            result.fold(
                onSuccess = { translatedText ->
                    val updatedMap = _translatedChapters.value.toMutableMap()
                    updatedMap[cacheKey] = translatedText
                    _translatedChapters.value = updatedMap

                    _translationState.value = TranslationState.Success(
                        chapterId = chapter.id,
                        translatedText = translatedText,
                        targetLanguage = language
                    )
                    _isTranslationActive.value = true
                },
                onFailure = { error ->
                    _translationState.value = TranslationState.Error(
                        error.message ?: "Failed to translate page"
                    )
                }
            )
        }
    }

    fun toggleTranslationActive() {
        _isTranslationActive.value = !_isTranslationActive.value
    }

    fun showOriginalText() {
        _isTranslationActive.value = false
    }

    fun showTranslatedText() {
        if (_translationState.value is TranslationState.Success) {
            _isTranslationActive.value = true
        }
    }

    class Factory(
        private val repository: BookRepository,
        private val bookId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReaderViewModel(repository, bookId) as T
        }
    }
}
