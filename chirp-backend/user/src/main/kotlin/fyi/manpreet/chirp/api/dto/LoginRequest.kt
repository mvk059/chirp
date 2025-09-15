package fyi.manpreet.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class LoginRequest(
    @param:JsonProperty("email")
    val email: String,
    @param:JsonProperty("password")
    val password: String,
)