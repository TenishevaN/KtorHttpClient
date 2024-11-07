package com.ktor.solution

import io.ktor.client.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json


fun ApplicationTestBuilder.configureServerAndGetClient(): HttpClient {
    application {
        configureRouting(getHttpClient())
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
