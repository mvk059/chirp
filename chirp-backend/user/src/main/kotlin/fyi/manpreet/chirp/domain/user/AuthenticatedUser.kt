package fyi.manpreet.chirp.domain.user

data class AuthenticatedUser(
    val user: User,
    val accessToken: AccessToken,
    val refreshToken: RefreshToken,
)

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