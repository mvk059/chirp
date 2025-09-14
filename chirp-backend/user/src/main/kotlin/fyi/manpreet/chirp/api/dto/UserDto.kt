package fyi.manpreet.chirp.api.dto

import fyi.manpreet.chirp.data.enum.EmailVerificationStatus
import fyi.manpreet.chirp.data.model.Email
import fyi.manpreet.chirp.data.model.UserId
import fyi.manpreet.chirp.data.model.Username

data class UserDto(
    val id: UserId,
    val email: Email,
    val username: Username,
    val hasVerifiedEmail: EmailVerificationStatus,
)
