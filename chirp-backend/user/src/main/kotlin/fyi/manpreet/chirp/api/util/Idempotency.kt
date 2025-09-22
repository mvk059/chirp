package fyi.manpreet.chirp.api.util

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY_GETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [])
@jakarta.validation.constraints.Pattern(
    regexp = "^[A-Za-z0-9_.-]{1,255}$",
    message = "Idempotency-Key must be 1–255 chars, only letters, digits, '-', '_', '.'"
)
annotation class IdempotencyKey(
    val message: String = "Invalid Idempotency-Key header",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Payload>> = []
)
