package com.saminz.focus

import org.springframework.data.domain.Page

fun FocusSession.toResponse(): FocusSessionResponse {
    return FocusSessionResponse(
        id = id,
        subject = subject,
        startTime = startTime,
        endTime = endTime,
        durationSeconds = duration?.toSeconds(),
        status = status,
    )
}

fun Page<FocusSession>.toPagedResponse(): PagedFocusSessionResponse {
    return PagedFocusSessionResponse(
        content = content.map { it.toResponse() },
        page = number,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
    )
}
