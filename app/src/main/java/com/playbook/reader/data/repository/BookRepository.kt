package com.playbook.reader.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.playbook.reader.data.parser.EpubParser
import com.playbook.reader.data.parser.TextParser
import com.playbook.reader.data.sample.SampleBooksProvider
import com.playbook.reader.domain.model.Book
import com.playbook.reader.domain.model.BookFileType
import com.playbook.reader.domain.model.Chapter
import com.playbook.reader.domain.model.ReaderSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class BookRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("playbook_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _readerSettings = MutableStateFlow(loadSettings())
    val readerSettings: StateFlow<ReaderSettings> = _readerSettings.asStateFlow()

    init {
        loadBooks()
    }

    private fun loadBooks() {
        val json = prefs.getString("user_books", null)
        val savedBooks: List<Book> = if (json != null) {
            val type = object : TypeToken<List<Book>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }

        // Merge sample books if not already saved
        val allBooks = (SampleBooksProvider.SAMPLE_BOOKS + savedBooks).distinctBy { it.id }
        _books.value = allBooks
        saveBooksToPrefs(savedBooks)
    }

    private fun saveBooksToPrefs(userBooks: List<Book>) {
        val json = gson.toJson(userBooks)
        prefs.edit().putString("user_books", json).apply()
    }

    fun importBook(uri: Uri, fileName: String): Book? {
        return try {
            val isEpub = fileName.lowercase().endsWith(".epub")
            val parseResult = if (isEpub) {
                EpubParser.parseEpubUri(context, uri)
            } else {
                TextParser.parseTextUri(context, uri, fileName)
            }

            // Cache chapters locally
            saveChaptersLocally(parseResult.book.id, parseResult.chapters)

            // Update books list
            val currentList = _books.value.toMutableList()
            currentList.removeAll { it.id == parseResult.book.id }
            currentList.add(0, parseResult.book)
            _books.value = currentList

            // Save to persistent preferences
            val userOnly = currentList.filterNot { it.id.startsWith("sample_") }
            saveBooksToPrefs(userOnly)

            parseResult.book
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getChaptersForBook(bookId: String): List<Chapter> {
        if (bookId.startsWith("sample_")) {
            return SampleBooksProvider.getSampleChapters(bookId)
        }

        val chapterFile = File(context.filesDir, "chapters_$bookId.json")
        if (!chapterFile.exists()) return emptyList()

        return try {
            val json = chapterFile.readText()
            val type = object : TypeToken<List<Chapter>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveChaptersLocally(bookId: String, chapters: List<Chapter>) {
        val chapterFile = File(context.filesDir, "chapters_$bookId.json")
        val json = gson.toJson(chapters)
        chapterFile.writeText(json)
    }

    fun updateReadingProgress(
        bookId: String,
        chapterIndex: Int,
        scrollOffset: Int,
        progressPercentage: Float
    ) {
        val currentList = _books.value.map { book ->
            if (book.id == bookId) {
                book.copy(
                    currentChapterIndex = chapterIndex,
                    currentScrollOffset = scrollOffset,
                    progress = progressPercentage.coerceIn(0f, 1f),
                    lastReadTimestamp = System.currentTimeMillis()
                )
            } else {
                book
            }
        }
        _books.value = currentList

        val userOnly = currentList.filterNot { it.id.startsWith("sample_") }
        saveBooksToPrefs(userOnly)
    }

    fun updateSettings(newSettings: ReaderSettings) {
        _readerSettings.value = newSettings
        val json = gson.toJson(newSettings)
        prefs.edit().putString("reader_settings", json).apply()
    }

    private fun loadSettings(): ReaderSettings {
        val json = prefs.getString("reader_settings", null) ?: return ReaderSettings()
        return try {
            gson.fromJson(json, ReaderSettings::class.java) ?: ReaderSettings()
        } catch (e: Exception) {
            ReaderSettings()
        }
    }
}
