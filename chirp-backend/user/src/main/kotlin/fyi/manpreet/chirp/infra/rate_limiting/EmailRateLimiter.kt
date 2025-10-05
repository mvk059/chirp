package fyi.manpreet.chirp.infra.rate_limiting

import fyi.manpreet.chirp.domain.type.Email
import fyi.manpreet.chirp.domain.exception.RateLimitException
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component

@Component
class EmailRateLimiter(
    private val redisTemplate: StringRedisTemplate,
) {

    companion object {
        private const val EMAIL_RATE_LIMIT_PREFIX = "rate_limit:email"
        private const val EMAIL_ATTEMPT_COUNT_PREFIX = "email_attempt_count"
    }

    @Value("classpath:email_rate_limit.lua")
    lateinit var rateLimitResource: Resource

    private val rateLimitScript by lazy {
        val script = rateLimitResource.inputStream.use {
            it.readBytes().decodeToString()
        }
        @Suppress("UNCHECKED_CAST")
        DefaultRedisScript(script, List::class.java as Class<List<Long>>)
    }

    /**
     * Runs [action] under rate-limiting for [email].
     * Throws RateLimitException if throttled.
     * Returns [T] which is the result of [action].
     */
    fun <T> withRateLimit(email: Email, action: () -> T): T {
        val normalizedEmail = email.value.lowercase().trim()
        val rateLimitKey = "$EMAIL_RATE_LIMIT_PREFIX:$normalizedEmail"
        val attemptCountKey = "$EMAIL_ATTEMPT_COUNT_PREFIX:$normalizedEmail"

        val result = redisTemplate.execute(rateLimitScript, listOf(rateLimitKey, attemptCountKey)) ?: throw IllegalStateException("Rate limit script returned null")

        // Script contract: { -1, ttl } or { newCount, 0 }
        val attemptCount = (result[0] as Number).toLong()
        val ttl = (result[1] as Number).toLong()

        if (attemptCount == -1L) {
            throw RateLimitException(resetsInSeconds = ttl)
        }

        return action()
    }
}