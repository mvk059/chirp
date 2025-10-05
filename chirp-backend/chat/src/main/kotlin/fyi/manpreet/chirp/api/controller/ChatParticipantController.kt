package fyi.manpreet.chirp.api.controller

import fyi.manpreet.chirp.api.dto.ChatParticipantDto
import fyi.manpreet.chirp.api.mappers.toChatParticipantDto
import fyi.manpreet.chirp.service.ChatParticipantService
import fyi.manpreet.chirp.api.util.requestUserId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/chat/participants")
class ChatParticipantController(
    private val chatParticipantService: ChatParticipantService,
) {

    @GetMapping
    fun getChatParticipantByUsernameOrEmail(
        @RequestParam(required = false) query: String?
    ): ChatParticipantDto {
        val participant = if (query == null) chatParticipantService.findChatParticipantById(requestUserId)
        else chatParticipantService.findChatParticipantByEmailOrUsername(query)

        return participant?.toChatParticipantDto() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
}
