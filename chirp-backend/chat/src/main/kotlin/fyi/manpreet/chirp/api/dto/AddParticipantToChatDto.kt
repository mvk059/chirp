package fyi.manpreet.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import fyi.manpreet.chirp.domain.type.UserId
import jakarta.validation.constraints.Size

data class AddParticipantToChatDto(
    @field:Size(min = 1)
    @param:JsonProperty("userIds")
    val userIds: List<UserId>
)