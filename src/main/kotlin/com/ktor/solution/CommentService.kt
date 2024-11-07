package com.ktor.solution

import com.ktor.solution.Config.API_BASE_URL
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class CommentService(private val client: HttpClient) {
    suspend fun fetchCommentsByUserId(userId: Int): List<Comment>? {
        try {
            val response: HttpResponse = client.get("$API_BASE_URL/comments")
            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()
                val comments: List<Comment> = Json.decodeFromString(responseBody)
                return comments.filter { it.postId == userId }
            }
        } catch (e: Exception) {
            println("Error fetching comments: ${e.localizedMessage}")
        }
        return null
    }
}