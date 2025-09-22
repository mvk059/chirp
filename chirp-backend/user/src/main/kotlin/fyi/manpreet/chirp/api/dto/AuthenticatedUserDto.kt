package fyi.manpreet.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import fyi.manpreet.chirp.domain.user.AccessToken
import fyi.manpreet.chirp.domain.user.RefreshToken

data class AuthenticatedUserDto(
    val user: UserDto,
    @field:JsonProperty
    @get:JsonIgnore
    val accessToken: AccessToken,
    @field:JsonProperty
    @get:JsonIgnore
    val refreshToken: RefreshToken,
)