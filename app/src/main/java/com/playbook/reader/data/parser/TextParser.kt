package com.playbook.reader.data.parser

import android.content.Context
import android.net.Uri
import com.playbook.reader.domain.model.Book
import com.playbook.reader.domain.model.BookFileType
import com.playbook.reader.domain.model.Chapter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

object TextParser {

    fun parseTextUri(context: Context, uri: Uri, fileName: String): EpubParser.ParsedBookResult {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open stream for URI: $uri")

        val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
        val fullContent = reader.readText()
        reader.close()

        val bookId = UUID.randomUUID().toString()
        val title = fileName.removeSuffix(".txt").replace("_", " ").replace("-", " ")

        // Detect chapter breaks (e.g., "CHAPTER 1", "Chapter I", "PART 1")
        val chapterRegex = Regex("(?i)^(chapter|part|book|section)\\s+([0-9a-zivxlcdm]+).*", RegexOption.MULTILINE)
        val matches = chapterRegex.findAll(fullContent).toList()

        val chapters = mutableListOf<Chapter>()

        if (matches.size >= 2) {
            // Split content by chapter regex matches
            var lastIndex = 0
            var lastTitle = "Introduction"

            matches.forEachIndexed { index, match ->
                val startIndex = match.range.first
                if (startIndex > lastIndex) {
                    val chapterContent = fullContent.substring(lastIndex, startIndex).trim()
                    if (chapterContent.isNotBlank()) {
                        chapters.add(
                            Chapter(
                                id = "$bookId-ch-${chapters.size}",
                                bookId = bookId,
                                title = lastTitle,
                                content = chapterContent,
                                chapterIndex = chapters.size
                            )
                        )
                    }
                }
                lastTitle = match.value.trim()
                lastIndex = startIndex
            }

            // Remaining text for last chapter
            if (lastIndex < fullContent.length) {
                val lastContent = fullContent.substring(lastIndex).trim()
                if (lastContent.isNotBlank()) {
                    chapters.add(
                        Chapter(
                            id = "$bookId-ch-${chapters.size}",
                            bookId = bookId,
                            title = lastTitle,
                            content = lastContent,
                            chapterIndex = chapters.size
                        )
                    )
                }
            }
        } else {
            // Fallback: Chunk text into ~2,500 word sections for comfortable continuous scrolling
            val paragraphs = fullContent.split("\n\n")
            var currentChunk = StringBuilder()
            var currentWordCount = 0

            paragraphs.forEach { paragraph ->
                val words = paragraph.split("\\s+".toRegex()).size
                if (currentWordCount + words > 2000 && currentChunk.isNotEmpty()) {
                    chapters.add(
                        Chapter(
                            id = "$bookId-ch-${chapters.size}",
                            bookId = bookId,
                            title = "Section ${chapters.size + 1}",
                            content = currentChunk.toString().trim(),
                            chapterIndex = chapters.size
                        )
                    )
                    currentChunk = StringBuilder()
                    currentWordCount = 0
                }
                currentChunk.append(paragraph).append("\n\n")
                currentWordCount += words
            }

            if (currentChunk.isNotEmpty()) {
                chapters.add(
                    Chapter(
                        id = "$bookId-ch-${chapters.size}",
                        bookId = bookId,
                        title = "Section ${chapters.size + 1}",
                        content = currentChunk.toString().trim(),
                        chapterIndex = chapters.size
                    )
                )
            }
        }

        val book = Book(
            id = bookId,
            title = title,
            author = "Local Document",
            filePath = uri.toString(),
            fileType = BookFileType.TXT,
            totalChapters = chapters.size.coerceAtLeast(1),
            progress = 0f
        )

        return EpubParser.ParsedBookResult(book, chapters)
    }
}
