package fyi.manpreet.chirp.domain.model

import fyi.manpreet.chirp.domain.type.Email
import fyi.manpreet.chirp.domain.type.ProfilePictureUrl
import fyi.manpreet.chirp.domain.type.UserId
import fyi.manpreet.chirp.domain.type.Username

data class ChatParticipant(
    val userId: UserId,
    val username: Username,
    val email: Email,
    val profilePictureUrl: ProfilePictureUrl?,
)
