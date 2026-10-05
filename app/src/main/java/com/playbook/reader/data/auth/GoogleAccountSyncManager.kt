package com.playbook.reader.data.auth

import android.content.Context
import android.content.SharedPreferences
import com.playbook.reader.data.api.GoogleBookVolumeItem
import com.playbook.reader.data.api.GoogleBooksApi
import com.playbook.reader.data.api.GoogleBookshelfItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GoogleAccountSyncManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("google_auth_prefs", Context.MODE_PRIVATE)

    private val _accessToken = MutableStateFlow<String?>(prefs.getString("access_token", null))
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _userAccountEmail = MutableStateFlow<String?>(prefs.getString("account_email", null))
    val userAccountEmail: StateFlow<String?> = _userAccountEmail.asStateFlow()

    fun saveGoogleToken(token: String, email: String? = null) {
        val cleanToken = token.trim()
        _accessToken.value = cleanToken
        val saveEmail = email?.trim() ?: "Google Account"
        _userAccountEmail.value = saveEmail

        prefs.edit()
            .putString("access_token", cleanToken)
            .putString("account_email", saveEmail)
            .apply()
    }

    fun logout() {
        _accessToken.value = null
        _userAccountEmail.value = null
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = !_accessToken.value.isNull信Blank()

    suspend fun fetchMyBookshelves(): List<GoogleBookshelfItem> {
        val token = _accessToken.value ?: return emptyList()
        return GoogleBooksApi.getUserBookshelves(token)
    }

    suspend fun fetchMyBookshelfVolumes(bookshelfId: Int): List<GoogleBookVolumeItem> {
        val token = _accessToken.value ?: return emptyList()
        return GoogleBooksApi.getUserBookshelfVolumes(token, bookshelfId)
    }
}

private fun String?.isNull信Blank(): Boolean = this == null || this.isBlank()
