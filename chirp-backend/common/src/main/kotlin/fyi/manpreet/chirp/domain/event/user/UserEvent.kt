package fyi.manpreet.chirp.domain.event.user

import fyi.manpreet.chirp.domain.event.ChirpEvent
import fyi.manpreet.fyi.manpreet.chirp.domain.type.Email
import fyi.manpreet.fyi.manpreet.chirp.domain.type.PasswordResetToken
import fyi.manpreet.fyi.manpreet.chirp.domain.type.UserId
import fyi.manpreet.fyi.manpreet.chirp.domain.type.Username
import fyi.manpreet.fyi.manpreet.chirp.domain.type.VerificationToken
import java.time.Instant
import java.util.UUID

sealed class UserEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val exchange: String = UserEventConstants.USER_EXCHANGE,
    override val occurredAt: Instant = Instant.now(),
): ChirpEvent {

    data class Created(
        val userId: UserId,
        val email: Email,
        val username: Username,
        val verificationToken: VerificationToken,
        override val eventKey: String = UserEventConstants.USER_CREATED_KEY
    ): UserEvent(), ChirpEvent

    data class Verified(
        val userId: UserId,
        val email: Email,
        val username: Username,
        override val eventKey: String = UserEventConstants.USER_VERIFIED
    ): UserEvent(), ChirpEvent

    data class RequestResendVerification(
        val userId: UserId,
        val email: Email,
        val username: Username,
        val verificationToken: VerificationToken,
        override val eventKey: String = UserEventConstants.USER_REQUEST_RESEND_VERIFICATION
    ): UserEvent(), ChirpEvent

    data class RequestResetPassword(
        val userId: UserId,
        val email: Email,
        val username: Username,
        val passwordResetToken: PasswordResetToken,
        val expiresInMinutes: Long,
        override val eventKey: String = UserEventConstants.USER_REQUEST_RESET_PASSWORD
    ): UserEvent(), ChirpEvent
}