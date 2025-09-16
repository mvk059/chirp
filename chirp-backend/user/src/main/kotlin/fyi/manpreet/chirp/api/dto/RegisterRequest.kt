package fyi.manpreet.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import fyi.manpreet.chirp.api.util.Password
import jakarta.validation.constraints.Email
import org.hibernate.validator.constraints.Length

data class RegisterRequest(
    @field:Email(message = "Must be a valid email address")
    @param:JsonProperty("email")
    val email: String,

    @field:Length(min = 3, max = 20, message = "Username length must be between 3 and 20 characters")
    @param:JsonProperty("username")
    val username: String,

    @field:Password
    @param:JsonProperty("password")
    val password: String
)