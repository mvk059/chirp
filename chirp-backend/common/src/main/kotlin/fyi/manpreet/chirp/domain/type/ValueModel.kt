package fyi.manpreet.fyi.manpreet.chirp.domain.type

import java.util.UUID

@JvmInline
value class UserId(val value: UUID)

@JvmInline
value class Username(val value: String)

@JvmInline
value class Email(val value: String)

@JvmInline
value class RawPassword(val value: String)

@JvmInline
value class HashedPassword(val value: String)

@JvmInline
value class VerificationToken(val value: String)

@JvmInline
value class PasswordResetToken(val value: String)