package fyi.manpreet.chirp.api.util

import fyi.manpreet.chirp.data.model.UserId
import fyi.manpreet.chirp.domain.exception.UnauthorizedException
import org.springframework.security.core.context.SecurityContextHolder

val requestUserId: UserId
    get() = SecurityContextHolder.getContext().authentication?.principal as? UserId
        ?: throw UnauthorizedException()