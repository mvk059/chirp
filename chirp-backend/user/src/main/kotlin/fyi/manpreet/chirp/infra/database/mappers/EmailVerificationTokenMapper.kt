package fyi.manpreet.chirp.infra.database.mappers

import fyi.manpreet.chirp.domain.model.EmailToken
import fyi.manpreet.chirp.domain.model.EmailVerificationToken
import fyi.manpreet.chirp.infra.database.entities.EmailVerificationTokenEntity

fun EmailVerificationTokenEntity.toEmailVerificationToken() = EmailVerificationToken(
    id = id,
    token = EmailToken(token),
    user = user.toUser(),
)