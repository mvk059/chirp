package fyi.manpreet.chirp.service.auth

import fyi.manpreet.chirp.data.model.Email
import fyi.manpreet.chirp.data.model.RawPassword
import fyi.manpreet.chirp.data.model.Username
import fyi.manpreet.chirp.domain.exception.PasswordEncodeException
import fyi.manpreet.chirp.domain.exception.UserAlreadyExistsException
import fyi.manpreet.chirp.domain.user.User
import fyi.manpreet.chirp.infra.database.entities.UserEntity
import fyi.manpreet.chirp.infra.database.mappers.toUser
import fyi.manpreet.chirp.infra.database.repository.UserRepository
import fyi.manpreet.chirp.infra.security.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    fun register(username: Username, email: Email, rawPassword: RawPassword): User {
        val username = Username(username.value.trim())
        val email = Email(email.value.trim())
        val user = userRepository.findByEmailOrUsername(
            email = email,
            username = username
        )
        if (user != null) throw UserAlreadyExistsException()

        val hashedPassword = passwordEncoder.encode(rawPassword).getOrElse { throw PasswordEncodeException(it) }
        val savedUser = userRepository.save(
            UserEntity(email = email, username = username, hashedPassword = hashedPassword)
        ).toUser()

        return savedUser
    }
}
