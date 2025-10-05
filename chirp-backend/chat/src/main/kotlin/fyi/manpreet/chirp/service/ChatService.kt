package fyi.manpreet.chirp.service

import fyi.manpreet.chirp.api.dto.ChatMessageDto
import fyi.manpreet.chirp.api.mappers.toChatMessageDto
import fyi.manpreet.chirp.domain.event.ChatParticipantLeftEvent
import fyi.manpreet.chirp.domain.event.ChatParticipantsJoinedEvent
import fyi.manpreet.chirp.domain.exception.ChatNotFoundException
import fyi.manpreet.chirp.domain.exception.ChatParticipantNotFoundException
import fyi.manpreet.chirp.domain.exception.ForbiddenException
import fyi.manpreet.chirp.domain.exception.InvalidChatSizeException
import fyi.manpreet.chirp.domain.model.Chat
import fyi.manpreet.chirp.domain.model.ChatMessage
import fyi.manpreet.chirp.domain.type.ChatId
import fyi.manpreet.chirp.domain.type.UserId
import fyi.manpreet.chirp.infra.database.entity.ChatEntity
import fyi.manpreet.chirp.infra.database.mapper.toChat
import fyi.manpreet.chirp.infra.database.mapper.toChatMessage
import fyi.manpreet.chirp.infra.database.repository.ChatMessageRepository
import fyi.manpreet.chirp.infra.database.repository.ChatParticipantRepository
import fyi.manpreet.chirp.infra.database.repository.ChatRepository
import fyi.manpreet.chirp.util.ChatConstants
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    @Transactional
    fun createChat(
        creatorId: UserId,
        otherParticipantUserIds: Set<UserId>,
    ): Chat {
        val allParticipants = chatParticipantRepository.findByUserIdIn(otherParticipantUserIds + creatorId)

        val creator = allParticipants.firstOrNull { it.userId == creatorId } ?: throw ChatParticipantNotFoundException(creatorId)
        if (allParticipants.size !in ChatConstants.MIN_CHAT_PARTICIPANTS..ChatConstants.MAX_CHAT_PARTICIPANTS) throw InvalidChatSizeException()

        return chatRepository.save(
            ChatEntity(
                creator = creator,
                participants = allParticipants.toMutableSet(),
            )
        ).toChat(lastMessage = null)
    }

    @Transactional
    fun addParticipantsToChat(
        requestUserId: UserId,
        chatId: ChatId,
        userIds: Set<UserId>,
    ): Chat {

        val chat = chatRepository.findChatById(chatId, requestUserId) ?: throw ChatNotFoundException()

        val isRequestingUserInChat = chat.participants.any { it.userId == requestUserId }
        if (isRequestingUserInChat.not()) throw ForbiddenException()

        if (chat.participants.size + userIds.size > ChatConstants.MAX_CHAT_PARTICIPANTS) throw InvalidChatSizeException()

        val users = userIds.map { userId ->
            chatParticipantRepository.findByIdOrNull(userId) ?: throw ChatParticipantNotFoundException(userId)
        }
        val lastMessage = chatId.lastMessageForChat()

        val updatedChat = chatRepository.save(
            chat.apply {
                this.participants = (this.participants + users).toMutableSet()
            }
        ).toChat(lastMessage = lastMessage)

        applicationEventPublisher.publishEvent(
            ChatParticipantsJoinedEvent(
                chatId = chatId,
                userIds = userIds
            )
        )

        return updatedChat
    }

    @Transactional
    fun removeParticipantFromChat(
        chatId: ChatId,
        userId: UserId,
    ) {

        val chat = chatRepository.findByIdOrNull(chatId) ?: throw ChatNotFoundException()
        val participant = chat.participants.find { it.userId == userId } ?: throw ChatParticipantNotFoundException(userId)

        val newParticipantsSize = chat.participants.size - 1
        if (newParticipantsSize <= 0) {
            chatRepository.delete(chat)
            return
        }

        chatRepository.save(
            chat.apply {
                this.participants = this.participants.minus(participant).toMutableSet()
            }
        )

        applicationEventPublisher.publishEvent(
            ChatParticipantLeftEvent(
                chatId = chatId,
                userId = userId
            )
        )
    }

    @Cacheable(
        value = ["messages"],
        key = "#chatId",
        condition = "#before == null && #pageSize <= 50",
        sync = true
    )
    fun getChatMessages(
        chatId: ChatId,
        before: Instant?,
        pageSize: Int,
    ): List<ChatMessage> {
        return chatMessageRepository
            .findByChatIdBefore(
                chatId = chatId,
                before = before ?: Instant.now(),
                pageable = PageRequest.of(0, pageSize)
            )
            .content
            .asReversed()
            .map { it.toChatMessage() }
    }

    fun getChatById(
        chatId: ChatId,
        requestUserId: UserId
    ): Chat? {
        return chatRepository
            .findChatById(chatId, requestUserId)
            ?.toChat(chatId.lastMessageForChat())
    }

    fun findChatsByUser(userId: UserId): List<Chat> {
        val chatEntities = chatRepository.findAllByUserId(userId)
        val chatIds = chatEntities.mapNotNull { it.id }
        val latestMessages = chatMessageRepository
            .findLatestMessagesByChatIds(chatIds.toSet())
            .associateBy { it.chatId }

        return chatEntities
            .map { it.toChat(lastMessage = latestMessages[it.id]?.toChatMessage()) }
            .sortedByDescending { it.lastActivityAt }
    }


    private fun ChatId.lastMessageForChat(): ChatMessage? =
        chatMessageRepository
            .findLatestMessagesByChatIds(setOf(this))
            .firstOrNull()
            ?.toChatMessage()

}