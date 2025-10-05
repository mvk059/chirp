package fyi.manpreet.chirp.domain.type

import java.util.UUID

typealias ChatId = UUID

typealias ChatMessageId = UUID

@JvmInline
value class ProfilePictureUrl(val value: String)

@JvmInline
value class ChatContent(val value: String)

