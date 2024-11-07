package com.ktor.solution

import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.application.*

import com.ktor.solution.Config.API_BASE_URL

import io.ktor.client.HttpClient
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object Config {
    const val API_BASE_URL = "https://jsonplaceholder.typicode.com"
}

fun Application.configureContentNegotiation() {
    install(ServerContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

fun getHttpClient(): HttpClient = HttpClient {
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

fun Application.configureRouting(client: HttpClient) {
    val postService = PostService(client);
    val commentService = CommentService(client)
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
                val response: HttpResponse = client.get("${API_BASE_URL}/posts")
                if (response.status == HttpStatusCode.OK) {
                    val posts: List<Post> = Json.decodeFromString(response.bodyAsText())
                    call.respond(posts)
                } else {
                    call.respondText(
                        "Failed to fetch posts, status: ${response.status}",
                        status = response.status
                    )
                }
            } catch (e: Exception) {
                application.log.error("Failed to fetch posts", e)
                call.respond(HttpStatusCode.InternalServerError, "An error occurred: ${e.message ?: "Unknown error"}")
            }
        }

        get("/comments/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid or missing user ID")
                return@get
            }

            val userComments = commentService.fetchCommentsByUserId(userId)
            if (userComments == null || userComments.isEmpty()) {
                call.respond(HttpStatusCode.NotFound, "No comments found for user ID $userId")
            } else {
                call.respond(userComments)
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
                val result = postService.updatePost(id, post)
                when (result.status) {
                    HttpStatusCode.OK -> call.respond(HttpStatusCode.OK, result.post ?: "Post not found")
                    HttpStatusCode.NotFound -> call.respond(HttpStatusCode.NotFound, "Post not found")
                    HttpStatusCode.InternalServerError -> call.respond(result.status, "An error occurred during the update process"
                    )
                }
            } catch (e: Exception) {
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
}