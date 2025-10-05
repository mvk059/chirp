package fyi.manpreet.chirp.infra.database.messaging

import fyi.manpreet.chirp.domain.event.user.UserEvent
import fyi.manpreet.chirp.infra.message_queue.MessageQueues
import fyi.manpreet.chirp.service.ChatParticipantService
import fyi.manpreet.chirp.domain.model.ChatParticipant
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class ChatUserEventListener(
    private val chatParticipantService: ChatParticipantService,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = [MessageQueues.CHAT_USER_EVENTS])
    fun handleUserEvent(event: UserEvent) {
        logger.info("Received user event: {}", event)
        when (event) {
            is UserEvent.Verified -> {
                chatParticipantService.createChatParticipant(
                    chatParticipant = ChatParticipant(
                        userId = event.userId,
                        username = event.username,
                        email = event.email,
                        profilePictureUrl = null,
                    )
                )
            }
            else -> Unit
        }
    }
}