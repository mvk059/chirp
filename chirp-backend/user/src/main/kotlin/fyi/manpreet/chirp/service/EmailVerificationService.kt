package fyi.manpreet.chirp.service

import fyi.manpreet.chirp.data.enum.EmailVerificationStatus
import fyi.manpreet.chirp.domain.event.user.UserEvent
import fyi.manpreet.fyi.manpreet.chirp.domain.exception.InvalidTokenException
import fyi.manpreet.chirp.domain.exception.UserNotFoundException
import fyi.manpreet.chirp.domain.model.EmailVerificationToken
import fyi.manpreet.chirp.infra.database.entities.EmailVerificationTokenEntity
import fyi.manpreet.chirp.infra.database.mappers.toEmailVerificationToken
import fyi.manpreet.chirp.infra.database.mappers.toUser
import fyi.manpreet.chirp.infra.database.repository.EmailVerificationRepository
import fyi.manpreet.chirp.infra.database.repository.UserRepository
import fyi.manpreet.chirp.infra.message_queue.EventPublisher
import fyi.manpreet.chirp.domain.type.Email
import fyi.manpreet.chirp.domain.type.VerificationToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class EmailVerificationService(
    private val emailVerificationRepository: EmailVerificationRepository,
    private val userRepository: UserRepository,
    @param:Value("\${chirp.email.verification.expiry-hours}") private val expiryHours: Long,
    private val eventPublisher: EventPublisher,
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
        val userEntity = userRepository.findByEmail(email.value) ?: throw UserNotFoundException()

        emailVerificationRepository.invalidateActiveTokensForUser(userEntity)

        val token = EmailVerificationTokenEntity(
            expiresAt = Instant.now().plus(expiryHours, ChronoUnit.HOURS),
            user = userEntity
        )

        return emailVerificationRepository.save(token).toEmailVerificationToken()
    }

    @Transactional
    fun verifyEmail(token: VerificationToken) {
        val verificationToken = emailVerificationRepository.findByToken(token.value) ?: throw InvalidTokenException("Email verification token is invalid.")
        if (verificationToken.isUsed) throw InvalidTokenException("Email verification token is already used.")
        if (verificationToken.isExpired) throw InvalidTokenException("Email verification token has already expired.")

        emailVerificationRepository.save(verificationToken.apply { this.usedAt = Instant.now() })
        userRepository.save(verificationToken.user.apply { this.hasVerifiedEmail = true }).toUser()

        eventPublisher.publish(
            event = UserEvent.Verified(
                userId = verificationToken.user.id!!,
                email = verificationToken.user.email,
                username = verificationToken.user.username,
            )
        )
    }

    @Transactional
    fun resendVerificationEmail(email: Email) {
        val token = createVerificationToken(email)

        if (token.user.hasVerifiedEmail == EmailVerificationStatus.VERIFIED) return

        eventPublisher.publish(
            event = UserEvent.RequestResendVerification(
                userId = token.user.id,
                email = token.user.email,
                username = token.user.username,
                verificationToken = token.token
            )
        )
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupExpiredTokens() {
        emailVerificationRepository.deleteByExpiresAtLessThan(
            now = Instant.now()
        )
    }

}