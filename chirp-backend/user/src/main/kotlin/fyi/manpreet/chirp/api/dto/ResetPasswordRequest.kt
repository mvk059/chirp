package fyi.manpreet.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import fyi.manpreet.chirp.api.util.Password
import jakarta.validation.constraints.NotBlank

data class ResetPasswordRequest(
    @field:NotBlank
    @param:JsonProperty("token")
    val token: String,
    @field:Password
    @param:JsonProperty("newPassword")
    val newPassword: String
)