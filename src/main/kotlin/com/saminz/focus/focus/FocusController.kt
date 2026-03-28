package com.saminz.focus.focus

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
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

        return FocusSessionResponse(
            id = session.id,
            subject = session.subject,
            startTime = session.startTime,
            status = session.status.name,
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgument(ex: IllegalArgumentException): Map<String, String> =
        mapOf("error" to (ex.message ?: "invalid request"))
}
