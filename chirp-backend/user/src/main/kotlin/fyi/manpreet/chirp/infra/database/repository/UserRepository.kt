package fyi.manpreet.chirp.infra.database.repository

import fyi.manpreet.chirp.data.model.Email
import fyi.manpreet.chirp.data.model.UserId
import fyi.manpreet.chirp.data.model.Username
import fyi.manpreet.chirp.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, UserId> {
    fun findByEmail(email: String): UserEntity?

    fun findByEmailOrUsername(email: Email, username: Username): UserEntity?

}
