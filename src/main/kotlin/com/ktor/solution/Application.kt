package com.ktor.solution

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.client.statement.*

import io.ktor.server.request.*
import io.ktor.http.*

import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

import kotlinx.serialization.Serializable

@Serializable
data class Post(val userId: Int, val id: Int, val title: String, val body: String)

@Serializable
data class Comment(val postId: Int, val id: Int, val name: String, val email: String, val body: String)


fun main() {
    val client = HttpClientProvider.client
    val postService = PostService(client)
    val commentService = CommentService(client)

    embeddedServer(Netty, port = 8080) {
        install(ServerContentNegotiation) {
            json(Json {
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        routing {
            get("/posts/{id}") {
                val postId = call.parameters["id"]?.toIntOrNull()
                if (postId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid post ID")
                    return@get
                }
                val response = postService.fetchPost(postId)
                if (response.status == HttpStatusCode.OK) {
                    call.respondText(response.bodyAsText(), contentType = ContentType.Application.Json)
                } else {
                    call.respondText("Failed to fetch post data: ${response.status}", status = response.status)
                }
            }

            get("/posts") {
                val response = postService.fetchAllPosts()
                if (response.status == HttpStatusCode.OK) {
                    call.respondText(response.bodyAsText(), contentType = ContentType.Application.Json)
                } else {
                    call.respondText("Failed to fetch posts: ${response.status}", status = response.status)
                }
            }

            post("/post") {
                val post = call.receive<Post>()
                val response = postService.createPost(post)
                if (response.status == HttpStatusCode.Created) {
                    call.respondText(response.bodyAsText(), status = HttpStatusCode.Created, contentType = ContentType.Application.Json)
                } else {
                    call.respondText("Failed to create post: ${response.status}", status = response.status)
                }
            }

            put("/post/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid post ID")
                    return@put
                }
                val post = call.receive<Post>()
                val response = postService.updatePost(id, post)
                if (response.status == HttpStatusCode.OK) {
                    call.respondText(response.bodyAsText(), contentType = ContentType.Application.Json)
                } else {
                    call.respondText("Failed to update post: ${response.status}", status = response.status)
                }
            }

            delete("/post/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid post ID")
                    return@delete
                }
                val response = postService.deletePost(id)
                if (response.status == HttpStatusCode.OK) {
                    call.respondText("Post deleted successfully", status = HttpStatusCode.OK)
                } else {
                    call.respondText("Failed to delete post: ${response.status}", status = response.status)
                }
            }

            get("/comments") {
                val response = commentService.fetchComments()
                if (response.status == HttpStatusCode.OK) {
                    call.respondText(response.bodyAsText(), contentType = ContentType.Application.Json)
                } else {
                    call.respondText("Failed to fetch comments: ${response.status}", status = response.status)
                }
            }
        }
    }.start(wait = true)
}
