package fyi.manpreet.chirp.domain.type

import java.util.*

//@JvmInline
//value class UserId(val value: UUID)
typealias UserId = UUID

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

@JvmInline
value class AccessToken(val token: String)

@JvmInline
value class RefreshToken(val token: String)

enum class Token {
    AccessToken, RefreshToken
}

enum class TokenValidity {
    ValidAccessToken,
    ValidRefreshToken,
    InvalidToken
}