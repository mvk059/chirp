package fyi.manpreet.chirp.api.controller

import fyi.manpreet.chirp.api.dto.AddParticipantToChatDto
import fyi.manpreet.chirp.api.dto.ChatDto
import fyi.manpreet.chirp.api.dto.ChatMessageDto
import fyi.manpreet.chirp.api.dto.CreateChatRequest
import fyi.manpreet.chirp.api.mappers.toChatDto
import fyi.manpreet.chirp.api.mappers.toChatMessageDto
import fyi.manpreet.chirp.service.ChatService
import fyi.manpreet.chirp.api.util.requestUserId
import fyi.manpreet.chirp.domain.type.ChatId
import fyi.manpreet.chirp.domain.type.UserId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService,
) {

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
    }

    @PostMapping
    fun createChat(
        @Valid @RequestBody body: CreateChatRequest,
    ): ChatDto {
        return chatService.createChat(
            creatorId = requestUserId,
            otherParticipantUserIds = body.otherParticipantUserIds.map { UUID.fromString(it) }.toSet()
        ).toChatDto()
    }

    @PostMapping("/{chatId}/add")
    fun addChatParticipants(
        @PathVariable chatId: ChatId,
        @Valid @RequestBody body: AddParticipantToChatDto
    ): ChatDto {
        return chatService.addParticipantsToChat(
            requestUserId = requestUserId,
            chatId = chatId,
            userIds = body.userIds.toSet()
        ).toChatDto()
    }

    @DeleteMapping("/{chatId}/leave")
    fun leaveChat(
        @PathVariable chatId: ChatId
    ) {
        chatService.removeParticipantFromChat(
            chatId = chatId,
            userId = requestUserId
        )
    }

    @GetMapping("/{chatId}/messages")
    fun getMessagesForChat(
        @PathVariable("chatId") chatId: ChatId,
        @PathVariable("before", required = false) before: Instant? = null,
        @PathVariable("pageSize", required = false) pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<ChatMessageDto> {
       return chatService.getChatMessages(chatId, before, pageSize).map { it.toChatMessageDto() }
    }

    @GetMapping("/{chatId}")
    fun getChat(
        @PathVariable("chatId") chatId: ChatId,
    ): ChatDto {
        return chatService.getChatById(
            chatId = chatId,
            requestUserId = requestUserId
        )?.toChatDto() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @GetMapping
    fun getChatsForUser(): List<ChatDto> {
        return chatService.findChatsByUser(userId = requestUserId).map { it.toChatDto() }
    }
}
