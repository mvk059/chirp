package fyi.manpreet.chirp.api.exception_handler

import fyi.manpreet.chirp.domain.exception.EmailNotVerifiedException
import fyi.manpreet.chirp.domain.exception.InvalidCredentialsException
import fyi.manpreet.chirp.domain.exception.InvalidTokenException
import fyi.manpreet.chirp.domain.exception.SamePasswordException
import fyi.manpreet.chirp.domain.exception.UserAlreadyExistsException
import fyi.manpreet.chirp.domain.exception.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AuthExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun onUserAlreadyExists(exception: UserAlreadyExistsException) =
        mapOf(
            "code" to "USER_EXISTS",
            "message" to exception.message,
        )

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onUserNotFound(
        e: UserNotFoundException,
    ) = mapOf(
        "code" to "USER_NOT_FOUND",
        "message" to e.message
    )

    @ExceptionHandler(InvalidCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onInvalidCredentials(
        e: InvalidCredentialsException,
    ) = mapOf(
        "code" to "INVALID_CREDENTIALS",
        "message" to e.message
    )

    @ExceptionHandler(InvalidTokenException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onInvalidToken(exception: InvalidTokenException) = mapOf(
        "code" to "INVALID_TOKEN",
        "message" to exception.message
    )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun onValidationException(exception: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = exception.bindingResult.allErrors.map { it.defaultMessage ?: "Invalid value" }
        return ResponseEntity.badRequest().body(
            mapOf(
                "code" to "VALIDATION_ERROR",
                "errors" to errors,
            )
        )
    }

    @ExceptionHandler(EmailNotVerifiedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onEmailNotVerified(exception: EmailNotVerifiedException) = mapOf(
        "code" to "EMAIL_NOT_VERIFIED",
        "message" to exception.message
    )

    @ExceptionHandler(SamePasswordException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun onSamePassword(
        e: SamePasswordException
    ) = mapOf(
        "code" to "SAME_PASSWORD",
        "message" to e.message
    )
}
