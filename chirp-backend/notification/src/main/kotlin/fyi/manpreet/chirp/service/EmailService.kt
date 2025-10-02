package fyi.manpreet.chirp.service


import fyi.manpreet.fyi.manpreet.chirp.domain.type.Email
import fyi.manpreet.fyi.manpreet.chirp.domain.type.PasswordResetToken
import fyi.manpreet.fyi.manpreet.chirp.domain.type.UserId
import fyi.manpreet.fyi.manpreet.chirp.domain.type.Username
import fyi.manpreet.fyi.manpreet.chirp.domain.type.VerificationToken
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.time.Duration

@Service
class EmailService(
    private val javaMailSender: JavaMailSender,
    private val templateService: EmailTemplateService,
    @param:Value("\${chirp.email.from}") private val emailFrom: String,
    @param:Value("\${chirp.email.url}") private val baseUrl: String,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun sendVerificationEmail(
        email: Email,
        username: Username,
        userId: UserId,
        token: VerificationToken
    ) {
        logger.info("Sending verification email for user $userId")

        val verificationUrl = UriComponentsBuilder
            .fromUriString("$baseUrl/api/auth/verify")
            .queryParam("token", token.value)
            .build()
            .toUriString()

        val htmlContent = templateService.processTemplate(
            templateName = "emails/account-verification",
            variables = mapOf(
                "username" to username,
                "verificationUrl" to verificationUrl
            )
        )

        sendHtmlEmail(
            to = email.value,
            subject = "Verify your Chirp account",
            html = htmlContent
        )
    }

    fun sendPasswordResetEmail(
        email: Email,
        username: Username,
        userId: UserId,
        token: PasswordResetToken,
        expiresIn: Duration
    ) {
        logger.info("Sending password reset email for user $userId")

        val resetPasswordUrl = UriComponentsBuilder
            .fromUriString("$baseUrl/api/auth/reset-password")
            .queryParam("token", token.value)
            .build()
            .toUriString()

        val htmlContent = templateService.processTemplate(
            templateName = "emails/reset-password",
            variables = mapOf(
                "username" to username,
                "resetPasswordUrl" to resetPasswordUrl,
                "expiresInMinutes" to expiresIn.toMinutes()
            )
        )

        sendHtmlEmail(
            to = email.value,
            subject = "Reset your Chirp password",
            html = htmlContent
        )
    }

    private fun sendHtmlEmail(
        to: String,
        subject: String,
        html: String
    ) {
        val message = javaMailSender.createMimeMessage()
        MimeMessageHelper(message, true, "UTF-8").apply {
            setFrom(emailFrom)
            setTo(to)
            setSubject(subject)
            setText(html, true)
        }

        try {
            javaMailSender.send(message)
        } catch(e: MailException) {
            logger.error("Could not send email", e)
        }
    }
}