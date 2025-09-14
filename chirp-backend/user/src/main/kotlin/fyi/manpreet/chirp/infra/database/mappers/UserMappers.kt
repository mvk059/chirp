package fyi.manpreet.chirp.infra.database.mappers

import fyi.manpreet.chirp.data.enum.EmailVerificationStatus
import fyi.manpreet.chirp.domain.user.User
import fyi.manpreet.chirp.infra.database.entities.UserEntity


fun UserEntity.toUser(): User {
    return User(
        id = id!!,
        username = username,
        email = email,
        hasVerifiedEmail = if (hasVerifiedEmail) EmailVerificationStatus.VERIFIED else EmailVerificationStatus.NOT_VERIFIED,
    )
}