package fyi.manpreet.chirp.domain.exception

import fyi.manpreet.chirp.domain.type.UserId

class ChatParticipantNotFoundException(private val id: UserId): RuntimeException("The chat participant with the ID $id was not found.")