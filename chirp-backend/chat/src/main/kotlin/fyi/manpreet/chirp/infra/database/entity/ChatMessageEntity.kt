package fyi.manpreet.chirp.infra.database.entity

import fyi.manpreet.chirp.domain.type.ChatContent
import fyi.manpreet.chirp.domain.type.ChatId
import fyi.manpreet.chirp.domain.type.ChatMessageId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "chat_messages",
    schema = "chat_service",
    indexes = [
        Index(name = "idx_chat_message_chat_id_created_at", columnList = "chat_id,created_at DESC")
    ]
)
class ChatMessageEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: ChatMessageId? = null,

    @Column(name = "chat_id", nullable = false, updatable = false)
    var chatId: ChatId,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false, insertable = false, updatable = false)
    var chat: ChatEntity? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    var sender: ChatParticipantEntity,

    @Column(nullable = false)
    var chatContent: ChatContent,

    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
)