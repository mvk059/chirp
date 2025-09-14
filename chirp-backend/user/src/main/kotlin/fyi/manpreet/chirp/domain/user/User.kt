package fyi.manpreet.chirp.domain.user

import fyi.manpreet.chirp.data.model.Email
import fyi.manpreet.chirp.data.model.UserId
import fyi.manpreet.chirp.data.model.Username

data class User(
    val id: UserId,
    val username: Username,
    val email: Email,
    val hasVerifiedEmail: EmailVerificationStatus,
)

enum class EmailVerificationStatus {
    VERIFIED, NOT_VERIFIED,
}
