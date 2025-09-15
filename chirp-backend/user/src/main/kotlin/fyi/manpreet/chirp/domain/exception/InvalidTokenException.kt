package fyi.manpreet.chirp.domain.exception

class InvalidTokenException(override val message: String?): RuntimeException(message ?: "Invalid token")