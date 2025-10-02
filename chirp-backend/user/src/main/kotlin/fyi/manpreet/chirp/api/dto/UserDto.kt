package fyi.manpreet.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import fyi.manpreet.chirp.data.enum.EmailVerificationStatus
import fyi.manpreet.fyi.manpreet.chirp.domain.type.Email
import fyi.manpreet.fyi.manpreet.chirp.domain.type.UserId
import fyi.manpreet.fyi.manpreet.chirp.domain.type.Username

data class UserDto(
    @field:JsonProperty
    @get:JsonIgnore
    val id: UserId,
    @field:JsonProperty
    @get:JsonIgnore
    val email: Email,
    @field:JsonProperty
    @get:JsonIgnore
    val username: Username,
    @field:JsonProperty
    @get:JsonIgnore
    val hasVerifiedEmail: EmailVerificationStatus,
)
