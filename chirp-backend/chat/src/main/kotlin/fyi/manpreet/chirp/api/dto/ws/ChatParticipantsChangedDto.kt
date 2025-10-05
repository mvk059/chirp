package fyi.manpreet.chirp.api.dto.ws

import fyi.manpreet.chirp.domain.type.ChatId

data class ChatParticipantsChangedDto(
    val chatId: ChatId
)