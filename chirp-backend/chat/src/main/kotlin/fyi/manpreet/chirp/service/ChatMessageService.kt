package fyi.manpreet.chirp.service

import fyi.manpreet.chirp.domain.event.MessageDeletedEvent
import fyi.manpreet.chirp.domain.event.chat.ChatEvent
import fyi.manpreet.chirp.domain.exception.ChatNotFoundException
import fyi.manpreet.chirp.domain.exception.ChatParticipantNotFoundException
import fyi.manpreet.chirp.domain.exception.ForbiddenException
import fyi.manpreet.chirp.domain.exception.MessageNotFoundException
import fyi.manpreet.chirp.domain.model.ChatMessage
import fyi.manpreet.chirp.domain.type.ChatContent
import fyi.manpreet.chirp.domain.type.ChatId
import fyi.manpreet.chirp.domain.type.ChatMessageId
import fyi.manpreet.chirp.domain.type.UserId
import fyi.manpreet.chirp.infra.database.entity.ChatMessageEntity
import fyi.manpreet.chirp.infra.database.mapper.toChatMessage
import fyi.manpreet.chirp.infra.database.repository.ChatMessageRepository
import fyi.manpreet.chirp.infra.database.repository.ChatParticipantRepository
import fyi.manpreet.chirp.infra.database.repository.ChatRepository
import fyi.manpreet.chirp.infra.message_queue.EventPublisher
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatMessageService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val eventPublisher: EventPublisher,
) {

    @Transactional
    @CacheEvict(
        value = ["messages"],
        key = "#chatId",
    )
    fun sendMessage(
        chatId: ChatId,
        senderId: UserId,
        content: ChatContent,
        messageId: ChatMessageId? = null,
    ): ChatMessage {
        val chat = chatRepository.findChatById(chatId, senderId)
            ?: throw ChatNotFoundException()
        val sender = chatParticipantRepository.findByIdOrNull(senderId)
            ?: throw ChatParticipantNotFoundException(senderId)

        val savedMessage = chatMessageRepository.saveAndFlush(
            ChatMessageEntity(
                id = messageId,
                chatContent = content, // TODO Trim
                chatId = chatId,
                chat = chat,
                sender = sender
            )
        )

        eventPublisher.publish(
            event = ChatEvent.NewMessage(
                senderId = sender.userId,
                senderUsername = sender.username,
                recipientIds = chat.participants.map { it.userId }.toSet(),
                chatId = chatId,
                message = savedMessage.chatContent
            )
        )

        return savedMessage.toChatMessage()
    }

    @Transactional
    fun deleteMessage(
        messageId: ChatMessageId,
        requestUserId: UserId,
    ) {
        val message = chatMessageRepository.findByIdOrNull(messageId)
            ?: throw MessageNotFoundException(messageId)

        if (message.sender.userId != requestUserId) {
            throw ForbiddenException()
        }

        chatMessageRepository.delete(message)

        applicationEventPublisher.publishEvent(
            MessageDeletedEvent(
                chatId = message.chatId,
                messageId = messageId
            )
        )

        evictMessagesCache(message.chatId)
    }

    @CacheEvict(
        value = ["messages"],
        key = "#chatId",
    )
    fun evictMessagesCache(chatId: ChatId) {
        // NO-OP: Let Spring handle the cache evict
    }
}