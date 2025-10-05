package fyi.manpreet.chirp.api.dto.ws

enum class IncomingWebSocketMessageType {
    NEW_MESSAGE,
}

enum class OutgoingWebSocketMessageType {
    NEW_MESSAGE,
    MESSAGE_DELETED,
    PROFILE_PICTURE_UPDATED,
    CHAT_PARTICIPANTS_CHANGED,
    ERROR,
}

data class IncomingWebSocketMessage(
    val type: IncomingWebSocketMessageType,
    val payload: Payload,
)

data class OutgoingWebSocketMessage(
    val type: OutgoingWebSocketMessageType,
    val payload: Payload,
)

typealias Payload = String
