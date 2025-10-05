package fyi.manpreet.chirp.api.util

import fyi.manpreet.chirp.domain.exception.UnauthorizedException
import fyi.manpreet.chirp.domain.type.UserId
import org.springframework.security.core.context.SecurityContextHolder

// Extract user id from the JWT token
val requestUserId: UserId
    get() = SecurityContextHolder.getContext().authentication?.principal as? UserId ?: throw UnauthorizedException()