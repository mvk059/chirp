package fyi.manpreet.chirp.service

import fyi.manpreet.chirp.infra.database.mapper.toChatParticipant
import fyi.manpreet.chirp.infra.database.mapper.toChatParticipantEntity
import fyi.manpreet.chirp.infra.database.repository.ChatParticipantRepository
import fyi.manpreet.chirp.domain.model.ChatParticipant
import fyi.manpreet.chirp.domain.type.UserId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ChatParticipantService(
    private val chatParticipantRepository: ChatParticipantRepository,
) {

    fun createChatParticipant(
        chatParticipant: ChatParticipant,
    ) {
        chatParticipantRepository.save(chatParticipant.toChatParticipantEntity())
    }

    fun findChatParticipantById(userId: UserId): ChatParticipant? = chatParticipantRepository.findByIdOrNull(userId)?.toChatParticipant()

    fun findChatParticipantByEmailOrUsername(query: String) = chatParticipantRepository.findByEmailOrUsername(query.lowercase().trim())?.toChatParticipant()

}
