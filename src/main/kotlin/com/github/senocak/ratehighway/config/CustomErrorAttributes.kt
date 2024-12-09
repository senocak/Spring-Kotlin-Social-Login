package com.github.senocak.ratehighway.config

import com.github.senocak.ratehighway.domain.dto.ExceptionDto
import com.github.senocak.ratehighway.util.OmaErrorMessageType
import com.github.senocak.ratehighway.util.logger
import jakarta.servlet.RequestDispatcher
import org.slf4j.Logger
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.WebRequest

@Configuration
@Profile("!integration-test")
class CustomErrorAttributes : DefaultErrorAttributes() {
    private val log: Logger by logger()

    override fun getErrorAttributes(webRequest: WebRequest, options: ErrorAttributeOptions): Map<String, Any> {
        val errorAttributes: Map<String, Any> = super.getErrorAttributes(webRequest, options)
        val errorMessage: Any? = webRequest.getAttribute(RequestDispatcher.ERROR_MESSAGE, RequestAttributes.SCOPE_REQUEST)
        val exceptionDto = ExceptionDto()
        if (errorMessage != null) {
            val omaErrorMessageType = OmaErrorMessageType.NOT_FOUND
            exceptionDto.statusCode = errorAttributes["status"] as Int

            val arrayOfVariables: MutableList<String> = mutableListOf(errorAttributes["error"].toString())
            if (errorAttributes["message"] != null) {
                arrayOfVariables.add(errorAttributes["message"].toString())
            }
            exceptionDto.variables = arrayOfVariables.toTypedArray()

            exceptionDto.error = ExceptionDto.OmaErrorMessageTypeDto(omaErrorMessageType.messageId, omaErrorMessageType.text)
        }
        val map: MutableMap<String, Any> = HashMap()
        map["exception"] = exceptionDto
        log.debug("Exception occurred in DefaultErrorAttributes: $map")
        return map
    }
}