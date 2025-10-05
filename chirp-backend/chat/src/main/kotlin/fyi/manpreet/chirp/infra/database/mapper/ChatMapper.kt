package fyi.manpreet.chirp.infra.database.mapper

import fyi.manpreet.chirp.domain.model.Chat
import fyi.manpreet.chirp.domain.model.ChatMessage
import fyi.manpreet.chirp.domain.model.ChatParticipant
import fyi.manpreet.chirp.infra.database.entity.ChatEntity
import fyi.manpreet.chirp.infra.database.entity.ChatMessageEntity
import fyi.manpreet.chirp.infra.database.entity.ChatParticipantEntity

fun ChatEntity.toChat(lastMessage: ChatMessage? = null): Chat {
    return Chat(
        chatId = id!!,
        participants = participants.map { it.toChatParticipant() }.toSet(),
        creator = creator.toChatParticipant(),
        lastActivityAt = lastMessage?.createdAt ?: createdAt,
        createdAt = createdAt,
        lastMessage = lastMessage
    )
}

fun ChatParticipantEntity.toChatParticipant(): ChatParticipant {
    return ChatParticipant(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}

fun ChatParticipant.toChatParticipantEntity(): ChatParticipantEntity {
    return ChatParticipantEntity(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}

fun ChatMessageEntity.toChatMessage(): ChatMessage {
    return ChatMessage(
        chatMessageId = id!!,
        chatId = chatId,
        sender = sender.toChatParticipant(),
        chatContent = chatContent,
        createdAt = createdAt
    )
}
