package fyi.manpreet.chirp.api.dto

import fyi.manpreet.chirp.data.model.Email
import fyi.manpreet.chirp.data.model.RawPassword

data class LoginRequest(
    val email: Email,
    val password: RawPassword,
)