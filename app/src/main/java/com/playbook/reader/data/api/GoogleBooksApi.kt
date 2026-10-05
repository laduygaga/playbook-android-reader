package com.playbook.reader.data.api

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.playbook.reader.domain.model.Book
import com.playbook.reader.domain.model.BookFileType
import com.playbook.reader.domain.model.Chapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

data class GoogleBooksSearchResponse(
    @SerializedName("kind") val kind: String?,
    @SerializedName("totalItems") val totalItems: Int?,
    @SerializedName("items") val items: List<GoogleBookVolumeItem>?
)

data class GoogleBookVolumeItem(
    @SerializedName("id") val id: String,
    @SerializedName("volumeInfo") val volumeInfo: GoogleVolumeInfo?
)

data class GoogleVolumeInfo(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("authors") val authors: List<String>?,
    @SerializedName("publisher") val publisher: String?,
    @SerializedName("publishedDate") val publishedDate: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("pageCount") val pageCount: Int?,
    @SerializedName("categories") val categories: List<String>?,
    @SerializedName("imageLinks") val imageLinks: GoogleImageLinks?,
    @SerializedName("previewLink") val previewLink: String?,
    @SerializedName("infoLink") val infoLink: String?
)

data class GoogleImageLinks(
    @SerializedName("smallThumbnail") val smallThumbnail: String?,
    @SerializedName("thumbnail") val thumbnail: String?
)

data class GoogleBookshelfResponse(
    @SerializedName("kind") val kind: String?,
    @SerializedName("items") val items: List<GoogleBookshelfItem>?
)

data class GoogleBookshelfItem(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("volumeCount") val volumeCount: Int?,
    @SerializedName("access") val access: String?
)

object GoogleBooksApi {

    private const val BASE_URL = "https://www.googleapis.com/books/v1/volumes"
    private val gson = Gson()

    suspend fun searchVolumes(query: String): List<GoogleBookVolumeItem> = withContext(Dispatchers.IO) {
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        val urlString = "$BASE_URL?q=$encodedQuery&maxResults=20"

        return@withContext try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                val responseText = reader.readText()
                reader.close()
                val response = gson.fromJson(responseText, GoogleBooksSearchResponse::class.java)
                response.items ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getUserBookshelves(accessToken: String): List<GoogleBookshelfItem> = withContext(Dispatchers.IO) {
        val urlString = "https://www.googleapis.com/books/v1/users/me/bookshelves"
        return@withContext try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                val responseText = reader.readText()
                reader.close()
                val response = gson.fromJson(responseText, GoogleBookshelfResponse::class.java)
                response.items ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getUserBookshelfVolumes(accessToken: String, bookshelfId: Int): List<GoogleBookVolumeItem> = withContext(Dispatchers.IO) {
        val urlString = "https://www.googleapis.com/books/v1/users/me/bookshelves/$bookshelfId/volumes?maxResults=40"
        return@withContext try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                val responseText = reader.readText()
                reader.close()
                val response = gson.fromJson(responseText, GoogleBooksSearchResponse::class.java)
                response.items ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getVolumeDetails(volumeId: String): GoogleBookVolumeItem? = withContext(Dispatchers.IO) {
        val urlString = "$BASE_URL/$volumeId"
        return@withContext try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                val responseText = reader.readText()
                reader.close()
                gson.fromJson(responseText, GoogleBookVolumeItem::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun convertVolumeToBookAndChapters(item: GoogleBookVolumeItem): Pair<Book, List<Chapter>> {
        val info = item.volumeInfo
        val bookId = "google_${item.id}"
        val title = info?.title ?: "Untitled Google Book"
        val author = info?.authors?.joinToString(", ") ?: "Unknown Author"
        val coverUrl = info?.imageLinks?.thumbnail?.replace("http://", "https://")
            ?: info?.imageLinks?.smallThumbnail?.replace("http://", "https://")
        val descriptionText = info?.description ?: "Synced from Google Play Books."

        val rawParagraphs = descriptionText.replace("<p>", "").replace("</p>", "\n\n").replace("<br>", "\n")
        val chapters = mutableListOf<Chapter>()

        chapters.add(
            Chapter(
                id = "$bookId-ch-0",
                bookId = bookId,
                title = "Overview & Summary",
                content = """
                    # $title
                    By $author
                    
                    Published: ${info?.publishedDate ?: "N/A"} (${info?.publisher ?: "Google Play Books"})
                    Page Count: ${info?.pageCount ?: "N/A"} pages
                    Categories: ${info?.categories?.joinToString(", ") ?: "General"}
                    
                    ---
                    
                    $rawParagraphs
                """.trimIndent(),
                chapterIndex = 0
            )
        )

        // Generate structured readable chapters for vertical scroll engine
        val chunks = rawParagraphs.split("\n\n")
        if (chunks.size > 2) {
            chunks.chunked(3).forEachIndexed { idx, chunkList ->
                val chapterContent = chunkList.joinToString("\n\n")
                if (chapterContent.isNotBlank()) {
                    chapters.add(
                        Chapter(
                            id = "$bookId-ch-${idx + 1}",
                            bookId = bookId,
                            title = "Chapter ${idx + 1}: Excerpt & Content",
                            content = chapterContent,
                            chapterIndex = idx + 1
                        )
                    )
                }
            }
        }

        val book = Book(
            id = bookId,
            title = title,
            author = author,
            coverPath = coverUrl,
            filePath = "google://$bookId",
            fileType = BookFileType.GOOGLE_PLAY_BOOK,
            totalChapters = chapters.size,
            progress = 0f,
            googleVolumeId = item.id,
            description = info?.description,
            categories = info?.categories?.joinToString(", "),
            previewLink = info?.previewLink,
            isSyncedFromGoogle = true
        )

        return Pair(book, chapters)
    }
}
