package fyi.manpreet.chirp.api.controller

import fyi.manpreet.chirp.api.dto.AuthenticatedUserDto
import fyi.manpreet.chirp.api.dto.LoginRequest
import fyi.manpreet.chirp.api.dto.RefreshRequest
import fyi.manpreet.chirp.api.dto.RegisterRequest
import fyi.manpreet.chirp.api.dto.UserDto
import fyi.manpreet.chirp.api.mapper.toAuthenticatedUserDto
import fyi.manpreet.chirp.api.mapper.toUserDto
import fyi.manpreet.chirp.data.model.Email
import fyi.manpreet.chirp.data.model.RawPassword
import fyi.manpreet.chirp.data.model.Username
import fyi.manpreet.chirp.domain.user.RefreshToken
import fyi.manpreet.chirp.service.auth.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody body: RegisterRequest
    ): UserDto {
        return authService.register(
            username = Username(body.username),
            email = Email(body.email),
            rawPassword = RawPassword(body.password)
        ).toUserDto()
    }

    @PostMapping("/login")
    fun login(
        @RequestBody body: LoginRequest
    ): AuthenticatedUserDto {
        return authService.login(
            email = Email(body.email),
            password = RawPassword(body.password)
        ).toAuthenticatedUserDto()
    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestBody body: RefreshRequest
    ): AuthenticatedUserDto {
        return authService
            .refresh(RefreshToken(body.refreshToken))
            .toAuthenticatedUserDto()
    }
}