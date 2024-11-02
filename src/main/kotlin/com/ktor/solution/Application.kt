package com.ktor.solution

import com.ktor.solution.Config.API_BASE_URL
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.HttpClient
import io.ktor.client.call.*

import io.ktor.server.request.*
import io.ktor.http.*

import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

import io.ktor.client.plugins.defaultRequest
import kotlinx.serialization.Serializable

@Serializable
data class Post(val userId: Int, val id: Int, val title: String, val body: String)

@Serializable
data class Comment(val postId: Int, val id: Int, val name: String, val email: String, val body: String)

object Config {
    const val API_BASE_URL = "https://jsonplaceholder.typicode.com"
}

fun main() {

    val client = HttpClient {
        install(ClientContentNegotiation) {
            json(Json {
                encodeDefaults = true
                isLenient = true
                coerceInputValues = true
                ignoreUnknownKeys = true
            })
        }
        defaultRequest {
            host = API_BASE_URL
            port = 80
        }
    }

    suspend fun updatePost(id: Int, newPostData: Post): Post? {
        return try {
            client.put("${API_BASE_URL}/posts/$id") {
                contentType(ContentType.Application.Json)
                setBody(newPostData)
            }.body<Post>()
        } catch (e: Exception) {
            println("Error updating post: ${e.localizedMessage}")
            null
        }
    }

    embeddedServer(Netty, port = 8080) {
        install(ServerContentNegotiation) {
            json(Json {
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        routing {
            get("/posts/{id}") {
                val postId = call.parameters["id"] ?: return@get call.respondText(
                    "Invalid request",
                    status = HttpStatusCode.BadRequest
                )

                try {
                    val response: HttpResponse = client.get("${API_BASE_URL}/posts/$postId")

                    if (response.status == HttpStatusCode.OK) {
                        val responseBody: String = response.bodyAsText()
                        call.respondText(responseBody, contentType = ContentType.Application.Json)
                    } else {
                        call.respondText("Failed to fetch post data: ${response.status}", status = response.status)
                    }
                } catch (e: ClientRequestException) {
                    call.respondText(
                        "Failed to fetch post data: ${e.response.status.description}",
                        status = e.response.status
                    )
                } catch (e: Exception) {
                    call.respondText(
                        "Failed to fetch post data: ${e.localizedMessage}",
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }

            get("/posts") {
                try {
                    val response: HttpResponse = client.get("posts")

                    if (response.status == HttpStatusCode.OK) {
                        val responseBody: String = response.bodyAsText()
                        val posts: List<Post> = Json.decodeFromString(responseBody)
                        call.respond(posts)
                    } else {
                        call.respondText(
                            "Failed to fetch posts, status: ${response.status}",
                            status = response.status
                        )
                    }
                } catch (e: Exception) {
                    application.log.error("Failed to fetch posts", e)
                    call.respond(HttpStatusCode.InternalServerError, "An error occurred: ${e.localizedMessage}")
                }
            }

            get("/comments") {
                val userId = call.parameters["userId"]?.toIntOrNull()
                if (userId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                    return@get
                }

                try {
                    val response: HttpResponse = client.get("${API_BASE_URL}/comments")
                    if (response.status == HttpStatusCode.OK) {
                        val responseBody: String = response.bodyAsText()
                        val comments: List<Comment> = Json.decodeFromString(responseBody)
                        val userComments = comments.filter { it.postId == userId }
                        call.respond(userComments)
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, "Failed to fetch comments")
                    }
                } catch (e: Exception) {
                    println("Error fetching comments: ${e.localizedMessage}")
                    call.respond(HttpStatusCode.InternalServerError, "Error fetching comments: ${e.localizedMessage}")
                }
            }

            post("/post") {
                try {
                    val post = call.receive<Post>()
                    val response = client.post("${API_BASE_URL}/posts") {
                        contentType(ContentType.Application.Json)
                        setBody(post)
                    }
                    val responseBody = response.bodyAsText()
                    call.respondText(responseBody, status = response.status)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to create post: ${e.localizedMessage}")
                }
            }

            put("/post/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid post ID")
                    return@put
                }

                try {
                    val post = call.receive<Post>()
                    val updatedPost = updatePost(id, post)
                    if (updatedPost != null) {
                        call.respond(HttpStatusCode.OK, updatedPost)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Post not found")
                    }
                } catch (e: Exception) {
                    println("Error updating post: ${e.localizedMessage}")
                    call.respond(HttpStatusCode.InternalServerError, "Error updating post: ${e.localizedMessage}")
                }
            }

            delete("/post/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid post ID")
                    return@delete
                }

                try {
                    val response: HttpResponse = client.delete("${API_BASE_URL}/posts/$id")
                    if (response.status == HttpStatusCode.OK) {
                        call.respondText("Post deleted successfully", status = response.status)
                    } else {
                        call.respondText("Failed to delete post", status = response.status)
                    }
                } catch (e: Exception) {
                    println("Error deleting post: ${e.localizedMessage}")
                    call.respond(HttpStatusCode.InternalServerError, "Error deleting post: ${e.localizedMessage}")
                }
            }
        }
    }.start(wait = true)
}