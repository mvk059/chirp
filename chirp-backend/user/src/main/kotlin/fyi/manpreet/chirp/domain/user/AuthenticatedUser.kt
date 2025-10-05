package fyi.manpreet.chirp.domain.user

import fyi.manpreet.chirp.domain.type.AccessToken
import fyi.manpreet.chirp.domain.type.RefreshToken

data class AuthenticatedUser(
    val user: User,
    val accessToken: AccessToken,
    val refreshToken: RefreshToken,
)
