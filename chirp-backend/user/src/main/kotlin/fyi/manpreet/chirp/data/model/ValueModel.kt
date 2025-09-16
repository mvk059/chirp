package fyi.manpreet.chirp.data.model

import java.util.*

@JvmInline
value class UserId(val value: UUID)

@JvmInline
value class Username(val value: String)

@JvmInline
value class Email(val value: String)

@JvmInline
value class RawPassword(val value: String)

@JvmInline
value class HashedPassword(val value: String)