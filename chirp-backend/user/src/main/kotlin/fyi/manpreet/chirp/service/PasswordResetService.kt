package fyi.manpreet.chirp.service

import fyi.manpreet.chirp.domain.event.user.UserEvent
import fyi.manpreet.chirp.domain.exception.InvalidCredentialsException
import fyi.manpreet.fyi.manpreet.chirp.domain.exception.InvalidTokenException
import fyi.manpreet.chirp.domain.exception.SamePasswordException
import fyi.manpreet.chirp.domain.exception.UserNotFoundException
import fyi.manpreet.chirp.infra.database.entities.PasswordResetTokenEntity
import fyi.manpreet.chirp.infra.database.repository.PasswordResetTokenRepository
import fyi.manpreet.chirp.infra.database.repository.RefreshTokenRepository
import fyi.manpreet.chirp.infra.database.repository.UserRepository
import fyi.manpreet.chirp.infra.message_queue.EventPublisher
import fyi.manpreet.chirp.infra.security.PasswordEncoder
import fyi.manpreet.chirp.domain.type.Email
import fyi.manpreet.chirp.domain.type.PasswordResetToken
import fyi.manpreet.chirp.domain.type.RawPassword
import fyi.manpreet.chirp.domain.type.UserId
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    @param:Value("\${chirp.email.reset-password.expiry-minutes}") private val expiryMinutes: Long,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val eventPublisher: EventPublisher,
) {

    @Transactional
    fun requestPasswordReset(email: Email) {
        val user = userRepository.findByEmail(email.value) ?: return

        passwordResetTokenRepository.invalidateActiveTokensForUser(user)

        val token = PasswordResetTokenEntity(
            user = user,
            expiresAt = Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES),
        )
        passwordResetTokenRepository.save(token)

        eventPublisher.publish(
            event = UserEvent.RequestResetPassword(
                userId = user.id!!,
                email = user.email,
                username = user.username,
                passwordResetToken = PasswordResetToken(token.token),
                expiresInMinutes = expiryMinutes
            )
        )
    }

    @Transactional
    fun resetPassword(token: String, newPassword: RawPassword) {
        val resetToken = passwordResetTokenRepository.findByToken(token) ?: throw InvalidTokenException("Invalid password reset token")
        if (resetToken.isUsed) throw InvalidTokenException("Email verification token is already used.")
        if (resetToken.isExpired) throw InvalidTokenException("Email verification token has already expired.")

        val user = resetToken.user
        if (passwordEncoder.matches(newPassword, user.hashedPassword)) throw SamePasswordException()
        val newHashedPassword = passwordEncoder.encode(newPassword).getOrElse { throw RuntimeException("Failed to save password") }
        userRepository.save(
            user.apply { this.hashedPassword = newHashedPassword }
        )

        passwordResetTokenRepository.save(
            resetToken.apply { this.usedAt = Instant.now() }
        )

        refreshTokenRepository.deleteByUserId(user.id!!)
    }

    @Transactional
    fun changePassword(
        userId: UserId,
        oldPassword: RawPassword,
        newPassword: RawPassword,
    ) {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()

        if (passwordEncoder.matches(oldPassword, user.hashedPassword).not()) throw InvalidCredentialsException()
        if (oldPassword == newPassword) throw SamePasswordException()

        refreshTokenRepository.deleteByUserId(user.id!!)

        val newHashedPassword = passwordEncoder.encode(newPassword).getOrElse { throw RuntimeException("Failed to save password") }
        userRepository.save(
            user.apply { this.hashedPassword = newHashedPassword }
        )
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteByExpiresAtLessThan(
            now = Instant.now()
        )
    }
}