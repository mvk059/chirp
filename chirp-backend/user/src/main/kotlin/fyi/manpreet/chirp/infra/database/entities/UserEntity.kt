package fyi.manpreet.chirp.infra.database.entities

import fyi.manpreet.chirp.domain.type.Email
import fyi.manpreet.chirp.domain.type.HashedPassword
import fyi.manpreet.chirp.domain.type.UserId
import fyi.manpreet.chirp.domain.type.Username
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(
    name = "users",
    schema = "user_service",
    indexes = [
        Index(name = "idx_users_email", columnList = "email"),
        Index(name = "idx_users_username", columnList = "username"),
    ]
)
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UserId? = null,

    @Column(nullable = false, unique = true)
    var email: Email,

    @Column(nullable = false, unique = true)
    var username: Username,

    @Column(nullable = false)
    var hashedPassword: HashedPassword,

    @Column(nullable = false)
    var hasVerifiedEmail: Boolean = false,

    @CreationTimestamp
    var createdAt: Instant = Instant.now(),

    @UpdateTimestamp
    var updatedAt: Instant = Instant.now(),
)