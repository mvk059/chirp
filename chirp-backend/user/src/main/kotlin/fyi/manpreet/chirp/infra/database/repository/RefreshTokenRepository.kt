package fyi.manpreet.chirp.infra.database.repository

import fyi.manpreet.fyi.manpreet.chirp.domain.type.UserId
import fyi.manpreet.chirp.infra.database.entities.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository: JpaRepository<RefreshTokenEntity, Long> {

    fun findByUserIdAndHashedToken(userId: UserId, hashedToken: String): RefreshTokenEntity?

    fun deleteByUserIdAndHashedToken(userId: UserId, hashedToken: String)

    fun deleteByUserId(userId: UserId)

}
