package fyi.manpreet.chirp.infra.security

import fyi.manpreet.chirp.domain.type.HashedPassword
import fyi.manpreet.chirp.domain.type.RawPassword
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordEncoder {

    private val bcrypt = BCryptPasswordEncoder()

    fun encode(password: RawPassword): Result<HashedPassword> {

        val hashedPassword = bcrypt.encode(password.value) ?: return Result.failure(Exception("Failed to encode password"))
        return Result.success(HashedPassword(hashedPassword))
    }

    fun matches(rawPassword: RawPassword, hashedPassword: HashedPassword): Boolean =
        bcrypt.matches(rawPassword.value, hashedPassword.value)


}