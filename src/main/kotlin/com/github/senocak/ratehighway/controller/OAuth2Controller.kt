package com.github.senocak.ratehighway.controller

import com.github.senocak.ratehighway.domain.OAuthDropboxUser
import com.github.senocak.ratehighway.domain.dto.ExceptionDto
import com.github.senocak.ratehighway.domain.dto.UserResponseWrapperDto
import com.github.senocak.ratehighway.domain.OAuthFacebookUser
import com.github.senocak.ratehighway.domain.OAuthGithubUser
import com.github.senocak.ratehighway.domain.OAuthGoogleUser
import com.github.senocak.ratehighway.domain.OAuthLinkedinUser
import com.github.senocak.ratehighway.domain.OAuthSlackUser
import com.github.senocak.ratehighway.domain.OAuthSpotifyUser
import com.github.senocak.ratehighway.domain.OAuthTwitchUser
import com.github.senocak.ratehighway.domain.OAuthTwitterUser
import com.github.senocak.ratehighway.domain.dto.OAuthTokenResponse
import com.github.senocak.ratehighway.exception.ServerException
import com.github.senocak.ratehighway.service.oauth2.OAuthDropboxService
import com.github.senocak.ratehighway.service.oauth2.OAuthFacebookService
import com.github.senocak.ratehighway.service.oauth2.OAuthGithubService
import com.github.senocak.ratehighway.service.oauth2.OAuthGoogleService
import com.github.senocak.ratehighway.service.oauth2.OAuthLinkedinService
import com.github.senocak.ratehighway.service.oauth2.OAuthSlackService
import com.github.senocak.ratehighway.service.oauth2.OAuthSpotifyService
import com.github.senocak.ratehighway.service.oauth2.OAuthTwitchService
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
    private val oAuthSpotifyService: OAuthSpotifyService,
    private val oAuthTwitchService: OAuthTwitchService,
    private val oAuthSlackService: OAuthSlackService,
    private val oAuthDropboxService: OAuthDropboxService,
): BaseController() {
    private val log: Logger by logger()

    @GetMapping("/google")
    fun googleLink(): String = oAuthGoogleService.link

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

    @GetMapping("/github")
    fun githubLink(): String = oAuthGithubService.link

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
        @Parameter(description = "State param") @RequestParam(required = false) state: String?
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
            "code" to code,
            "oAuthTokenResponse" to oAuthTokenResponse,
            "oAuthUserResponse" to oAuthUserResponse
        )
    }

    @GetMapping("/linkedin")
    fun linkedinLink(): String = oAuthLinkedinService.link

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

    @GetMapping("/facebook")
    fun facebookLink(): String = oAuthFacebookService.link

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

    @GetMapping("/twitter")
    fun twitterLink(): String = oAuthTwitterService.link

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

    @GetMapping("/spotify")
    fun spotifyLink(): String = oAuthSpotifyService.link

    @GetMapping("/spotify/redirect")
    @Operation(summary = "Spotify redirect endpoint", tags = ["OAuth2"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = Map::class))]),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class))])
        ]
    )
    fun spotify(request: HttpServletRequest,
        @Parameter(description = "State param") @RequestParam(required = false) state: String?,
        @Parameter(description = "Code param") @RequestParam(required = false) code: String?,
        @Parameter(description = "Error param") @RequestParam(value="error_code", required = false) error: String?,
        @Parameter(description = "Error Description param") @RequestParam(value = "errorMessage", required = false) errorMessage: String?,
    ): Map<String, Any> {
        validateError(error = error, errorMessage = errorMessage)
        validateResponse(code = code, state = state)
        log.info("Started processing auth for facebook. Code: $code, state: $state")
        val oAuthTokenResponse: OAuthTokenResponse = oAuthSpotifyService.getToken(code = code!!)
        var oAuthSpotifyUser: OAuthSpotifyUser = oAuthSpotifyService.getUserInfo(accessToken = oAuthTokenResponse.access_token!!)

        oAuthSpotifyUser = try {
            oAuthSpotifyService.getByIdOrThrowException(id = oAuthSpotifyUser.id!!)
        } catch (e: Exception) {
            log.warn("OAuthSpotifyUser is saved to db: $oAuthSpotifyUser")
            oAuthSpotifyService.save(entity = oAuthSpotifyUser)
        }
        val oAuthUserResponse: UserResponseWrapperDto = oAuthSpotifyService.authenticate(
            jwtToken = request.getHeader("Authorization"), oAuthGoogleUser = oAuthSpotifyUser)

        log.info("Finished processing auth for OAuthSpotifyUser: $oAuthSpotifyUser")
        return mapOf(
            "state" to state!!,
            "code" to code,
            "oAuthTokenResponse" to oAuthTokenResponse,
            "oAuthUserResponse" to oAuthUserResponse
        )
    }

    @GetMapping("/twitch")
    fun twitchLink(): String = oAuthTwitchService.link

    @GetMapping("/twitch/redirect")
    @Operation(summary = "Twitch redirect endpoint", tags = ["OAuth2"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = Map::class))]),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class))])
        ]
    )
    fun twitch(request: HttpServletRequest,
        @Parameter(description = "State param") @RequestParam(required = false) state: String?,
        @Parameter(description = "Code param") @RequestParam(required = false) code: String?,
        @Parameter(description = "Error param") @RequestParam(value="error_code", required = false) error: String?,
        @Parameter(description = "Error Description param") @RequestParam(value = "errorMessage", required = false) errorMessage: String?,
    ): Map<String, Any> {
        validateError(error = error, errorMessage = errorMessage)
        validateResponse(code = code, state = state)
        log.info("Started processing auth for facebook. Code: $code, state: $state")
        val oAuthTokenResponse: OAuthTokenResponse = oAuthTwitchService.getToken(code = code!!)
        var oAuthTwitchUser: OAuthTwitchUser = oAuthTwitchService.getUserInfo(accessToken = oAuthTokenResponse.access_token!!)

        oAuthTwitchUser = try {
            oAuthTwitchService.getByIdOrThrowException(id = oAuthTwitchUser.id!!)
        } catch (e: Exception) {
            log.warn("OAuthTwitchUser is saved to db: $oAuthTwitchUser")
            oAuthTwitchService.save(entity = oAuthTwitchUser)
        }
        val oAuthUserResponse: UserResponseWrapperDto = oAuthTwitchService.authenticate(
            jwtToken = request.getHeader("Authorization"), oAuthGoogleUser = oAuthTwitchUser)

        log.info("Finished processing auth for OAuthTwitchUser: $oAuthTwitchUser")
        return mapOf(
            "state" to state!!,
            "code" to code,
            "oAuthTokenResponse" to oAuthTokenResponse,
            "oAuthUserResponse" to oAuthUserResponse
        )
    }

    @GetMapping("/slack")
    fun slackLink(): String = oAuthSlackService.link

    @GetMapping("/slack/redirect")
    @Operation(summary = "Slack redirect endpoint", tags = ["OAuth2"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = Map::class))]),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class))])
        ]
    )
    fun slack(request: HttpServletRequest,
       @Parameter(description = "State param") @RequestParam(required = false) state: String?,
       @Parameter(description = "Code param") @RequestParam(required = false) code: String?,
       @Parameter(description = "Error param") @RequestParam(value="error_code", required = false) error: String?,
       @Parameter(description = "Error Description param") @RequestParam(value = "errorMessage", required = false) errorMessage: String?,
    ): Map<String, Any> {
        validateError(error = error, errorMessage = errorMessage)
        validateResponse(code = code, state = state)
        log.info("Started processing auth for facebook. Code: $code, state: $state")
        val oAuthTokenResponse: OAuthTokenResponse = oAuthSlackService.getToken(code = code!!)
        var oAuthSlackUser: OAuthSlackUser = oAuthSlackService.getUserInfo(accessToken = oAuthTokenResponse.access_token!!)

        oAuthSlackUser = try {
            oAuthSlackService.getByIdOrThrowException(id = oAuthSlackUser.id!!)
        } catch (e: Exception) {
            log.warn("oAuthSlackService is saved to db: $oAuthSlackUser")
            oAuthSlackService.save(entity = oAuthSlackUser)
        }
        val oAuthUserResponse: UserResponseWrapperDto = oAuthSlackService.authenticate(
            jwtToken = request.getHeader("Authorization"), oAuthGoogleUser = oAuthSlackUser)

        log.info("Finished processing auth for oAuthSlackService: $oAuthSlackUser")
        return mapOf(
            "state" to state!!,
            "code" to code,
            "oAuthTokenResponse" to oAuthTokenResponse,
            "oAuthUserResponse" to oAuthUserResponse
        )
    }

    @GetMapping("/dropbox")
    fun dropboxLink(): String = oAuthDropboxService.link

    @GetMapping("/dropbox/redirect")
    @Operation(summary = "Dropbox redirect endpoint", tags = ["OAuth2"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = Map::class))]),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class))])
        ]
    )
    fun dropbox(request: HttpServletRequest,
      @Parameter(description = "State param") @RequestParam(required = false) state: String?,
      @Parameter(description = "Code param") @RequestParam(required = false) code: String?,
      @Parameter(description = "Error param") @RequestParam(value="error_code", required = false) error: String?,
      @Parameter(description = "Error Description param") @RequestParam(value = "errorMessage", required = false) errorMessage: String?,
    ): Map<String, Any> {
        validateError(error = error, errorMessage = errorMessage)
        validateResponse(code = code, state = state)
        log.info("Started processing auth for facebook. Code: $code, state: $state")
        val oAuthTokenResponse: OAuthTokenResponse = oAuthDropboxService.getToken(code = code!!)
        var oAuthDropboxUser: OAuthDropboxUser = oAuthDropboxService.getUserInfo(accessToken = oAuthTokenResponse.access_token!!)

        oAuthDropboxUser = try {
            oAuthDropboxService.getByIdOrThrowException(id = oAuthDropboxUser.id!!)
        } catch (e: Exception) {
            log.warn("oAuthDropboxService is saved to db: $oAuthDropboxUser")
            oAuthDropboxService.save(entity = oAuthDropboxUser)
        }
        val oAuthUserResponse: UserResponseWrapperDto = oAuthDropboxService.authenticate(
            jwtToken = request.getHeader("Authorization"), oAuthGoogleUser = oAuthDropboxUser)

        log.info("Finished processing auth for oAuthDropboxService: $oAuthDropboxUser")
        return mapOf(
            "state" to state!!,
            "code" to code,
            "oAuthTokenResponse" to oAuthTokenResponse,
            "oAuthUserResponse" to oAuthUserResponse
        )
    }

    @GetMapping
    fun showAllLinks(): Map<String, String> = mapOf(
        "google" to oAuthGoogleService.link,
        "github" to oAuthGithubService.link,
        "linkedin" to oAuthLinkedinService.link,
        "facebook" to oAuthFacebookService.link,
        "x/twitter" to oAuthTwitterService.link,
        "spotify" to oAuthSpotifyService.link,
        "twitch" to oAuthTwitchService.link,
        "slack" to oAuthSlackService.link,
        "dropbox" to oAuthDropboxService.link,
    )

    /**
     * Validates the response received from the server by checking if the required parameters code and state are present.
     * If either of them is missing, it logs an error and throws a custom ServerException indicating the mandatory input missing.
     *
     * @param code The code received from the server.
     * @param state The state received from the server.
     * @throws ServerException If either code or state is null.
     */
    private fun validateResponse(code: String?, state: String?) {
        if (code == null && state == null) {
            val msg = "code and state is null!"
            log.error(msg)
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.MANDATORY_INPUT_MISSING,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf(msg))
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