package fyi.manpreet.chirp.api.dto.ws

import fyi.manpreet.chirp.domain.type.ChatId
import fyi.manpreet.chirp.domain.type.ChatMessageId

data class SendMessageDto(
    val chatId: ChatId,
    val content: String,
    val messageId: ChatMessageId? = null
)