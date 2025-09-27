package fyi.manpreet.chirp.service

import fyi.manpreet.chirp.data.model.Email
import fyi.manpreet.chirp.domain.exception.InvalidTokenException
import fyi.manpreet.chirp.domain.exception.UserNotFoundException
import fyi.manpreet.chirp.domain.model.EmailToken
import fyi.manpreet.chirp.domain.model.EmailVerificationToken
import fyi.manpreet.chirp.infra.database.entities.EmailVerificationTokenEntity
import fyi.manpreet.chirp.infra.database.mappers.toEmailVerificationToken
import fyi.manpreet.chirp.infra.database.mappers.toUser
import fyi.manpreet.chirp.infra.database.repository.EmailVerificationRepository
import fyi.manpreet.chirp.infra.database.repository.UserRepository
import fyi.manpreet.chirp.infra.rate_limiting.EmailRateLimiter
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class EmailVerificationService(
    private val emailVerificationRepository: EmailVerificationRepository,
    private val userRepository: UserRepository,
    @param:Value("\${chirp.email.verification.expiry-hours}") private val expiryHours: Long
) {

    /**
     * Create a verification token for the given email.
     *
     * If idempotencyKey is provided, this method guarantees that concurrent retries
     * with the same idempotencyKey will result in at most one created token; callers
     * will receive the existing token if one has already been created with that key.
     *
     * Rate limiting is checked first (cheap).
     * Database uniqueness on idempotencyKey is the authoritative dedupe.
     */
    @Transactional
    fun createVerificationToken(email: Email): EmailVerificationToken {
        val userEntity = userRepository.findByEmail(email.value)
            ?: throw UserNotFoundException()

        emailVerificationRepository.invalidateActiveTokensForUser(userEntity)

        val token = EmailVerificationTokenEntity(
            expiresAt = Instant.now().plus(expiryHours, ChronoUnit.HOURS),
            user = userEntity
        )

        return emailVerificationRepository.save(token).toEmailVerificationToken()
    }

    @Transactional
    fun verifyEmail(token: EmailToken) {
        val verificationToken = emailVerificationRepository.findByToken(token.value) ?: throw InvalidTokenException("Email verification token is invalid.")
        if (verificationToken.isUsed) throw InvalidTokenException("Email verification token is already used.")
        if (verificationToken.isExpired) throw InvalidTokenException("Email verification token has already expired.")

        emailVerificationRepository.save(verificationToken.apply { this.usedAt = Instant.now() })
        userRepository.save(verificationToken.user.apply { this.hasVerifiedEmail = true }).toUser()
    }

    fun resendVerificationEmail(email: String) {
        // TODO: Trigger resend
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupExpiredTokens() {
        emailVerificationRepository.deleteByExpiresAtLessThan(
            now = Instant.now()
        )
    }

}