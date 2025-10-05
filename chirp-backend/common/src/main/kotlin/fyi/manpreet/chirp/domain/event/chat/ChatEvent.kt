package fyi.manpreet.chirp.domain.event.chat

import fyi.manpreet.chirp.domain.event.ChirpEvent
import fyi.manpreet.chirp.domain.type.ChatContent
import fyi.manpreet.chirp.domain.type.ChatId
import fyi.manpreet.chirp.domain.type.UserId
import fyi.manpreet.chirp.domain.type.Username
import java.time.Instant
import java.util.UUID

sealed class ChatEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val exchange: String = ChatEventConstants.CHAT_EXCHANGE,
    override val occurredAt: Instant = Instant.now(),
): ChirpEvent {

    data class NewMessage(
        val senderId: UserId,
        val senderUsername: Username,
        val recipientIds: Set<UserId>,
        val chatId: ChatId,
        val message: ChatContent,
        override val eventKey: String = ChatEventConstants.CHAT_NEW_MESSAGE
    ): ChatEvent(), ChirpEvent
}