package com.github.senocak.ratehighway.exception

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.github.senocak.ratehighway.domain.dto.ExceptionDto
import com.github.senocak.ratehighway.util.OmaErrorMessageType
import com.github.senocak.ratehighway.util.logger
import org.slf4j.Logger
import org.springframework.beans.TypeMismatchException
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.NoHandlerFoundException
import java.lang.reflect.UndeclaredThrowableException
import java.security.InvalidParameterException
import jakarta.validation.ConstraintViolationException

@RestControllerAdvice
class RestExceptionHandler {
    private val log: Logger by logger()

    @ExceptionHandler(
        BadCredentialsException::class,
        ConstraintViolationException::class,
        InvalidParameterException::class,
        TypeMismatchException::class,
        MissingPathVariableException::class,
        HttpMessageNotReadableException::class,
        MissingServletRequestParameterException::class,
        MismatchedInputException::class,
        UndeclaredThrowableException::class,
        IllegalArgumentException::class,
        InvalidDataAccessApiUsageException::class
    )
    fun handleBadRequestException(ex: Exception): ResponseEntity<ExceptionDto> =
        generateResponseEntity(HttpStatus.BAD_REQUEST, OmaErrorMessageType.BASIC_INVALID_INPUT, arrayOf(ex.message))

    @ExceptionHandler(
        AccessDeniedException::class,
        AuthenticationCredentialsNotFoundException::class,
        com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException::class
    )
    fun handleUnAuthorized(ex: Exception): ResponseEntity<ExceptionDto> =
        generateResponseEntity(HttpStatus.UNAUTHORIZED, OmaErrorMessageType.UNAUTHORIZED, arrayOf(ex.message))

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupported(ex: HttpRequestMethodNotSupportedException): ResponseEntity<ExceptionDto> =
        generateResponseEntity(HttpStatus.METHOD_NOT_ALLOWED, OmaErrorMessageType.EXTRA_INPUT_NOT_ALLOWED, arrayOf(ex.message))

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleHttpMediaTypeNotSupported(ex: HttpMediaTypeNotSupportedException): ResponseEntity<ExceptionDto> =
        generateResponseEntity(HttpStatus.UNSUPPORTED_MEDIA_TYPE, OmaErrorMessageType.BASIC_INVALID_INPUT, arrayOf(ex.message))

    @ExceptionHandler(
        NoHandlerFoundException::class,
        UsernameNotFoundException::class,
        NotFoundException::class
    )
    fun handleNoHandlerFoundException(ex: Exception): ResponseEntity<ExceptionDto> =
        generateResponseEntity(HttpStatus.NOT_FOUND, OmaErrorMessageType.NOT_FOUND, arrayOf(ex.message))

    @ExceptionHandler(ServerException::class)
    fun handleServerException(ex: ServerException): ResponseEntity<ExceptionDto> =
        generateResponseEntity(ex.statusCode, ex.omaErrorMessageType, ex.variables)

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxSizeException(ex: MaxUploadSizeExceededException): ResponseEntity<ExceptionDto> =
        generateResponseEntity(HttpStatus.EXPECTATION_FAILED, OmaErrorMessageType.BASIC_INVALID_INPUT, arrayOf("File too large!"))

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<ExceptionDto> =
        generateResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, OmaErrorMessageType.GENERIC_SERVICE_ERROR, arrayOf(ex.message))

    /**
     * @param httpStatus -- returned code
     * @return -- returned body
     */
    private fun generateResponseEntity(httpStatus: HttpStatus, omaErrorMessageType: OmaErrorMessageType, variables: Array<String?>): ResponseEntity<ExceptionDto> {
        log.error("Exception is handled. HttpStatus: $httpStatus, OmaErrorMessageType: $omaErrorMessageType, variables: ${variables.joinToString(separator = ",")}")
        val exceptionDto = ExceptionDto()
            .also { ed: ExceptionDto ->
                ed.statusCode = httpStatus.value()
                ed.error = ExceptionDto.OmaErrorMessageTypeDto(id = omaErrorMessageType.messageId,
                    text = omaErrorMessageType.text)
                ed.variables = variables
            }
        return ResponseEntity.status(httpStatus).body(exceptionDto)
    }
}
