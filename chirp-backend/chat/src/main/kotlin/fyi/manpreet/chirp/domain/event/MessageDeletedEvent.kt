package fyi.manpreet.chirp.domain.event

import fyi.manpreet.chirp.domain.type.ChatId
import fyi.manpreet.chirp.domain.type.ChatMessageId

data class MessageDeletedEvent(
    val chatId: ChatId,
    val messageId: ChatMessageId,
)