package fyi.manpreet.chirp.domain.event

import fyi.manpreet.chirp.domain.type.ChatId
import fyi.manpreet.chirp.domain.type.UserId

data class ChatParticipantsJoinedEvent(
    val chatId: ChatId,
    val userIds: Set<UserId>
)