package fyi.manpreet.chirp.infra.database.entity

import fyi.manpreet.chirp.domain.type.Email
import fyi.manpreet.chirp.domain.type.ProfilePictureUrl
import fyi.manpreet.chirp.domain.type.UserId
import fyi.manpreet.chirp.domain.type.Username
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "chat_participants",
    schema = "chat_service",
    indexes = [
        Index(name = "idx_chat_participant_username", columnList = "username"),
        Index(name = "idx_chat_participant_email", columnList = "email"),
    ]
)
class ChatParticipantEntity(

    @Id
    var userId: UserId,

    @Column(nullable = false, unique = true)
    var username: Username,

    @Column(nullable = false, unique = true)
    var email: Email,

    @Column(nullable = true)
    var profilePictureUrl: ProfilePictureUrl? = null,

    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
)