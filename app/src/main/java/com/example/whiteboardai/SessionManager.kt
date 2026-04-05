package com.example.whiteboardai

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("whiteboard_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) = prefs.edit().putString("token", token).apply()
    fun getToken(): String? = prefs.getString("token", null)

    fun saveName(name: String) = prefs.edit().putString("name", name).apply()
    fun getName(): String? = prefs.getString("name", null)

    fun isLoggedIn(): Boolean = getToken() != null

    fun logout() = prefs.edit().clear().apply()
}