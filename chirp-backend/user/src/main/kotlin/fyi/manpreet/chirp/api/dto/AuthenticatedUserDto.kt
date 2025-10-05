package fyi.manpreet.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import fyi.manpreet.chirp.domain.type.AccessToken
import fyi.manpreet.chirp.domain.type.RefreshToken

data class AuthenticatedUserDto(
    val user: UserDto,
    @field:JsonProperty
    @get:JsonIgnore
    val accessToken: AccessToken,
    @field:JsonProperty
    @get:JsonIgnore
    val refreshToken: RefreshToken,
)