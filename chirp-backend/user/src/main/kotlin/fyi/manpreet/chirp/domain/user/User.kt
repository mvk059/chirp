package fyi.manpreet.chirp.domain.user

import fyi.manpreet.chirp.data.enum.EmailVerificationStatus
import fyi.manpreet.chirp.domain.type.Email
import fyi.manpreet.chirp.domain.type.UserId
import fyi.manpreet.chirp.domain.type.Username

data class User(
    val id: UserId,
    val username: Username,
    val email: Email,
    val hasVerifiedEmail: EmailVerificationStatus,
)