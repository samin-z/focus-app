package com.saminz.focus

data class PagedFocusSessionResponse(
    val content: List<FocusSessionResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
