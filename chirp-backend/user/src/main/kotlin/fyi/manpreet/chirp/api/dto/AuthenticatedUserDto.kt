package fyi.manpreet.chirp.api.dto

import fyi.manpreet.chirp.domain.user.AccessToken
import fyi.manpreet.chirp.domain.user.RefreshToken

data class AuthenticatedUserDto(
    val user: UserDto,
    val accessToken: AccessToken,
    val refreshToken: RefreshToken,
)