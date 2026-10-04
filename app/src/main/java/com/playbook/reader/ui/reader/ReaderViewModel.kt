package com.playbook.reader.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.playbook.reader.data.repository.BookRepository
import com.playbook.reader.domain.model.Book
import com.playbook.reader.domain.model.Chapter
import com.playbook.reader.domain.model.ReaderSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val repository: BookRepository,
    val bookId: String
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

    init {
        loadBookData()
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
