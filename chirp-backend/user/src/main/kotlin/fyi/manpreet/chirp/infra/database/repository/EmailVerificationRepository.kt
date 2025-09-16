package fyi.manpreet.chirp.infra.database.repository

import fyi.manpreet.chirp.infra.database.entities.EmailVerificationTokenEntity
import fyi.manpreet.chirp.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface EmailVerificationRepository : JpaRepository<EmailVerificationTokenEntity, Long> {

    fun findByToken(token: String): EmailVerificationTokenEntity?

    fun deleteByExpiresAtLessThan(now: Instant)

    fun findByUserAndUsedAtIsNull(user: UserEntity): List<EmailVerificationTokenEntity>
}
