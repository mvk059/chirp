package fyi.manpreet.chirp.service

import fyi.manpreet.chirp.domain.type.AccessToken
import fyi.manpreet.chirp.domain.type.RefreshToken
import fyi.manpreet.chirp.domain.type.Token
import fyi.manpreet.chirp.domain.type.TokenValidity
import fyi.manpreet.chirp.domain.type.UserId
import fyi.manpreet.fyi.manpreet.chirp.domain.exception.InvalidTokenException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import kotlin.io.encoding.Base64

@Service
class JwtService(
    @param:Value("\${jwt.secret}") private val secret: String,
    @param:Value("\${jwt.expiration-minutes}") private val expirationMinutes: Int,
) {

    private val secretKey = Keys.hmacShaKeyFor(Base64.decode(secret))
    private val accessTokenValidityMs = expirationMinutes * 60 * 1000L
    val refreshTokenValidityMs = 30 * 24 * 60 * 60 * 1000L

    fun generateAccessToken(userId: UserId): AccessToken = AccessToken(
        token = generateToken(
            userId = userId,
            type = Token.AccessToken,
            expirationMs = accessTokenValidityMs
        )
    )

    fun generateRefreshToken(userId: UserId): RefreshToken = RefreshToken(
        token = generateToken(
            userId = userId,
            type = Token.RefreshToken,
            expirationMs = refreshTokenValidityMs
        )
    )

    fun validateAccessToken(token: AccessToken): TokenValidity {
        val claims = parseAllClaims(token.token).getOrElse { return TokenValidity.InvalidToken }
        val tokenType = claims[TYPE] as? String ?: return TokenValidity.InvalidToken
        return if (tokenType == Token.AccessToken.name) TokenValidity.ValidAccessToken else TokenValidity.InvalidToken
    }

    fun validateRefreshToken(token: RefreshToken): TokenValidity {
        val claims = parseAllClaims(token.token).getOrElse { return TokenValidity.InvalidToken }
        val tokenType = claims[TYPE] as? String ?: return TokenValidity.InvalidToken
        return if (tokenType == Token.RefreshToken.name) TokenValidity.ValidRefreshToken else TokenValidity.InvalidToken
    }

    fun getUserIdFromToken(token: String): UserId {
        val claims = parseAllClaims(token).getOrElse { throw InvalidTokenException(message = "The attached JWT token is not valid") }
        return UUID.fromString(claims.subject)
    }

    private fun generateToken(
        userId: UserId,
        type: Token,
        expirationMs: Long,
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationMs)
        return Jwts.builder()
            .subject(userId.toString())
            .claim(TYPE, type.name)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    private fun parseAllClaims(token: String): Result<Claims> {
        val rawToken =
            if (token.startsWith(BEARER)) token.removePrefix(BEARER)
            else token

        return runCatching {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(rawToken)
                .payload
        }
    }

    private companion object {
        const val TYPE = "type"
        const val BEARER = "Bearer "
    }
}