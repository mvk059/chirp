package fyi.manpreet.chirp.api.mapper

import fyi.manpreet.chirp.api.dto.AuthenticatedUserDto
import fyi.manpreet.chirp.api.dto.UserDto
import fyi.manpreet.chirp.domain.user.AuthenticatedUser
import fyi.manpreet.chirp.domain.user.User

fun AuthenticatedUser.toAuthenticatedUserDto(): AuthenticatedUserDto = AuthenticatedUserDto(
    user = user.toUserDto(),
    accessToken = accessToken,
    refreshToken = refreshToken,
)

fun User.toUserDto(): UserDto = UserDto(
    id = id,
    email = email,
    username = username,
    hasVerifiedEmail = hasVerifiedEmail,
)