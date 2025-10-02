package fyi.manpreet.chirp.infra.database.mappers

import fyi.manpreet.chirp.domain.model.EmailVerificationToken
import fyi.manpreet.chirp.infra.database.entities.EmailVerificationTokenEntity
import fyi.manpreet.fyi.manpreet.chirp.domain.type.VerificationToken

fun EmailVerificationTokenEntity.toEmailVerificationToken() = EmailVerificationToken(
    id = id,
    token = VerificationToken(token),
    user = user.toUser(),
)