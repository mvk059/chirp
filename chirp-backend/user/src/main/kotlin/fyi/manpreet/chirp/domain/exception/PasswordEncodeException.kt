package fyi.manpreet.chirp.domain.exception

class PasswordEncodeException(val exception: Throwable): RuntimeException(
    "Password encoding failed: $exception"
)