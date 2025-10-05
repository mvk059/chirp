package fyi.manpreet.chirp.domain.exception

import fyi.manpreet.chirp.domain.type.ChatMessageId

class MessageNotFoundException(id: ChatMessageId): RuntimeException("Message with ID $id not found")