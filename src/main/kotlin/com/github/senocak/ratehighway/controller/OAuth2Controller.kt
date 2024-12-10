package com.github.senocak.ratehighway.controller

import com.github.senocak.ratehighway.domain.dto.ExceptionDto
import com.github.senocak.ratehighway.domain.dto.UserResponseWrapperDto
import com.github.senocak.ratehighway.domain.dto.oauth2.OAuthTokenResponse
import com.github.senocak.ratehighway.domain.OAuthFacebookUser
import com.github.senocak.ratehighway.domain.OAuthGithubUser
import com.github.senocak.ratehighway.domain.OAuthGoogleUser
import com.github.senocak.ratehighway.domain.OAuthLinkedinUser
import com.github.senocak.ratehighway.domain.OAuthTwitterUser
import com.github.senocak.ratehighway.exception.ServerException
import com.github.senocak.ratehighway.service.oauth2.OAuthFacebookService
import com.github.senocak.ratehighway.service.oauth2.OAuthGithubService
import com.github.senocak.ratehighway.service.oauth2.OAuthGoogleService
import com.github.senocak.ratehighway.service.oauth2.OAuthLinkedinService
import com.github.senocak.ratehighway.service.oauth2.OAuthTwitterService
import com.github.senocak.ratehighway.util.OmaErrorMessageType
import com.github.senocak.ratehighway.util.logger
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.MediaType

@RestController
@RequestMapping("/oauth2")
@Tag(name = "OAuth2", description = "OAuth2 Controller")
class OAuth2Controller(
    private val oAuthGoogleService: OAuthGoogleService,
    private val oAuthGithubService: OAuthGithubService,
    private val oAuthLinkedinService: OAuthLinkedinService,
    private val oAuthFacebookService: OAuthFacebookService,
    private val oAuthTwitterService: OAuthTwitterService,
): BaseController() {
    private val log: Logger by logger()

    @GetMapping("/google/redirect")
    @Operation(summary = "Google redirect endpoint", tags = ["OAuth2"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = Map::class))]),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class))])
        ]
    )
    fun google(request: HttpServletRequest,
        @Parameter(description = "State param") @RequestParam state: String,
        @Parameter(description = "Code param") @RequestParam code: String,
        @Parameter(description = "Scope param") @RequestParam scope: String,
        @Parameter(description = "AuthUser param") @RequestParam authuser: String,
        @Parameter(description = "Hd param") @RequestParam(required = false, defaultValue = "hd") hd: String,
        @Parameter(description = "Prompt param") @RequestParam prompt: String,
    ): Map<String, Any> {
        validateResponse(code = code, state = state)
        log.info("Started processing auth for google. Code: $code, state: $state")
        val oAuthTokenResponse: OAuthTokenResponse = oAuthGoogleService.getToken(code = code)
        var oAuthGoogleUser: OAuthGoogleUser = oAuthGoogleService.getGoogleUserInfo(accessToken = oAuthTokenResponse.access_token!!)

        oAuthGoogleUser = try {
            oAuthGoogleService.getByIdOrThrowException(id = oAuthGoogleUser.id!!)
        } catch (e: ServerException) {
            log.warn("oAuthGoogleUser is saved to db: $oAuthGoogleUser")
            oAuthGoogleService.save(entity = oAuthGoogleUser)
        }
        val oAuthUserResponse: UserResponseWrapperDto = oAuthGoogleService.authenticate(
            request.getHeader("Authorization"), oAuthGoogleUser)

        return mapOf(
            "state" to state,
            "code" to code,
            "scope" to scope,
            "authuser" to authuser,
            "hd" to hd,
            "prompt" to prompt,
            "oAuthTokenResponse" to oAuthTokenResponse,
            "oAuthUserResponse" to oAuthUserResponse
        )
    }

    @GetMapping("/github/redirect")
    @Operation(summary = "Github redirect endpoint", tags = ["OAuth2"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = Map::class))]),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class))])
        ]
    )
    fun github(request: HttpServletRequest,
        @Parameter(description = "Code param") @RequestParam code: String,
        @Parameter(description = "State param") @RequestParam state: String
    ): Map<String, Any> {
        validateResponse(code = code, state = state)
        log.info("Started processing auth for github. Code: $code, state: $state")
        val oAuthTokenResponse: OAuthTokenResponse = oAuthGithubService.getToken(code = code)
        val accessToken: String = oAuthTokenResponse.access_token!!
        var oAuthGithubUser: OAuthGithubUser = oAuthGithubService.getGithubUserInfo(accessToken = accessToken)

        oAuthGithubUser = try {
            oAuthGithubService.getByIdOrThrowException(id = oAuthGithubUser.id!!)
        } catch (e: ServerException) {
            log.warn("oAuthGithubUser is saved to db: $oAuthGithubUser")
            oAuthGithubService.save(entity = oAuthGithubUser)
        }
        val oAuthUserResponse: UserResponseWrapperDto = oAuthGithubService.authenticate(
            jwtToken = request.getHeader("Authorization"), oAuthGoogleUser = oAuthGithubUser)

        log.info("Finished processing auth for github. Response: $oAuthUserResponse")
        return mapOf(
            "state" to state,
            "code" to code,
            "oAuthTokenResponse" to oAuthTokenResponse,
            "oAuthUserResponse" to oAuthUserResponse
        )
    }

    @GetMapping("/linkedin/redirect")
    @Operation(summary = "Linkedin redirect endpoint", tags = ["OAuth2"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = Map::class))]),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class))])
        ]
    )
    fun linkedin(request: HttpServletRequest,
        @Parameter(description = "Error param") @RequestParam(required = false) error: String?,
        @Parameter(description = "Error Description param") @RequestParam(value = "errorDescription",required = false) errorDescription: String?,
        @Parameter(description = "Code param") @RequestParam(required = false) code: String?,
        @Parameter(description = "State param") @RequestParam(required = false) state: String?
    ): Map<String, Any> {
        validateError(error = error, errorMessage = errorDescription)
        validateResponse(code = code, state = state)
        log.info("Started processing auth for linkedin. Code: $code, state: $state")
        val oAuthTokenResponse: OAuthTokenResponse = oAuthLinkedinService.getToken(code = code!!)
        var oAuthLinkedinUser: OAuthLinkedinUser = oAuthLinkedinService.getLinkedinUserInfo(accessToken = oAuthTokenResponse.access_token!!)

        oAuthLinkedinUser = try {
            oAuthLinkedinService.getByIdOrEmailOrThrowException(id = oAuthLinkedinUser.id!!, email = oAuthLinkedinUser.email!!)
        } catch (e: ServerException) {
            log.warn("oAuthLinkedinUser is saved to db: $oAuthLinkedinUser")
            oAuthLinkedinService.save(entity = oAuthLinkedinUser)
        }
        val oAuthUserResponse: UserResponseWrapperDto = oAuthLinkedinService.authenticate(
            jwtToken = request.getHeader("Authorization"), oAuthGoogleUser = oAuthLinkedinUser)

        log.info("Finished processing auth for linkedin")
        return mapOf(
            "state" to state!!,
            "code" to code,
            "oAuthTokenResponse" to oAuthTokenResponse,
            "oAuthUserResponse" to oAuthUserResponse
        )
    }

    @GetMapping("/facebook/redirect")
    @Operation(summary = "Facebook redirect endpoint", tags = ["OAuth2"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = Map::class))]),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class))])
        ]
    )
    fun facebook(request: HttpServletRequest,
        @Parameter(description = "Error param") @RequestParam(value="error_code", required = false) error: String?,
        @Parameter(description = "Error Description param") @RequestParam(value = "errorMessage", required = false) errorMessage: String?,
        @Parameter(description = "Code param") @RequestParam(required = false) code: String?,
        @Parameter(description = "State param") @RequestParam(required = false) state: String?
    ): Map<String, Any> {
        validateError(error = error, errorMessage = errorMessage)
        validateResponse(code = code, state = state)
        log.info("Started processing auth for facebook. Code: $code, state: $state")
        val oAuthTokenResponse: OAuthTokenResponse = oAuthFacebookService.getToken(code = code!!)
        var oAuthFacebookUser: OAuthFacebookUser = oAuthFacebookService.getFacebookUserInfo(accessToken = oAuthTokenResponse.access_token!!)

        oAuthFacebookUser = try {
            oAuthFacebookService.getByIdOrThrowException(id = oAuthFacebookUser.id!!)
        } catch (e: Exception) {
            log.warn("OAuthFacebookUser is saved to db: $oAuthFacebookUser")
            oAuthFacebookService.save(entity = oAuthFacebookUser)
        }
        val oAuthUserResponse: UserResponseWrapperDto = oAuthFacebookService.authenticate(
            jwtToken = request.getHeader("Authorization"), oAuthGoogleUser = oAuthFacebookUser)

        log.info("Finished processing auth for OAuthFacebookUser: $oAuthFacebookUser")
        return mapOf(
            "state" to state!!,
            "code" to code,
            "oAuthTokenResponse" to oAuthTokenResponse,
            "oAuthUserResponse" to oAuthUserResponse
        )
    }

    @GetMapping("/twitter/redirect")
    @Operation(summary = "Twitter redirect endpoint", tags = ["OAuth2"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = Map::class))]),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class))])
        ]
    )
    fun twitter(request: HttpServletRequest,
        @Parameter(description = "State param") @RequestParam(required = false) state: String?,
        @Parameter(description = "Code param") @RequestParam(required = false) code: String?,
        @Parameter(description = "Error param") @RequestParam(value="error_code", required = false) error: String?,
        @Parameter(description = "Error Description param") @RequestParam(value = "errorMessage", required = false) errorMessage: String?,
    ): Map<String, Any> {
        validateError(error = error, errorMessage = errorMessage)
        validateResponse(code = code, state = state)
        log.info("Started processing auth for facebook. Code: $code, state: $state")
        val oAuthTokenResponse: OAuthTokenResponse = oAuthTwitterService.getToken(code = code!!)
        var oAuthTwitterUser: OAuthTwitterUser = oAuthTwitterService.getUserInfo(accessToken = oAuthTokenResponse.access_token!!)

        oAuthTwitterUser = try {
            oAuthTwitterService.getByIdOrThrowException(id = oAuthTwitterUser.id!!)
        } catch (e: Exception) {
            log.warn("OAuthTwitterUser is saved to db: $oAuthTwitterUser")
            oAuthTwitterService.save(entity = oAuthTwitterUser)
        }
        val oAuthUserResponse: UserResponseWrapperDto = oAuthTwitterService.authenticate(
            jwtToken = request.getHeader("Authorization"), oAuthGoogleUser = oAuthTwitterUser)

        log.info("Finished processing auth for OAuthTwitterUser: $oAuthTwitterUser")
        return mapOf(
            "state" to state!!,
            "code" to code,
            "oAuthTokenResponse" to oAuthTokenResponse,
            "oAuthUserResponse" to oAuthUserResponse
        )
    }

    /**
     * Validates the response received from the server by checking if the required parameters code and state are present.
     * If either of them is missing, it logs an error and throws a custom ServerException indicating the mandatory input missing.
     *
     * @param code The code received from the server.
     * @param state The state received from the server.
     * @throws ServerException If either code or state is null.
     */
    private fun validateResponse(code: String?, state: String?) {
        if (code == null || state == null) {
            log.error("code:$code or state:$state is null!")
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.MANDATORY_INPUT_MISSING,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf(code, state))
        }
    }

    /**
     * Validates the response received from the server by checking if the optional error parameters are present.
     * If either of them is present, it logs an error and throws a custom ServerException indicating that error occurred.
     *
     * @param error The error received from the server.
     * @param errorMessage The errorMessage received from the server.
     * @throws ServerException If either error or errorMessage is null.
     */
    private fun validateError(error: String?, errorMessage: String?) {
        if(error != null || errorMessage != null) {
            log.info("Error occurred for oAuthFacebook, Error: $error, ErrorMessage: $errorMessage")
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                variables = arrayOf(error, errorMessage), statusCode = HttpStatus.BAD_REQUEST)
        }
    }
}