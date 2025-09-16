package fyi.manpreet.chirp.infra.database.entities

import fyi.manpreet.chirp.infra.security.TokenGenerator
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "email_verification_tokens",
    schema = "user_service"
)
class EmailVerificationTokenEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, unique = true)
    var token: String = TokenGenerator.generateSecureToken(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,

    @Column(nullable = false)
    var expiresAt: Instant,

    @Column
    var usedAt: Instant? = null,

    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
) {

    val isUsed: Boolean
        get() = usedAt != null

    val isExpired: Boolean
        get() = expiresAt.isBefore(Instant.now())
}