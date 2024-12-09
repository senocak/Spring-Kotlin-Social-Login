package com.github.senocak.ratehighway.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.ratehighway.domain.dto.ExceptionDto
import com.github.senocak.ratehighway.exception.RestExceptionHandler
import com.github.senocak.ratehighway.util.logger
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.springframework.http.ResponseEntity
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class JwtAuthenticationEntryPoint(val objectMapper: ObjectMapper) : AuthenticationEntryPoint {
    private val log: Logger by logger()

    @Throws(IOException::class)
    override fun commence(request: HttpServletRequest, response: HttpServletResponse, ex: AuthenticationException) {
        log.error("Responding with unauthorized error. Exception: ${ExceptionUtils.getStackTrace(ex)}")
        val responseEntity: ResponseEntity<ExceptionDto> = RestExceptionHandler()
            .handleUnAuthorized(RuntimeException(ex.message))
        response.writer.write(objectMapper.writeValueAsString(responseEntity.body))
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
    }
}