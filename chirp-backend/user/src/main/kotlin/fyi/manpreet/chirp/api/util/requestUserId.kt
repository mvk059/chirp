package fyi.manpreet.chirp.api.util

import fyi.manpreet.fyi.manpreet.chirp.domain.type.UserId
import fyi.manpreet.chirp.domain.exception.UnauthorizedException
import org.springframework.security.core.context.SecurityContextHolder

val requestUserId: UserId
    get() = SecurityContextHolder.getContext().authentication?.principal as? UserId
        ?: throw UnauthorizedException()