package com.playbook.reader.ui.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.playbook.reader.data.repository.BookRepository
import com.playbook.reader.domain.model.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LibraryViewModel(private val repository: BookRepository) : ViewModel() {

    val books: StateFlow<List<Book>> = repository.books

    private val _isGridView = MutableStateFlow(true)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun importBook(uri: Uri, fileName: String): Book? {
        return repository.importBook(uri, fileName)
    }

    fun removeBook(bookId: String) {
        repository.removeBook(bookId)
    }

    class Factory(private val repository: BookRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LibraryViewModel(repository) as T
        }
    }
}
