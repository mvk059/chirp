package fyi.manpreet.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import fyi.manpreet.chirp.domain.type.Email
import fyi.manpreet.chirp.domain.type.ProfilePictureUrl
import fyi.manpreet.chirp.domain.type.UserId
import fyi.manpreet.chirp.domain.type.Username

data class ChatParticipantDto(
    @field:JsonProperty
    @get:JsonIgnore
    val userId: UserId,

    @field:JsonProperty
    @get:JsonIgnore
    val username: Username,

    @field:JsonProperty
    @get:JsonIgnore
    val email: Email,

    @field:JsonProperty
    @get:JsonIgnore
    val profilePictureUrl: ProfilePictureUrl?,
)