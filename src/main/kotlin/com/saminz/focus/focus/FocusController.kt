package com.saminz.focus.focus

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/focus")
class FocusController(
    private val focusSessionService: FocusSessionService,
) {

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    fun startFocusSession(@RequestBody request: StartFocusSessionRequest): FocusSessionResponse {
        val session = focusSessionService.startSession(request.subject)
        return session.toResponse()
    }

    @PostMapping("/{id}/stop")
    fun stopFocusSession(@PathVariable id: Long): FocusSessionResponse {
        val session = focusSessionService.stopSession(id)
        // convert internal FocusSession model to API response DTO
        return session.toResponse()
    }

    @GetMapping("/history")
    // list is the collection or array like
    fun getFocusHistory(): List<FocusSessionResponse> {
        // it => each item
        return focusSessionService.getHistory().map { it.toResponse() }
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgument(ex: IllegalArgumentException): Map<String, String> =
        mapOf("error" to (ex.message ?: "invalid request"))

    @ExceptionHandler(NoSuchElementException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: NoSuchElementException): Map<String, String> =
        mapOf("error" to (ex.message ?: "focus session not found"))

    @ExceptionHandler(IllegalStateException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleIllegalState(ex: IllegalStateException): Map<String, String> =
        mapOf("error" to (ex.message ?: "invalid focus session state"))

    private fun FocusSession.toResponse(): FocusSessionResponse {
        return FocusSessionResponse(
            id = id,
            subject = subject,
            startTime = startTime,
            endTime = endTime,
            durationSeconds = durationSeconds,
            status = status.name,
        )
    }
}
