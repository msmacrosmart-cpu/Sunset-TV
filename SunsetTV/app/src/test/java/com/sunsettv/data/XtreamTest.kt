package com.sunsettv.data

import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.*
import org.junit.Test

class XtreamTest {
    @Test fun credentialsAreEncodedInQueryAndStreamPaths() {
        val api = Xtream("https://example.org", "a+b @", "p/&?#")
        assertTrue(api.api.contains("username=a%2Bb%20%40"))
        assertTrue(api.api.contains("password=p%2F%26%3F%23"))
        assertEquals("p%2F%26%3F%23", api.pass)
    }

    private fun server(response: String, block: (Xtream) -> Unit) {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(200).setBody(response))
        server.start()
        try { block(Xtream(server.url("/").toString().trimEnd('/'), "user", "pass")) }
        finally { server.shutdown() }
    }

    @Test fun acceptsActiveAccount() = server("""{"user_info":{"auth":1,"status":"Active"}}""") { assertTrue(it.authOk()) }
    @Test fun rejectsExpiredAccount() = server("""{"user_info":{"auth":1,"status":"Expired"}}""") { assertFalse(it.authOk()) }
    @Test fun rejectsInvalidAccount() = server("""{"user_info":{"auth":0}}""") { assertFalse(it.authOk()) }
}
