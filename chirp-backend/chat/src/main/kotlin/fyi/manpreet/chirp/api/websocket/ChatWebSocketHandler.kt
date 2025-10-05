package fyi.manpreet.chirp.api.websocket

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import fyi.manpreet.chirp.api.dto.ws.ChatParticipantsChangedDto
import fyi.manpreet.chirp.api.dto.ws.DeleteMessageDto
import fyi.manpreet.chirp.api.dto.ws.ErrorDto
import fyi.manpreet.chirp.api.dto.ws.IncomingWebSocketMessage
import fyi.manpreet.chirp.api.dto.ws.IncomingWebSocketMessageType
import fyi.manpreet.chirp.api.dto.ws.OutgoingWebSocketMessage
import fyi.manpreet.chirp.api.dto.ws.OutgoingWebSocketMessageType
import fyi.manpreet.chirp.api.dto.ws.SendMessageDto
import fyi.manpreet.chirp.api.mappers.toChatMessageDto
import fyi.manpreet.chirp.domain.event.ChatParticipantLeftEvent
import fyi.manpreet.chirp.domain.event.ChatParticipantsJoinedEvent
import fyi.manpreet.chirp.domain.event.MessageDeletedEvent
import fyi.manpreet.chirp.domain.type.ChatContent
import fyi.manpreet.chirp.domain.type.ChatId
import fyi.manpreet.chirp.domain.type.UserId
import fyi.manpreet.chirp.service.ChatMessageService
import fyi.manpreet.chirp.service.ChatService
import fyi.manpreet.chirp.service.JwtService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.PingMessage
import org.springframework.web.socket.PongMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import sun.jvm.hotspot.HelloWorld.e
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.jvm.java

private data class UserSession(
    val userId: UserId,
    val session: WebSocketSession,
    val lastPongTimestamp: Long = System.currentTimeMillis(),
)

@Component
class ChatWebSocketHandler(
    private val chatMessageService: ChatMessageService,
    private val objectMapper: ObjectMapper,
    private val chatService: ChatService,
    private val jwtService: JwtService,
) : TextWebSocketHandler() {

    companion object {
        private const val PING_INTERVAL_MS = 30_000L
        private const val PONG_TIMEOUT_MS = 60_000L
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    private val connectionLock = ReentrantReadWriteLock()

    private val sessions = ConcurrentHashMap<String, UserSession>()
    private val userToSessions = ConcurrentHashMap<UserId, MutableSet<String>>()
    private val userChatIds = ConcurrentHashMap<UserId, MutableSet<ChatId>>()
    private val chatToSessions = ConcurrentHashMap<ChatId, MutableSet<String>>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val authHeader = session
            .handshakeHeaders
            .getFirst(HttpHeaders.AUTHORIZATION)
            ?: run {
                logger.warn("Session ${session.id} was closed due to missing Authorization header")
                session.close(CloseStatus.SERVER_ERROR.withReason("Authentication failed"))
                return
            }

        val userId = jwtService.getUserIdFromToken(authHeader)
        val userSession = UserSession(userId = userId, session = session)

        connectionLock.write {
            sessions[session.id] = userSession

            userToSessions.compute(userId) { _, existingSessions ->
                (existingSessions ?: mutableSetOf()).also { it.add(session.id) }
            }

            val chatIds = userChatIds.computeIfAbsent(userId) {
                val chatIds = chatService.findChatsByUser(userId).map { it.chatId }
                ConcurrentHashMap.newKeySet<ChatId>().apply { addAll(chatIds) }
            }
            chatIds.forEach { chatId ->
                chatToSessions.compute(chatId) { _, existingSessions -> (existingSessions ?: mutableSetOf()).also { it.add(session.id) } }
            }
        }

        logger.info("Websocket connection established for user $userId")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        connectionLock.write {
            sessions.remove(session.id)?.let { userSession ->
                val userId = userSession.userId

                userToSessions.compute(userId) { _, sessions ->
                    sessions
                        ?.apply { remove(session.id) }
                        ?.takeIf { it.isNotEmpty() }
                }

                userChatIds[userId]?.forEach { chatId ->
                    chatToSessions.compute(chatId) { _, sessions ->
                        sessions
                            ?.apply { remove(session.id) }
                            ?.takeIf { it.isNotEmpty() }
                    }
                }

                logger.info("Websocket session closed for user $userId")
            }
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error("Transport error for session ${session.id}", exception)
        session.close(CloseStatus.SERVER_ERROR.withReason("Transport error"))
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.debug("Received message ${message.payload}")

        val userSession = connectionLock.read { sessions[session.id] ?: return }

        try {
            val websocketMessage = objectMapper.readValue(message.payload, IncomingWebSocketMessage::class.java)
            if (websocketMessage.type == IncomingWebSocketMessageType.NEW_MESSAGE) {
                val dto = objectMapper.readValue(websocketMessage.payload, SendMessageDto::class.java)
                logger.debug("Sending chat message from {}", userSession.userId)
                handleSendMessage(dto, userSession.userId)
            }
        } catch (exception: JsonMappingException) {
            logger.warn("Could not parse message ${message.payload}", exception)
            sendError(
                session = userSession.session,
                error = ErrorDto(
                    code = "INVALID_JSON",
                    message = "Incoming JSON or UUID is invalid"
                )
            )
        }
    }

    override fun handlePongMessage(session: WebSocketSession, message: PongMessage) {
        connectionLock.write {
            sessions.compute(session.id) { _, userSession ->
                userSession?.copy(
                    lastPongTimestamp = System.currentTimeMillis()
                )
            }
        }
        logger.debug("Received pong from ${session.id}")
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onDeleteMessage(event: MessageDeletedEvent) {
        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.MESSAGE_DELETED,
                payload = objectMapper.writeValueAsString(
                    DeleteMessageDto(
                        chatId = event.chatId,
                        messageId = event.messageId
                    )
                )
            )
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onJoinChat(event: ChatParticipantsJoinedEvent) {
        connectionLock.write {
            event.userIds.forEach { userId ->
                userChatIds.compute(userId) { _, chatIds ->
                    (chatIds ?: mutableSetOf()).apply {
                        add(event.chatId)
                    }
                }

                userToSessions[userId]?.forEach { sessionId ->
                    chatToSessions.compute(event.chatId) { _, sessions ->
                        (sessions ?: mutableSetOf()).apply { add(sessionId) }
                    }
                }
            }
        }

        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.CHAT_PARTICIPANTS_CHANGED,
                payload = objectMapper.writeValueAsString(
                    ChatParticipantsChangedDto(
                        chatId = event.chatId
                    )
                )
            )
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onLeftChat(event: ChatParticipantLeftEvent) {
        connectionLock.write {
            userChatIds.compute(event.userId) { _, chatIds ->
                chatIds
                    ?.apply { remove(event.chatId) }
                    ?.takeIf { it.isNotEmpty() }
            }

            userToSessions[event.userId]?.forEach { sessionId ->
                chatToSessions.compute(event.chatId) { _, sessions ->
                    sessions
                        ?.apply { remove(sessionId) }
                        ?.takeIf { it.isNotEmpty() }
                }
            }
        }

        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.CHAT_PARTICIPANTS_CHANGED,
                payload = objectMapper.writeValueAsString(
                    ChatParticipantsChangedDto(
                        chatId = event.chatId
                    )
                )
            )
        )
    }

    @Scheduled(fixedDelay = PING_INTERVAL_MS)
    fun pingClients() {
        val currentTime = System.currentTimeMillis()
        val sessionsToClose = mutableListOf<String>()

        val sessionsSnapshot = connectionLock.read { sessions.toMap() }

        sessionsSnapshot.forEach { (sessionId, userSession) ->
            try {
                if(userSession.session.isOpen) {
                    val lastPong = userSession.lastPongTimestamp
                    if(currentTime - lastPong > PONG_TIMEOUT_MS) {
                        logger.warn("Session $sessionId has timed out, closing connection.")
                        sessionsToClose.add(sessionId)
                        return@forEach
                    }

                    userSession.session.sendMessage(PingMessage())
                    logger.debug("Sent ping to {}", userSession.userId)
                }
            } catch(e: Exception) {
                logger.error("Could not ping session $sessionId", e)
                sessionsToClose.add(sessionId)
            }
        }

        sessionsToClose.forEach { sessionId ->
            connectionLock.read {
                sessions[sessionId]?.session?.let { session ->
                    try {
                        session.close(CloseStatus.GOING_AWAY.withReason("Ping timeout"))
                    } catch(e: Exception) {
                        logger.error("Couldn't close sessions for session ${session.id} with $e")
                    }
                }
            }
        }
    }

    private fun handleSendMessage(dto: SendMessageDto, senderId: UserId) {
        val userChatIds = connectionLock.read { this@ChatWebSocketHandler.userChatIds[senderId] ?: return }
        if (dto.chatId !in userChatIds) return

        val savedMessage = chatMessageService.sendMessage(
            chatId = dto.chatId,
            senderId = senderId,
            content = ChatContent(dto.content),
            messageId = dto.messageId,
        )

        broadcastToChat(
            chatId = dto.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.NEW_MESSAGE,
                payload = objectMapper.writeValueAsString(
                    savedMessage.toChatMessageDto()
                )
            )
        )
    }

    private fun broadcastToChat(
        chatId: ChatId,
        message: OutgoingWebSocketMessage,
    ) {

        val chatSessions = connectionLock.read { chatToSessions[chatId]?.toList() ?: emptyList() }

        chatSessions.forEach { sessionId ->
            val userSession = connectionLock.read { sessions[sessionId] } ?: return@forEach

            sendToUser(
                userId = userSession.userId,
                message = message
            )
        }
    }

    private fun sendToUser(userId: UserId, message: OutgoingWebSocketMessage) {
        val userSessions = connectionLock.read { userToSessions[userId] ?: emptySet() }

        userSessions.forEach { sessionId ->
            val userSession = connectionLock.read { sessions[sessionId] ?: return@forEach }

            if(userSession.session.isOpen) {
                try {
                    val messageJson = objectMapper.writeValueAsString(message)
                    userSession.session.sendMessage(TextMessage(messageJson))
                    logger.debug("Sent message to user {}: {}", userId, messageJson)
                } catch(e: Exception) {
                    logger.error("Error while sending message to $userId", e)
                }
            }
        }
    }

    private fun sendError(
        session: WebSocketSession,
        error: ErrorDto
    ) {
        val webSocketMessage = objectMapper.writeValueAsString(
            OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.ERROR,
                payload = objectMapper.writeValueAsString(error)
            )
        )

        try {
            session.sendMessage(TextMessage(webSocketMessage))
        } catch(e: Exception) {
            logger.warn("Couldn't send error message", e)
        }
    }
}
