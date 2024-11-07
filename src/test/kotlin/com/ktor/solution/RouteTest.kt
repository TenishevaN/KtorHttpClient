package com.ktor.solution

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json


class RouteTest {

    @Test
    fun testGetPosts() = testApplication {
        val client = configureServerAndGetClient()
        val response: HttpResponse = client.get("/posts")
        assertEquals(HttpStatusCode.OK, response.status)
        val posts: List<Post> = Json.decodeFromString(response.bodyAsText())
        assertEquals(100, posts.size)
        assertEquals("sunt aut facere repellat provident occaecati excepturi optio reprehenderit", posts.first().title)
    }

    @Test
    fun testGetCommentsForUser() = testApplication {
        val client = configureServerAndGetClient()
        val response: HttpResponse = client.get("/comments/1")
        assertEquals(HttpStatusCode.OK, response.status)
        val comments: List<Comment> = Json.decodeFromString(response.bodyAsText())
        assertTrue(comments.isNotEmpty())
        assertEquals("id labore ex et quam laborum", comments.first().name)
    }

    @Test
    fun testCreatePost() = testApplication {
        val client = configureServerAndGetClient()
        val newPost = Post(101, 101, "New post", "Content")
        val response: HttpResponse = client.post("/post") {
            contentType(ContentType.Application.Json)
            setBody(newPost)
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val createdPost: Post = Json.decodeFromString(response.bodyAsText())
        assertEquals("New post", createdPost.title)
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
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testGetCommentsForNonExistedUser() = testApplication {
        val client = configureServerAndGetClient()
        val response: HttpResponse = client.get("/comments/9999")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testCreatePostWithInvalidData() = testApplication {
        val client = configureServerAndGetClient()
        val response: HttpResponse = client.post("/post") {
            contentType(ContentType.Application.Json)
            setBody("{}")
        }
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    @Test
    fun testDeleteNonExistentPostDoesNotFail() = testApplication {
        val client = configureServerAndGetClient()
        val response: HttpResponse = client.delete("/posts/9999")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testUpdatePost() = testApplication {
        val client = configureServerAndGetClient()
        val updatedData = Post(1, 1, "Updated Title", "Updated Post")
        val response: HttpResponse = client.put("/post/1") {
            contentType(ContentType.Application.Json)
            setBody(updatedData)
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testUpdateNonExistentPost() = testApplication {
        val client = configureServerAndGetClient()
        val updatedData = Post(1, 1, "Title", "Updated Post")
        val response: HttpResponse = client.put("/post/9999") {
            contentType(ContentType.Application.Json)
            setBody(updatedData)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}