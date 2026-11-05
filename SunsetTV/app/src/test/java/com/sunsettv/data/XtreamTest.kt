package com.sunsettv.data

import com.sun.net.httpserver.HttpServer
import org.junit.Assert.*
import org.junit.Test
import java.net.InetSocketAddress

class XtreamTest {
    @Test fun credentialsAreEncodedInQueryAndStreamPaths() {
        val api = Xtream("https://example.org", "a+b @", "p/&?#")
        assertTrue(api.api.contains("username=a%2Bb%20%40"))
        assertTrue(api.api.contains("password=p%2F%26%3F%23"))
        assertEquals("p%2F%26%3F%23", api.pass)
    }

    private fun server(response: String, block: (Xtream) -> Unit) {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/player_api.php") { exchange ->
            val bytes = response.toByteArray()
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        try { block(Xtream("http://127.0.0.1:${server.address.port}", "user", "pass")) }
        finally { server.stop(0) }
    }

    @Test fun acceptsActiveAccount() = server("""{"user_info":{"auth":1,"status":"Active"}}""") { assertTrue(it.authOk()) }
    @Test fun rejectsExpiredAccount() = server("""{"user_info":{"auth":1,"status":"Expired"}}""") { assertFalse(it.authOk()) }
    @Test fun rejectsInvalidAccount() = server("""{"user_info":{"auth":0}}""") { assertFalse(it.authOk()) }
}
