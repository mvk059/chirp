package fyi.manpreet.chirp.api.mappers

import fyi.manpreet.chirp.api.dto.ChatDto
import fyi.manpreet.chirp.api.dto.ChatMessageDto
import fyi.manpreet.chirp.api.dto.ChatParticipantDto
import fyi.manpreet.chirp.domain.model.Chat
import fyi.manpreet.chirp.domain.model.ChatMessage
import fyi.manpreet.chirp.domain.model.ChatParticipant


fun Chat.toChatDto(): ChatDto {
    return ChatDto(
        id = chatId,
        participants = participants.map { it.toChatParticipantDto() },
        lastActivityAt = lastActivityAt,
        lastMessage = lastMessage?.toChatMessageDto(),
        creator = creator.toChatParticipantDto()
    )
}

fun ChatMessage.toChatMessageDto(): ChatMessageDto {
    return ChatMessageDto(
        id = chatMessageId,
        chatId = chatId,
        content = chatContent,
        createdAt = createdAt,
        senderId = sender.userId
    )
}

fun ChatParticipant.toChatParticipantDto(): ChatParticipantDto {
    return ChatParticipantDto(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}