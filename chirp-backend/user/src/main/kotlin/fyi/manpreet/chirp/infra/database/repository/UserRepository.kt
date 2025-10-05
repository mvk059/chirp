package fyi.manpreet.chirp.infra.database.repository

import fyi.manpreet.chirp.domain.type.Email
import fyi.manpreet.chirp.domain.type.UserId
import fyi.manpreet.chirp.domain.type.Username
import fyi.manpreet.chirp.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<UserEntity, UserId> {
    fun findByEmail(email: String): UserEntity?

    fun findByEmailOrUsername(email: Email, username: Username): UserEntity?

    override fun findById(id: UserId): Optional<UserEntity>

}
