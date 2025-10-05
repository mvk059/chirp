package fyi.manpreet.chirp.service

import fyi.manpreet.chirp.domain.event.user.UserEvent
import fyi.manpreet.chirp.domain.exception.EmailNotVerifiedException
import fyi.manpreet.chirp.domain.exception.InvalidCredentialsException
import fyi.manpreet.chirp.domain.exception.PasswordEncodeException
import fyi.manpreet.chirp.domain.exception.UserAlreadyExistsException
import fyi.manpreet.chirp.domain.exception.UserNotFoundException
import fyi.manpreet.chirp.domain.type.Email
import fyi.manpreet.chirp.domain.type.RawPassword
import fyi.manpreet.chirp.domain.type.RefreshToken
import fyi.manpreet.chirp.domain.type.TokenValidity
import fyi.manpreet.chirp.domain.type.UserId
import fyi.manpreet.chirp.domain.type.Username
import fyi.manpreet.chirp.domain.user.AuthenticatedUser
import fyi.manpreet.chirp.domain.user.User
import fyi.manpreet.chirp.infra.database.entities.RefreshTokenEntity
import fyi.manpreet.chirp.infra.database.entities.UserEntity
import fyi.manpreet.chirp.infra.database.mappers.toUser
import fyi.manpreet.chirp.infra.database.repository.RefreshTokenRepository
import fyi.manpreet.chirp.infra.database.repository.UserRepository
import fyi.manpreet.chirp.infra.message_queue.EventPublisher
import fyi.manpreet.chirp.infra.security.PasswordEncoder
import fyi.manpreet.fyi.manpreet.chirp.domain.exception.InvalidTokenException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailVerificationService: EmailVerificationService,
    private val eventPublisher: EventPublisher,
) {

    fun register(username: Username, email: Email, rawPassword: RawPassword): User {
        val user = userRepository.findByEmailOrUsername(
            email = email,
            username = username
        )
        if (user != null) throw UserAlreadyExistsException()

        val hashedPassword = passwordEncoder.encode(rawPassword).getOrElse { throw PasswordEncodeException(it) }
        val savedUser = userRepository.saveAndFlush(
            UserEntity(email = email, username = username, hashedPassword = hashedPassword)
        ).toUser()

        val token = emailVerificationService.createVerificationToken(email)

        eventPublisher.publish(
            event = UserEvent.Created(
                userId = savedUser.id,
                email = savedUser.email,
                username = savedUser.username,
                verificationToken = token.token
            )
        )

        return savedUser
    }

    fun login(email: Email, password: RawPassword): AuthenticatedUser {
        val user = userRepository.findByEmail(email.value.trim()) ?: throw InvalidCredentialsException()
        if (passwordEncoder.matches(password, user.hashedPassword).not()) throw InvalidCredentialsException()
        if (!user.hasVerifiedEmail) throw EmailNotVerifiedException()

        val userId = user.id ?: throw UserNotFoundException()
        val accessToken = jwtService.generateAccessToken(userId)
        val refreshToken = jwtService.generateRefreshToken(userId)

        storeRefreshToken(userId, refreshToken)
        return AuthenticatedUser(
            user = user.toUser(),
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    @Transactional
    fun refresh(refreshToken: RefreshToken): AuthenticatedUser {
        if (jwtService.validateRefreshToken(refreshToken) == TokenValidity.InvalidToken) throw InvalidTokenException("Invalid refresh token")

        val userIdFromRefreshToken = jwtService.getUserIdFromToken(refreshToken.token)
        val user = userRepository.findById(userIdFromRefreshToken).orElseThrow { UserNotFoundException() }

        val hashed = hashToken(refreshToken.token)
        val userId = user?.id ?: throw UserNotFoundException()

        refreshTokenRepository.findByUserIdAndHashedToken(userId, hashed) ?: throw InvalidTokenException("Invalid refresh token")
        refreshTokenRepository.deleteByUserIdAndHashedToken(userId = userId, hashedToken = hashed)

        val newAccessToken = jwtService.generateAccessToken(userId)
        val newRefreshToken = jwtService.generateRefreshToken(userId)

        storeRefreshToken(userId, newRefreshToken)

        return AuthenticatedUser(
            user = user.toUser(),
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    @Transactional
    fun logout(refreshToken: RefreshToken) {
        val userId = jwtService.getUserIdFromToken(refreshToken.token)
        val hashedToken = hashToken(refreshToken.token)
        refreshTokenRepository.deleteByUserIdAndHashedToken(userId, hashedToken)
    }

    private fun storeRefreshToken(userId: UserId, refreshToken: RefreshToken) {
        val hashedToken = hashToken(refreshToken.token)
        val expiryMs = jwtService.refreshTokenValidityMs
        val expiresAt = Instant.now().plusMillis(expiryMs)

        refreshTokenRepository.save(
            RefreshTokenEntity(
                userId = userId,
                expiresAt = expiresAt,
                hashedToken = hashedToken,
            )
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}
