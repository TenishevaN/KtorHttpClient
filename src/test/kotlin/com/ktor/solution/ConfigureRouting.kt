package com.ktor.solution

import io.ktor.client.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json

val emtyPost = mutableMapOf<Int, Post>()
val posts = mutableMapOf<Int, Post>(
    1 to Post(userId = 1, id = 1, title = "Original Post", body = "Original body.")
)

fun Application.configureRouting() {
    routing {
        get("/posts") {
            val posts = listOf(
                Post(userId = 1, id = 1, title = "Test Post", body = "This is a test post")
            )
            call.respond(posts)
        }

        get("/posts/{id}") {
            val idParam = call.parameters["id"]
            val id = idParam?.toIntOrNull()
            if (idParam == null || id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid post ID")
            } else if (!emtyPost.containsKey(id)) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(emtyPost[id]!!)
            }
        }

        get("/comments/{userId}") {
            val userIdParam = call.parameters["userId"]
            if (userIdParam == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing user ID")
                return@get
            }

            val userId = userIdParam.toIntOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID format")
                return@get
            }

            val validUserIds = setOf(1, 2, 3)

            if (!validUserIds.contains(userId)) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            val comments = if (userId == 1) {
                listOf(
                    Comment(
                        postId = 1,
                        id = 1,
                        name = "John Doe",
                        email = "john@example.com",
                        body = "This is a comment"
                    )
                )
            } else {
                emptyList<Comment>()
            }

            call.respond(comments)
        }

    post("/post") {
        val post = call.receive<Post>()
        call.respond(HttpStatusCode.Created, post)

    }

        put("/post/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val post = call.receive<Post>()
            if (id == null || !posts.containsKey(id)) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                posts[id] = post
                call.respond(HttpStatusCode.OK, post)
            }
        }

        delete("/post/{id}") {
            val postId = call.parameters["id"]?.toIntOrNull()
            if (postId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid post ID format")
                return@delete
            }

            if (!posts.containsKey(postId)) {
                call.respond(HttpStatusCode.NotFound)
                return@delete
            }

            posts.remove(postId)
            call.respond(HttpStatusCode.OK)
        }
}
}

fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json(Json {
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

fun ApplicationTestBuilder.configureServerAndGetClient(): HttpClient {
    application {
        configureRouting()
        configureContentNegotiation()
    }
    val client = createClient {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }
    return client
}