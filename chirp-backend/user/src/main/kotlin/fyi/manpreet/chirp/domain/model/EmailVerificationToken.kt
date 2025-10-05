package fyi.manpreet.chirp.domain.model

import fyi.manpreet.chirp.domain.user.User
import fyi.manpreet.chirp.domain.type.VerificationToken

data class EmailVerificationToken(
    val id: Long,
    val token: VerificationToken,
    val user: User,
)
