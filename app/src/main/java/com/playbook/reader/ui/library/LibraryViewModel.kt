package com.playbook.reader.ui.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.playbook.reader.data.api.GoogleBookVolumeItem
import com.playbook.reader.data.api.GoogleBooksApi
import com.playbook.reader.data.repository.BookRepository
import com.playbook.reader.domain.model.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibraryViewModel(private val repository: BookRepository) : ViewModel() {

    val books: StateFlow<List<Book>> = repository.books

    private val _isGridView = MutableStateFlow(true)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _googleSearchResults = MutableStateFlow<List<GoogleBookVolumeItem>>(emptyList())
    val googleSearchResults: StateFlow<List<GoogleBookVolumeItem>> = _googleSearchResults.asStateFlow()

    private val _isSearchingGoogle = MutableStateFlow(false)
    val isSearchingGoogle: StateFlow<Boolean> = _isSearchingGoogle.asStateFlow()

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchGoogleBooks(query: String) {
        if (query.isBlank()) {
            _googleSearchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearchingGoogle.value = true
            val results = GoogleBooksApi.searchVolumes(query)
            _googleSearchResults.value = results
            _isSearchingGoogle.value = false
        }
    }

    fun syncGoogleBook(volumeItem: GoogleBookVolumeItem): Book {
        return repository.addGoogleBookToLibrary(volumeItem)
    }

    fun importBook(uri: Uri, fileName: String): Book? {
        return repository.importBook(uri, fileName)
    }

    class Factory(private val repository: BookRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LibraryViewModel(repository) as T
        }
    }
}
