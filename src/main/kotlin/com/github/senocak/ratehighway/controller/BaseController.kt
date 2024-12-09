package com.github.senocak.ratehighway.controller

import com.github.senocak.ratehighway.exception.ServerException
import com.github.senocak.ratehighway.util.OmaErrorMessageType
import com.github.senocak.ratehighway.util.logger
import org.slf4j.Logger
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.util.PathMatcher
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.servlet.HandlerMapping
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import jakarta.servlet.http.HttpServletRequest

@CrossOrigin(origins = ["*"], maxAge = 3600)
abstract class BaseController(private val pathMatcher: PathMatcher? = null) {
    private val log: Logger by logger()

    /**
     * @param resultOfValidation -- error registration capabilities
     * @throws ServerException -- throws ServerException when validation fails
     */
    fun validate(resultOfValidation: BindingResult) {
        if (resultOfValidation.hasErrors()) {
            val variables: Array<String?> = resultOfValidation.fieldErrors
                .map { fieldError: FieldError? -> "${fieldError?.field}: ${fieldError?.defaultMessage}" }
                .toList().toTypedArray()
            log.error("Validation is failed. variables: ${variables.asList()}")
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.JSON_SCHEMA_VALIDATOR,
                statusCode = HttpStatus.BAD_REQUEST, variables = variables
            )
        }
    }

    /**
     * @param allFields -- Array of all fields
     * @return -- mutableList of all fields
     */
    fun findPrivateFields(allFields: Array<Field>): MutableList<String> =
        allFields
            .filter { f: Field -> f.modifiers == Modifier.PRIVATE }
            .map { it.name.lowercase() }
            .toMutableList()

    /**
     * Extract wildcard path from request
     * @param request HttpServletRequest for getting attributes
     * @return String value
     */
    protected fun extractWildcardPath(request: HttpServletRequest): String {
        if (pathMatcher == null) {
            val variables: Array<String?> = arrayOf("pathMatcher is not initialized")
            log.error("Error occurred for extracting wildcard path: ${variables.asList()}")
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.INTERNAL_SERVER_ERROR, variables = variables)
        }
        val patternAttribute: String = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString()
        val mappingAttribute: String = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString()
        return pathMatcher.extractPathWithinPattern(patternAttribute, mappingAttribute)
    }

    /**
     * Creates an HTTP header containing a user ID.
     * @param userId The user ID to include in the header.
     * @return The HttpHeaders object containing the user ID header.
     */
    protected fun userIdHeader(userId: String): HttpHeaders =
        HttpHeaders()
            .also { hh: HttpHeaders ->
                hh.add("userId", userId)
            }

    companion object {
        const val v1 = "/api/v1"
        const val v1AuthUrl = "$v1/auth"
        const val v1UserUrl = "$v1/user"
        const val v1SharedUrl = "$v1/shared"
    }
}