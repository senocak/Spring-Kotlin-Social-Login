package com.github.senocak.ratehighway.util

import com.github.senocak.ratehighway.domain.dto.RegisterRequest
import com.github.senocak.ratehighway.domain.dto.UpdateUserDto
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.slf4j.Logger
import org.springframework.beans.BeanWrapperImpl
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PasswordMatchesValidator::class])
annotation class PasswordMatches(
    val message: String = "{validation.constraints.Size.PasswordMatches}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = []
)
internal class PasswordMatchesValidator : ConstraintValidator<PasswordMatches, Any> {
    private val log: Logger by logger()

    override fun initialize(passwordMatches: PasswordMatches) {
        log.info("PasswordMatchesValidator initialized")
    }

    override fun isValid(obj: Any, context: ConstraintValidatorContext): Boolean =
        when (obj) {
            is RegisterRequest, is UpdateUserDto -> {
                val wrapper = BeanWrapperImpl(obj)
                val password: Any? = wrapper.getPropertyValue("password")
                val passwordConfirmation: Any? = wrapper.getPropertyValue("passwordConfirmation")
                val valid: Boolean = password == null && passwordConfirmation == null || password != null
                        && password == passwordConfirmation
                if (!valid) {
                    context.disableDefaultConstraintViolation()
                    context.buildConstraintViolationWithTemplate(context.defaultConstraintMessageTemplate)
                        .addConstraintViolation()
                }
                valid
            }
            else -> false
        }
}

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [EmailValidator::class])
annotation class ValidEmail (
    val message: String = "{validation.constraints.Size.ValidEmail}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = []
)
internal class EmailValidator : ConstraintValidator<ValidEmail?, String?> {
    private val log: Logger by logger()

    override fun initialize(constraintAnnotation: ValidEmail?) {
        log.info("EmailValidator initialized")
    }

    override fun isValid(email: String?, context: ConstraintValidatorContext): Boolean =
        when (email) {
            null -> false
            else -> {
                val pattern: Pattern = Pattern.compile("^[_A-Za-z0-9-+]" +
                        "(.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(.[A-Za-z0-9]+)*" + "(.[A-Za-z]{2,})$")
                val matcher: Matcher = pattern.matcher(email)
                val matches: Boolean = matcher.matches()
                log.info("Is Email:$email valid: $matches")
                matches
            }
        }
}
