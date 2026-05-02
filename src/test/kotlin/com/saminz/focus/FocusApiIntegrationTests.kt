package com.saminz.focus

import com.saminz.focus.focus.FocusSessionService
import com.saminz.focus.focus.StartFocusSessionRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@AutoConfigureWebTestClient
class FocusApiIntegrationTests {

    @Autowired private lateinit var webTestClient: WebTestClient

    // builds requests like a real caller would: choose method (GET/POST/…), path, headers, JSON body
    @Autowired private lateinit var focusSessionService: FocusSessionService

    @BeforeEach
    fun resetService() {
        focusSessionService.resetStateForTests()
    }

    // exchange actually fire the request and get a response
    @Test
    fun `POST start returns 201 and ACTIVE session`() {
        webTestClient
            .post()
            .uri("/focus/start")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(StartFocusSessionRequest("Reading"))
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody()
            .jsonPath("$.id")
            .isEqualTo(1)
            .jsonPath("$.subject")
            .isEqualTo("Reading")
            .jsonPath("$.status")
            .isEqualTo("ACTIVE")
            .jsonPath("$.endTime")
            .isEmpty
            .jsonPath("$.durationSeconds")
            .isEmpty
    }

    @Test
    fun `POST start blank subject returns 400 with ApiErrorResponse`() {
        webTestClient
            .post()
            .uri("/focus/start")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(StartFocusSessionRequest("   "))
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.error")
            .isEqualTo("subject can not be empty")
    }
}
