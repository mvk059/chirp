package fyi.manpreet.chirp.domain.model

import fyi.manpreet.chirp.domain.type.ChatId
import java.time.Instant

data class Chat(
    val chatId: ChatId,
    val participants: Set<ChatParticipant>,
    val lastMessage: ChatMessage?,
    val creator: ChatParticipant,
    val lastActivityAt: Instant,
    val createdAt: Instant,
)
