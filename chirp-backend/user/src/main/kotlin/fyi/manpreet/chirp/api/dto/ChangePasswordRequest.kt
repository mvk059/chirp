package fyi.manpreet.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import fyi.manpreet.chirp.api.util.Password
import jakarta.validation.constraints.NotBlank

data class ChangePasswordRequest(
    @param:JsonProperty("oldPassword")
    @field:NotBlank
    val oldPassword: String,
    @param:JsonProperty("newPassword")
    @field:Password
    val newPassword: String
)