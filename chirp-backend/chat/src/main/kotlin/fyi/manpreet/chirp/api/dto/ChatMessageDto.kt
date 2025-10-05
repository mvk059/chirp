package fyi.manpreet.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import fyi.manpreet.chirp.domain.type.ChatContent
import fyi.manpreet.chirp.domain.type.ChatId
import fyi.manpreet.chirp.domain.type.ChatMessageId
import fyi.manpreet.chirp.domain.type.UserId
import java.time.Instant

data class ChatMessageDto(
    @field:JsonProperty
    @get:JsonIgnore
    val id: ChatMessageId,

    @field:JsonProperty
    @get:JsonIgnore
    val chatId: ChatId,

    @field:JsonProperty
    @get:JsonIgnore
    val content: ChatContent,

    val createdAt: Instant,

    @field:JsonProperty
    @get:JsonIgnore
    val senderId: UserId,
)