package com.ktor.solution

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val userId: Int? = null, val id: Int? = null, val title: String, val body: String
)

@Serializable
data class Comment(val postId: Int? = null, val id: Int? = null, val name: String, val email: String, val body: String)

fun main() {
    embeddedServer(Netty, port = 8080) {
        configureRouting(getHttpClient())
        configureContentNegotiation()
    }.start(wait = true)
}
