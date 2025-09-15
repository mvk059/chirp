package fyi.manpreet.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class RefreshRequest(
    @param:JsonProperty("refreshToken")
    val refreshToken: String
)