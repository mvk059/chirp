package fyi.manpreet.chirp.data.model

import java.util.UUID

@JvmInline
value class UserId(val id: UUID)

@JvmInline
value class Username(val username: String)

@JvmInline
value class Email(val email: String)

@JvmInline
value class HashedPassword(val hash: String)