package com.saminz.focus

fun FocusSession.toResponse(): FocusSessionResponse {
    return FocusSessionResponse(
        id = id,
        subject = subject,
        startTime = startTime,
        endTime = endTime,
        durationSeconds = durationSeconds,
        status = status,
    )
}
