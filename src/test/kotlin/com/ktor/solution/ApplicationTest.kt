package com.ktor.solution

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import io.ktor.client.statement.*

class ApplicationTest {

    @Test
    fun testGetPosts() = testApplication {
        val client = configureServerAndGetClient()
        val response: HttpResponse = client.get("/posts")
        assertEquals(HttpStatusCode.OK, response.status)
        val posts: List<Post> = Json.decodeFromString(response.bodyAsText())
        assertEquals(1, posts.size)
        assertEquals("Test Post", posts.first().title)
    }

    @Test
    fun testGetCommentsForUser() = testApplication {
        val client = configureServerAndGetClient()
        val response: HttpResponse = client.get("/comments/1")
        assertEquals(HttpStatusCode.OK, response.status)
        val comments: List<Comment> = Json.decodeFromString(response.bodyAsText())
        assertTrue(comments.isNotEmpty())
        assertEquals("John Doe", comments.first().name)
    }

    @Test
    fun testCreatePost() = testApplication {
        val client = configureServerAndGetClient()
        val newPost = Post(userId = 1, id = 101, title = "New Post", body = "This is a new post.")
        val response: HttpResponse = client.post("/post") {
            contentType(ContentType.Application.Json)
            setBody(newPost)
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val createdPost: Post = Json.decodeFromString(response.bodyAsText())
        assertEquals("New Post", createdPost.title)
    }

    @Test
    fun testUpdatePost() = testApplication {
        val client = configureServerAndGetClient()
        val updatedData = Post(userId = 1, id = 1, title = "Updated Post", body = "Updated body.")
        val response: HttpResponse = client.put("/post/1") {
            contentType(ContentType.Application.Json)
            setBody(updatedData)
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testDeletePost() = testApplication {
        val client = configureServerAndGetClient()
        val response: HttpResponse = client.delete("/post/1")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testGetPostWithInvalidId() = testApplication {
        val client = configureServerAndGetClient()
        val response: HttpResponse = client.get("/posts/invalid_id")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Invalid post ID"))
    }

    @Test
    fun testGetCommentsForNonExistentUser() = testApplication {
        val client = configureServerAndGetClient()
        val response: HttpResponse = client.get("/comments/9999")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.bodyAsText().isEmpty())
    }

    @Test
    fun testCreatePostWithInvalidData() = testApplication {
        val client = configureServerAndGetClient()
        val response: HttpResponse = client.post("/post") {
            contentType(ContentType.Application.Json)
            setBody("{}")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testUpdateNonExistentPost() = testApplication {
        val client = configureServerAndGetClient()
        val updatedData = Post(userId = 1, id = 9999, title = "Updated Post", body = "Updated body.")
        val response: HttpResponse = client.put("/post/9999") {
            contentType(ContentType.Application.Json)
            setBody(updatedData)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testDeleteNonExistentPostDoesNotFail() = testApplication {
        val client = configureServerAndGetClient()
        val response: HttpResponse = client.delete("/posts/9999")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}