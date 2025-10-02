package fyi.manpreet.chirp.api.controller

import fyi.manpreet.chirp.api.dto.AuthenticatedUserDto
import fyi.manpreet.chirp.api.dto.ChangePasswordRequest
import fyi.manpreet.chirp.api.dto.EmailRequest
import fyi.manpreet.chirp.api.dto.LoginRequest
import fyi.manpreet.chirp.api.dto.RefreshRequest
import fyi.manpreet.chirp.api.dto.RegisterRequest
import fyi.manpreet.chirp.api.dto.ResetPasswordRequest
import fyi.manpreet.chirp.api.dto.UserDto
import fyi.manpreet.chirp.api.mapper.toAuthenticatedUserDto
import fyi.manpreet.chirp.api.mapper.toUserDto
import fyi.manpreet.chirp.api.util.requestUserId
import fyi.manpreet.fyi.manpreet.chirp.domain.type.Email
import fyi.manpreet.fyi.manpreet.chirp.domain.type.RawPassword
import fyi.manpreet.fyi.manpreet.chirp.domain.type.Username
import fyi.manpreet.chirp.domain.user.RefreshToken
import fyi.manpreet.chirp.infra.config.IpRateLimit
import fyi.manpreet.chirp.infra.rate_limiting.EmailRateLimiter
import fyi.manpreet.chirp.service.AuthService
import fyi.manpreet.chirp.service.EmailVerificationService
import fyi.manpreet.chirp.service.PasswordResetService
import fyi.manpreet.fyi.manpreet.chirp.domain.type.VerificationToken
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService,
    private val passwordResetService: PasswordResetService,
    private val emailRateLimiter: EmailRateLimiter,
) {

    @PostMapping("/register")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS
    )
    fun register(
        @Valid @RequestBody body: RegisterRequest,
    ): UserDto {
        return authService.register(
            username = Username(body.username.trim()),
            email = Email(body.email.lowercase().trim()),
            rawPassword = RawPassword(body.password),
        ).toUserDto()
    }

    @PostMapping("/login")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS
    )
    fun login(
        @RequestBody body: LoginRequest,
    ): AuthenticatedUserDto {
        return authService.login(
            email = Email(body.email),
            password = RawPassword(body.password)
        ).toAuthenticatedUserDto()
    }

    @PostMapping("/refresh")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS
    )
    fun refresh(
        @RequestBody body: RefreshRequest,
    ): AuthenticatedUserDto {
        return authService
            .refresh(RefreshToken(body.refreshToken))
            .toAuthenticatedUserDto()
    }

    @PostMapping("/logout")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS
    )
    fun logout(
        @RequestBody body: RefreshRequest,
    ) {
        authService.logout(RefreshToken(body.refreshToken))
    }

    @PostMapping("/resend-verification")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS
    )
    fun resendVerification(
        @Valid @RequestBody body: EmailRequest,
    ) {
        val email = Email(body.email.lowercase().trim())
        emailRateLimiter.withRateLimit(email = email) {
            emailVerificationService.resendVerificationEmail(email)
        }
    }

    @GetMapping("/verify")
    fun verifyEmail(
        @RequestParam token: String,
    ) {
        emailVerificationService.verifyEmail(VerificationToken(token))
    }

    @PostMapping("/forgot-password")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS
    )
    fun forgotPassword(
        @Valid @RequestBody body: EmailRequest,
    ) {
        passwordResetService.requestPasswordReset(Email(body.email.trim()))
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @Valid @RequestBody body: ResetPasswordRequest,
    ) {
        passwordResetService.resetPassword(
            token = body.token,
            newPassword = RawPassword(body.newPassword)
        )
    }

    @PostMapping("/change-password")
    fun changePassword(
        @Valid @RequestBody body: ChangePasswordRequest,
    ) {
        passwordResetService.changePassword(
            userId = requestUserId,
            oldPassword = RawPassword(body.oldPassword),
            newPassword = RawPassword(body.newPassword),
        )
    }
}
