package fyi.manpreet.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email

data class EmailRequest(
    @param:JsonProperty("email")
    @field:Email
    val email: String
)