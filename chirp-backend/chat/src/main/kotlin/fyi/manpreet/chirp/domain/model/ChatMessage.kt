package fyi.manpreet.chirp.domain.model

import fyi.manpreet.chirp.domain.type.ChatContent
import fyi.manpreet.chirp.domain.type.ChatId
import fyi.manpreet.chirp.domain.type.ChatMessageId
import java.time.Instant

data class ChatMessage(
    val chatMessageId: ChatMessageId,
    val chatId: ChatId,
    val sender: ChatParticipant,
    val chatContent: ChatContent,
    val createdAt: Instant,
)
