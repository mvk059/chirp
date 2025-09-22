package fyi.manpreet.chirp.api.dto

import java.time.Instant

data class ErrorBody(
    val timestamp: Instant = Instant.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)
