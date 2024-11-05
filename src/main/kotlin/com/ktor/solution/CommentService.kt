package com.ktor.solution

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CommentService(private val client: HttpClient) {
    private val logger: Logger = LoggerFactory.getLogger(CommentService::class.java)

    suspend fun fetchComments(): HttpResponse {
        return try {
            client.get("${Config.API_BASE_URL}/comments")
        } catch (e: Exception) {
            logger.error("Error fetching comments: ${e.localizedMessage}")
            throw e
        }
    }
}