package com.example.whiteboardai

import retrofit2.Response
import retrofit2.http.*

// Request / Response data classes
data class RegisterRequest(val name: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class AuthUser(val id: String, val name: String, val email: String)
data class AuthResponse(val token: String, val user: AuthUser)

data class SaveBoardRequest(val id: String?, val title: String, val boardData: Any)
data class BoardItem(val _id: String, val title: String, val updatedAt: String)

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("api/boards/save")
    suspend fun saveBoard(
        @Header("Authorization") token: String,
        @Body body: SaveBoardRequest
    ): Response<BoardItem>

    @GET("api/boards/load")
    suspend fun loadBoards(
        @Header("Authorization") token: String
    ): Response<List<BoardItem>>
}