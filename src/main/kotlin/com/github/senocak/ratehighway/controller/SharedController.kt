package com.github.senocak.ratehighway.controller

import com.github.senocak.ratehighway.domain.dto.ExceptionDto
import com.github.senocak.ratehighway.service.MessageSourceService
import com.github.senocak.ratehighway.util.OAuth2Services
import com.github.senocak.ratehighway.util.OmaErrorMessageType
import com.github.senocak.ratehighway.util.RoleName
import com.github.senocak.ratehighway.util.fromProperties
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import io.swagger.v3.oas.annotations.tags.Tag as TagSwagger

@Validated
@RestController
@TagSwagger(name = "Shared", description = "Shared API")
@RequestMapping(BaseController.v1SharedUrl)
class SharedController(
    private val messageSourceService: MessageSourceService,
    private val environment: Environment
): BaseController() {

    @GetMapping
    fun ping(request: HttpServletRequest): Map<String, Any> =
        mapOf(
            "ip" to (request.getHeader("X-FORWARDED-FOR") ?: request.remoteAddr),
            "appVersion" to "appVersion".fromProperties(),
            "locale" to LocaleContextHolder.getLocale(),
            "message" to messageSourceService.get(code = "welcome"),
            "activeProfiles" to environment.activeProfiles,
            "defaultProfiles" to environment.defaultProfiles
        )

    @GetMapping("/enums")
    @Operation(
        summary = "Enums Endpoint",
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = Map::class))]),
            ApiResponse(responseCode = "400", description = "Bad credentials",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class))]),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class))])
        ]
    )
    fun enums(): Map<String, Any> {
        val omaErrorMessageType: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
        for (it: OmaErrorMessageType in OmaErrorMessageType.entries)
            omaErrorMessageType[it.name] =  mutableMapOf(it.messageId to it.text)
        return mapOf(
            "RoleName" to RoleName.entries.map { "$it" },
            "OmaErrorMessageType" to omaErrorMessageType,
            "OAuth2Services" to OAuth2Services.entries.map { it.toString() },
        )
    }
}