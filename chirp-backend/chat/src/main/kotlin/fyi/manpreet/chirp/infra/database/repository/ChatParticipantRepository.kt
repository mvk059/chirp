package fyi.manpreet.chirp.infra.database.repository

import fyi.manpreet.chirp.domain.type.UserId
import fyi.manpreet.chirp.infra.database.entity.ChatParticipantEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ChatParticipantRepository: JpaRepository<ChatParticipantEntity, UserId> {
    fun findByUserIdIn(userIds: Set<UserId>): Set<ChatParticipantEntity>

    @Query("""
        SELECT p
        FROM ChatParticipantEntity p
        WHERE LOWER(p.username) = :query OR LOWER(p.email) = :query
    """)
    fun findByEmailOrUsername(query: String): ChatParticipantEntity?
}