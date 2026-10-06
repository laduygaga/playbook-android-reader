package com.playbook.reader.data.translation

import com.google.gson.JsonParser
import com.playbook.reader.domain.model.SupportedLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

class TranslationService {

    private val translationCache = ConcurrentHashMap<String, String>()

    suspend fun translateText(
        chapterId: String,
        text: String,
        targetLanguage: SupportedLanguage
    ): Result<String> = withContext(Dispatchers.IO) {
        val cacheKey = "$chapterId-${targetLanguage.code}"
        translationCache[cacheKey]?.let { cached ->
            return@withContext Result.success(cached)
        }

        if (text.isBlank()) {
            return@withContext Result.success("")
        }

        try {
            val paragraphs = text.split("\n\n").map { it.trim() }.filter { it.isNotEmpty() }
            val translatedParagraphs = mutableListOf<String>()

            for (paragraph in paragraphs) {
                if (paragraph.length > 1500) {
                    // Split long paragraph by sentences if needed
                    val sentences = paragraph.split(Regex("(?<=[.!?])\\s+"))
                    val translatedSentences = sentences.map { sentence ->
                        if (sentence.isNotBlank()) translateSingleChunk(sentence, targetLanguage.code) else sentence
                    }
                    translatedParagraphs.add(translatedSentences.joinToString(" "))
                } else {
                    translatedParagraphs.add(translateSingleChunk(paragraph, targetLanguage.code))
                }
            }

            val fullTranslatedText = translatedParagraphs.joinToString("\n\n")
            translationCache[cacheKey] = fullTranslatedText
            Result.success(fullTranslatedText)
        } catch (e: Exception) {
            e.printStackTrace()
            // Provide a graceful fallback if network fails
            Result.failure(e)
        }
    }

    private fun translateSingleChunk(chunk: String, targetLangCode: String): String {
        return try {
            val encodedQuery = URLEncoder.encode(chunk, "UTF-8")
            val urlString = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=$targetLangCode&dt=t&q=$encodedQuery"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            if (connection.responseCode == 200) {
                val jsonResponse = connection.inputStream.bufferedReader().use { it.readText() }
                parseGtxResponse(jsonResponse)
            } else {
                translateViaMyMemory(chunk, targetLangCode)
            }
        } catch (e: Exception) {
            try {
                translateViaMyMemory(chunk, targetLangCode)
            } catch (e2: Exception) {
                chunk // Fallback to original text if both offline
            }
        }
    }

    private fun parseGtxResponse(json: String): String {
        val root = JsonParser.parseString(json).asJsonArray
        val sentencesArray = root.get(0).asJsonArray
        val sb = StringBuilder()
        for (i in 0 until sentencesArray.size()) {
            val sentence = sentencesArray.get(i).asJsonArray
            if (sentence.size() > 0 && !sentence.get(0).isJsonNull) {
                sb.append(sentence.get(0).asString)
            }
        }
        return sb.toString().ifBlank { json }
    }

    private fun translateViaMyMemory(chunk: String, targetLangCode: String): String {
        val encodedQuery = URLEncoder.encode(chunk, "UTF-8")
        val urlString = "https://api.mymemory.translated.net/get?q=$encodedQuery&langpair=autodetect|$targetLangCode"
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 8000
        connection.readTimeout = 8000

        if (connection.responseCode == 200) {
            val jsonResponse = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JsonParser.parseString(jsonResponse).asJsonObject
            val responseData = root.getAsJsonObject("responseData")
            val translated = responseData.get("translatedText").asString
            if (translated.isNotBlank() && !translated.contains("MYMEMORY WARNING")) {
                return translated
            }
        }
        return chunk
    }

    fun clearCache() {
        translationCache.clear()
    }
}
