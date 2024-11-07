package com.ktor.solution

import com.ktor.solution.Config.API_BASE_URL
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

import io.ktor.client.HttpClient
import io.ktor.client.statement.*

data class PostUpdateResult(
    val post: Post?,
    val status: HttpStatusCode
)

class PostService(private val client: HttpClient) {

    suspend fun updatePost(id: Int, newPostData: Post): PostUpdateResult {
        return try {
            val response: HttpResponse = client.put("$API_BASE_URL/posts/$id") {
                contentType(ContentType.Application.Json)
                setBody(newPostData)
            }
            if (response.status == HttpStatusCode.OK) {
                PostUpdateResult(response.body<Post>(), HttpStatusCode.OK)
            } else {
                PostUpdateResult(null, HttpStatusCode.NotFound)
            }
        } catch (e: Exception) {
            println("Error updating post: ${e.localizedMessage}")
            PostUpdateResult(null, HttpStatusCode.InternalServerError)
        }
    }
}