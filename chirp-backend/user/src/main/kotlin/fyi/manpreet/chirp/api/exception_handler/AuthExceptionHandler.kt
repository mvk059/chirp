package fyi.manpreet.chirp.api.exception_handler

import fyi.manpreet.chirp.domain.exception.UserAlreadyExistsException
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
}
