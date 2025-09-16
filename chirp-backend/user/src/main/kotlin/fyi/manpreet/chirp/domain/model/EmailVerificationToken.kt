package fyi.manpreet.chirp.domain.model

import fyi.manpreet.chirp.domain.user.User

data class EmailVerificationToken(
    val id: Long,
    val token: EmailToken,
    val user: User,
)

@JvmInline
value class EmailToken(val value: String)