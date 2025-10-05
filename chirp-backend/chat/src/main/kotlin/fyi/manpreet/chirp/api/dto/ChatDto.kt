package fyi.manpreet.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import fyi.manpreet.chirp.domain.type.ChatId
import java.time.Instant

data class ChatDto(
    @field:JsonProperty
    @get:JsonIgnore
    val id: ChatId,
    val participants: List<ChatParticipantDto>,
    val lastActivityAt: Instant,
    val lastMessage: ChatMessageDto?,
    val creator: ChatParticipantDto,
)