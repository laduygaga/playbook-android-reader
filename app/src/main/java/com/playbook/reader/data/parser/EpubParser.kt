package com.playbook.reader.data.parser

import android.content.Context
import android.net.Uri
import com.playbook.reader.domain.model.Book
import com.playbook.reader.domain.model.BookFileType
import com.playbook.reader.domain.model.Chapter
import org.jsoup.Jsoup
import java.io.File
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object EpubParser {

    data class ParsedBookResult(
        val book: Book,
        val chapters: List<Chapter>
    )

    fun parseEpubUri(context: Context, uri: Uri): ParsedBookResult {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open stream for URI: $uri")

        val bookId = UUID.randomUUID().toString()
        val tempDir = File(context.cacheDir, "epub_$bookId")
        tempDir.mkdirs()

        // Unzip EPUB contents into temp folder
        extractZip(inputStream, tempDir)

        val containerFile = File(tempDir, "META-INF/container.xml")
        var opfPath = ""
        if (containerFile.exists()) {
            val doc = Jsoup.parse(containerFile, "UTF-8")
            val rootFile = doc.select("rootfile").first()
            opfPath = rootFile?.attr("full-path") ?: ""
        }

        val opfFile = if (opfPath.isNotEmpty()) File(tempDir, opfPath) else findOpfFile(tempDir)
            ?: throw IllegalStateException("Could not find OPF manifest file in EPUB")

        val opfDoc = Jsoup.parse(opfFile, "UTF-8")
        val title = opfDoc.select("dc\\:title, title").first()?.text() ?: "Untitled Book"
        val author = opfDoc.select("dc\\:creator, creator").first()?.text() ?: "Unknown Author"

        // Extract manifest item hrefs mapped by id
        val manifestMap = mutableMapOf<String, String>()
        opfDoc.select("manifest > item").forEach { item ->
            val id = item.attr("id")
            val href = item.attr("href")
            manifestMap[id] = href
        }

        // Spine order
        val spineItems = opfDoc.select("spine > itemref").mapNotNull { itemref ->
            val idref = itemref.attr("idref")
            manifestMap[idref]
        }

        val opfParent = opfFile.parentFile ?: tempDir
        val chapters = mutableListOf<Chapter>()

        spineItems.forEachIndexed { index, relativePath ->
            val htmlFile = File(opfParent, relativePath)
            if (htmlFile.exists()) {
                val chapterDoc = Jsoup.parse(htmlFile, "UTF-8")
                
                // Get chapter title from h1, h2, title, or fallback
                val chapterTitle = chapterDoc.select("h1, h2, h3, title").first()?.text()
                    ?: "Chapter ${index + 1}"

                // Format body text with paragraphs
                val bodyText = buildFormattedChapterText(chapterDoc)

                if (bodyText.isNotBlank()) {
                    chapters.add(
                        Chapter(
                            id = "$bookId-ch-$index",
                            bookId = bookId,
                            title = chapterTitle,
                            content = bodyText,
                            chapterIndex = index,
                            href = relativePath
                        )
                    )
                }
            }
        }

        val book = Book(
            id = bookId,
            title = title,
            author = author,
            coverPath = null,
            filePath = uri.toString(),
            fileType = BookFileType.EPUB,
            totalChapters = chapters.size.coerceAtLeast(1),
            progress = 0f
        )

        return ParsedBookResult(book, chapters)
    }

    private fun buildFormattedChapterText(doc: org.jsoup.nodes.Document): String {
        val elements = doc.select("p, h1, h2, h3, h4, h5, h6, li, blockquote")
        if (elements.isEmpty()) {
            return doc.body()?.text() ?: ""
        }
        val sb = StringBuilder()
        elements.forEach { el ->
            val text = el.text().trim()
            if (text.isNotEmpty()) {
                val tag = el.tagName().lowercase()
                if (tag.startsWith("h")) {
                    sb.append("\n\n### ").append(text).append("\n\n")
                } else if (tag == "li") {
                    sb.append("• ").append(text).append("\n")
                } else {
                    sb.append(text).append("\n\n")
                }
            }
        }
        return sb.toString().trim()
    }

    private fun extractZip(inputStream: InputStream, targetDir: File) {
        ZipInputStream(inputStream).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val newFile = File(targetDir, entry.name)
                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    newFile.outputStream().use { fos ->
                        zis.copyTo(fos)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    private fun findOpfFile(dir: File): File? {
        return dir.walkTopDown().firstOrNull { it.extension.lowercase() == "opf" }
    }
}
