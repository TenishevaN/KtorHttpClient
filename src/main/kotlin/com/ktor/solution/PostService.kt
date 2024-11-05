package com.ktor.solution

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PostService(private val client: HttpClient) {
    private val logger: Logger = LoggerFactory.getLogger(PostService::class.java)

    suspend fun fetchPost(id: Int): HttpResponse {
        return try {
            client.get("${Config.API_BASE_URL}/posts/$id")
        } catch (e: Exception) {
            logger.error("Error fetching post: ${e.localizedMessage}")
            throw e
        }
    }

    suspend fun fetchAllPosts(): HttpResponse {
        return try {
            client.get("${Config.API_BASE_URL}/posts")
        } catch (e: Exception) {
            logger.error("Error fetching all posts: ${e.localizedMessage}")
            throw e
        }
    }

    suspend fun createPost(post: Post): HttpResponse {
        return try {
            client.post("${Config.API_BASE_URL}/posts") {
                contentType(ContentType.Application.Json)
                setBody(post)
            }
        } catch (e: Exception) {
            logger.error("Error creating post: ${e.localizedMessage}")
            throw e
        }
    }

    suspend fun updatePost(id: Int, post: Post): HttpResponse {
        return try {
            client.put("${Config.API_BASE_URL}/posts/$id") {
                contentType(ContentType.Application.Json)
                setBody(post)
            }
        } catch (e: Exception) {
            logger.error("Error updating post: ${e.localizedMessage}")
            throw e
        }
    }

    suspend fun deletePost(id: Int): HttpResponse {
        return try {
            client.delete("${Config.API_BASE_URL}/posts/$id")
        } catch (e: Exception) {
            logger.error("Error deleting post: ${e.localizedMessage}")
            throw e
        }
    }
}