package ru.sleepryrabbit.dzserver

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import ru.sleepyrabbit.dzserver.http.HttpPacket
import ru.sleepyrabbit.dzserver.http.HttpRouter
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class SimpleHttpRequestAndResponseTest: AbstractTest() {

    @Test
    @DisplayName("Simple get request test")
    fun testSimpleGetRequest() {
        httpRouter.bind(HttpRouter.GET, "/get"){
            println("Route function")
            "Cool, it`s work!"
        }.addPreProcessor{
            println("preprocessor")
            it!!
        }.addPostProcessor {
            println("postprocessor 1")
            it!!
        }.addPostProcessor {
            println("postprocessor 2")
            val body = (it!! as String).toByteArray()
            val headers = mapOf(
                "Server" to "dzrestserver/1.0",
                "Content-Type" to "application/json",
                "Content-Length" to body.size.toString()
            )
            HttpPacket("HTTP/1.1 200 OK", headers, body)
        }

        val simpleData = """
            {
                "hello":"world"
            }
        """.trimIndent().toByteArray()

        val httpClient = HttpClient.newHttpClient()
        val request = HttpRequest
            .newBuilder("http://127.0.0.1:9000/get".toUri())
            .header("Content-Type", "application/json")
            .timeout(Duration.ofMillis(100))
            .method("GET", HttpRequest.BodyPublishers.ofByteArray(simpleData))
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        Assertions.assertEquals(200, response.statusCode())
        Assertions.assertEquals("Cool, it`s work!", response.body())

        httpRouter.unbind(HttpRouter.GET, "/get")
    }

}