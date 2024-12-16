package com.github.senocak.ratehighway.controller

import com.github.senocak.ratehighway.domain.OAuthAsanaUser
import com.github.senocak.ratehighway.domain.OAuthBoxUser
import com.github.senocak.ratehighway.domain.OAuthDiscordUser
import com.github.senocak.ratehighway.domain.OAuthDropboxUser
import com.github.senocak.ratehighway.domain.dto.UserResponseWrapperDto
import com.github.senocak.ratehighway.domain.OAuthFacebookUser
import com.github.senocak.ratehighway.domain.OAuthFoursquareUser
import com.github.senocak.ratehighway.domain.OAuthGithubUser
import com.github.senocak.ratehighway.domain.OAuthGitlabUser
import com.github.senocak.ratehighway.domain.OAuthGoogleUser
import com.github.senocak.ratehighway.domain.OAuthInstagramUser
import com.github.senocak.ratehighway.domain.OAuthLinkedinUser
import com.github.senocak.ratehighway.domain.OAuthOktaUser
import com.github.senocak.ratehighway.domain.OAuthPaypalUser
import com.github.senocak.ratehighway.domain.OAuthRedditUser
import com.github.senocak.ratehighway.domain.OAuthSlackUser
import com.github.senocak.ratehighway.domain.OAuthSpotifyUser
import com.github.senocak.ratehighway.domain.OAuthTiktokUser
import com.github.senocak.ratehighway.domain.OAuthTwitchUser
import com.github.senocak.ratehighway.domain.OAuthTwitterUser
import com.github.senocak.ratehighway.domain.OAuthVimeoUser
import com.github.senocak.ratehighway.domain.dto.OAuthTokenResponse
import com.github.senocak.ratehighway.exception.ServerException
import com.github.senocak.ratehighway.service.oauth2.OAuthAsanaService
import com.github.senocak.ratehighway.service.oauth2.OAuthBoxService
import com.github.senocak.ratehighway.service.oauth2.OAuthDiscordService
import com.github.senocak.ratehighway.service.oauth2.OAuthDropboxService
import com.github.senocak.ratehighway.service.oauth2.OAuthFacebookService
import com.github.senocak.ratehighway.service.oauth2.OAuthFourSquareService
import com.github.senocak.ratehighway.service.oauth2.OAuthGithubService
import com.github.senocak.ratehighway.service.oauth2.OAuthGitlabService
import com.github.senocak.ratehighway.service.oauth2.OAuthGoogleService
import com.github.senocak.ratehighway.service.oauth2.OAuthInstagramService
import com.github.senocak.ratehighway.service.oauth2.OAuthLinkedinService
import com.github.senocak.ratehighway.service.oauth2.OAuthOktaService
import com.github.senocak.ratehighway.service.oauth2.OAuthPaypalService
import com.github.senocak.ratehighway.service.oauth2.OAuthRedditService
import com.github.senocak.ratehighway.service.oauth2.OAuthSlackService
import com.github.senocak.ratehighway.service.oauth2.OAuthSpotifyService
import com.github.senocak.ratehighway.service.oauth2.OAuthTiktokService
import com.github.senocak.ratehighway.service.oauth2.OAuthTwitchService
import com.github.senocak.ratehighway.service.oauth2.OAuthTwitterService
import com.github.senocak.ratehighway.service.oauth2.OAuthVimeoService
import com.github.senocak.ratehighway.util.OAuth2Services
import com.github.senocak.ratehighway.util.OmaErrorMessageType
import com.github.senocak.ratehighway.util.logger
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.PathVariable

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
    private val oAuthInstagramService: OAuthInstagramService,
    private val oAuthPaypalService: OAuthPaypalService,
    private val oAuthDiscordService: OAuthDiscordService,
    private val oAuthOktaService: OAuthOktaService,
    private val oAuthRedditService: OAuthRedditService,
    private val oAuthTiktokService: OAuthTiktokService,
    private val oAuthBoxService: OAuthBoxService,
    private val oAuthVimeoService: OAuthVimeoService,
    private val oAuthGitlabService: OAuthGitlabService,
    private val oAuthAsanaService: OAuthAsanaService,
    private val oAuthFourSquareService: OAuthFourSquareService,
): BaseController() {
    private val log: Logger by logger()

    @GetMapping
    fun links(): Map<String, String> = mapOf(
        "google" to oAuthGoogleService.link,
        "github" to oAuthGithubService.link,
        "linkedin" to oAuthLinkedinService.link,
        "facebook" to oAuthFacebookService.link,
        "x/twitter" to oAuthTwitterService.link,
        "spotify" to oAuthSpotifyService.link,
        "twitch" to oAuthTwitchService.link,
        "slack" to oAuthSlackService.link,
        "dropbox" to oAuthDropboxService.link,
        "instagram" to oAuthInstagramService.link,
        "paypal" to oAuthPaypalService.link,
        "discord" to oAuthDiscordService.link,
        "okta" to oAuthOktaService.link,
        "reddit" to oAuthRedditService.link,
        "tiktok" to oAuthTiktokService.link,
        "box" to oAuthBoxService.link,
        "vimeo" to oAuthVimeoService.link,
        "gitlab" to oAuthGitlabService.link,
        "asana" to oAuthAsanaService.link,
        "foursquare" to oAuthFourSquareService.link,
    )

    @GetMapping("/{service}")
    fun authorize(@PathVariable service: String): String =
        links()[service] ?: throw Exception("Service not found")

    @GetMapping("/{service}/redirect")
    fun redirect(request: HttpServletRequest,
         @PathVariable service: String,
         @RequestParam(required = false) state: String? = null,
         @RequestParam(required = false) code: String? = null,
         @RequestParam(required = false) scope: String? = null, // Google
         @RequestParam(required = false) authuser: String? = null, // Google
         @RequestParam(required = false, defaultValue = "hd") hd: String? = null, // Google
         @RequestParam(required = false) prompt: String? = null, // Google

         @RequestParam(value="error_code", required = false) errorCode: String?,
         @RequestParam(value = "errorMessage", required = false) errorMessage: String?,

         @RequestParam(required = false) error: String?, // Linkedin & Instagram
         @RequestParam(value = "errorDescription",required = false) errorDescription: String?, // Linkedin

         @RequestParam(required = false) error_reason: String?, // Instagram
    ): Map<String, Any?> {
        if (code == null && state == null) {
            val msg = "code and state is null!"
            log.error(msg)
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.MANDATORY_INPUT_MISSING,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf(msg))
        }
        if(error != null || errorMessage != null) {
            log.info("Error occurred for oAuthFacebook, Error: $error, ErrorMessage: $errorMessage")
            //throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
            //    variables = arrayOf(error, errorMessage), statusCode = HttpStatus.BAD_REQUEST)
        }
        log.info("Started processing auth for $service. Code: $code, state: $state")
        when (OAuth2Services.fromString(service = service)) {
            OAuth2Services.GOOGLE -> {
                val oAuthTokenResponse: OAuthTokenResponse = oAuthGoogleService.getToken(code = code!!)
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
            OAuth2Services.GITHUB -> {
                val oAuthTokenResponse: OAuthTokenResponse = oAuthGithubService.getToken(code = code!!)
                var oAuthGithubUser: OAuthGithubUser = oAuthGithubService.getGithubUserInfo(accessToken = oAuthTokenResponse.access_token!!)
                oAuthGithubUser = try {
                    oAuthGithubService.getByIdOrThrowException(id = oAuthGithubUser.id!!)
                } catch (e: ServerException) {
                    log.warn("oAuthGithubUser is saved to db: $oAuthGithubUser")
                    oAuthGithubService.save(entity = oAuthGithubUser)
                }
                val oAuthUserResponse: UserResponseWrapperDto = oAuthGithubService.authenticate(
                    jwtToken = request.getHeader("Authorization"), oAuthUser = oAuthGithubUser)
                log.info("Finished processing auth for github. Response: $oAuthUserResponse")
                return mapOf(
                    "code" to code,
                    "oAuthTokenResponse" to oAuthTokenResponse,
                    "oAuthUserResponse" to oAuthUserResponse
                )
            }
            OAuth2Services.LINKEDIN -> {
                val oAuthTokenResponse: OAuthTokenResponse = oAuthLinkedinService.getToken(code = code!!)
                var oAuthLinkedinUser: OAuthLinkedinUser = oAuthLinkedinService.getLinkedinUserInfo(accessToken = oAuthTokenResponse.access_token!!)

                oAuthLinkedinUser = try {
                    oAuthLinkedinService.getByIdOrEmailOrThrowException(id = oAuthLinkedinUser.id!!, email = oAuthLinkedinUser.email!!)
                } catch (e: ServerException) {
                    log.warn("oAuthLinkedinUser is saved to db: $oAuthLinkedinUser")
                    oAuthLinkedinService.save(entity = oAuthLinkedinUser)
                }
                val oAuthUserResponse: UserResponseWrapperDto = oAuthLinkedinService.authenticate(
                    jwtToken = request.getHeader("Authorization"), oAuthUser = oAuthLinkedinUser)

                log.info("Finished processing auth for linkedin")
                return mapOf(
                    "state" to state,
                    "code" to code,
                    "oAuthTokenResponse" to oAuthTokenResponse,
                    "oAuthUserResponse" to oAuthUserResponse
                )
            }
            OAuth2Services.FACEBOOK -> {
                val oAuthTokenResponse: OAuthTokenResponse = oAuthFacebookService.getToken(code = code!!)
                var oAuthFacebookUser: OAuthFacebookUser = oAuthFacebookService.getFacebookUserInfo(accessToken = oAuthTokenResponse.access_token!!)
                oAuthFacebookUser = try {
                    oAuthFacebookService.getByIdOrThrowException(id = oAuthFacebookUser.id!!)
                } catch (e: Exception) {
                    log.warn("OAuthFacebookUser is saved to db: $oAuthFacebookUser")
                    oAuthFacebookService.save(entity = oAuthFacebookUser)
                }
                val oAuthUserResponse: UserResponseWrapperDto = oAuthFacebookService.authenticate(
                    jwtToken = request.getHeader("Authorization"), oAuthUser = oAuthFacebookUser)

                log.info("Finished processing auth for OAuthFacebookUser: $oAuthFacebookUser")
                return mapOf(
                    "state" to state,
                    "code" to code,
                    "oAuthTokenResponse" to oAuthTokenResponse,
                    "oAuthUserResponse" to oAuthUserResponse
                )
            }
            OAuth2Services.TWITTER -> {
                val oAuthTokenResponse: OAuthTokenResponse = oAuthTwitterService.getToken(code = code!!)
                var oAuthTwitterUser: OAuthTwitterUser = oAuthTwitterService.getUserInfo(accessToken = oAuthTokenResponse.access_token!!)

                oAuthTwitterUser = try {
                    oAuthTwitterService.getByIdOrThrowException(id = oAuthTwitterUser.id!!)
                } catch (e: Exception) {
                    log.warn("OAuthTwitterUser is saved to db: $oAuthTwitterUser")
                    oAuthTwitterService.save(entity = oAuthTwitterUser)
                }
                val oAuthUserResponse: UserResponseWrapperDto = oAuthTwitterService.authenticate(
                    jwtToken = request.getHeader("Authorization"), oAuthUser = oAuthTwitterUser)

                log.info("Finished processing auth for OAuthTwitterUser: $oAuthTwitterUser")
                return mapOf(
                    "state" to state,
                    "code" to code,
                    "oAuthTokenResponse" to oAuthTokenResponse,
                    "oAuthUserResponse" to oAuthUserResponse
                )
            }
            OAuth2Services.SPOTIFY -> {
                val oAuthTokenResponse: OAuthTokenResponse = oAuthSpotifyService.getToken(code = code!!)
                var oAuthSpotifyUser: OAuthSpotifyUser = oAuthSpotifyService.getUserInfo(accessToken = oAuthTokenResponse.access_token!!)

                oAuthSpotifyUser = try {
                    oAuthSpotifyService.getByIdOrThrowException(id = oAuthSpotifyUser.id!!)
                } catch (e: Exception) {
                    log.warn("OAuthSpotifyUser is saved to db: $oAuthSpotifyUser")
                    oAuthSpotifyService.save(entity = oAuthSpotifyUser)
                }
                val oAuthUserResponse: UserResponseWrapperDto = oAuthSpotifyService.authenticate(
                    jwtToken = request.getHeader("Authorization"), oAuthUser = oAuthSpotifyUser)

                log.info("Finished processing auth for OAuthSpotifyUser: $oAuthSpotifyUser")
                return mapOf(
                    "state" to state,
                    "code" to code,
                    "oAuthTokenResponse" to oAuthTokenResponse,
                    "oAuthUserResponse" to oAuthUserResponse
                )
            }
            OAuth2Services.TWITCH -> {
                val oAuthTokenResponse: OAuthTokenResponse = oAuthTwitchService.getToken(code = code!!)
                var oAuthTwitchUser: OAuthTwitchUser = oAuthTwitchService.getUserInfo(accessToken = oAuthTokenResponse.access_token!!)

                oAuthTwitchUser = try {
                    oAuthTwitchService.getByIdOrThrowException(id = oAuthTwitchUser.id!!)
                } catch (e: Exception) {
                    log.warn("OAuthTwitchUser is saved to db: $oAuthTwitchUser")
                    oAuthTwitchService.save(entity = oAuthTwitchUser)
                }
                val oAuthUserResponse: UserResponseWrapperDto = oAuthTwitchService.authenticate(
                    jwtToken = request.getHeader("Authorization"), oAuthUser = oAuthTwitchUser)

                log.info("Finished processing auth for OAuthTwitchUser: $oAuthTwitchUser")
                return mapOf(
                    "state" to state,
                    "code" to code,
                    "oAuthTokenResponse" to oAuthTokenResponse,
                    "oAuthUserResponse" to oAuthUserResponse
                )
            }
            OAuth2Services.SLACK -> {
                val oAuthTokenResponse: OAuthTokenResponse = oAuthSlackService.getToken(code = code!!)
                var oAuthSlackUser: OAuthSlackUser = oAuthSlackService.getUserInfo(accessToken = oAuthTokenResponse.access_token!!)

                oAuthSlackUser = try {
                    oAuthSlackService.getByIdOrThrowException(id = oAuthSlackUser.id!!)
                } catch (e: Exception) {
                    log.warn("oAuthSlackService is saved to db: $oAuthSlackUser")
                    oAuthSlackService.save(entity = oAuthSlackUser)
                }
                val oAuthUserResponse: UserResponseWrapperDto = oAuthSlackService.authenticate(
                    jwtToken = request.getHeader("Authorization"), oAuthUser = oAuthSlackUser)

                log.info("Finished processing auth for oAuthSlackService: $oAuthSlackUser")
                return mapOf(
                    "state" to state,
                    "code" to code,
                    "oAuthTokenResponse" to oAuthTokenResponse,
                    "oAuthUserResponse" to oAuthUserResponse
                )
            }
            OAuth2Services.DROPBOX -> {
                val oAuthTokenResponse: OAuthTokenResponse = oAuthDropboxService.getToken(code = code!!)
                var oAuthDropboxUser: OAuthDropboxUser = oAuthDropboxService.getUserInfo(accessToken = oAuthTokenResponse.access_token!!)

                oAuthDropboxUser = try {
                    oAuthDropboxService.getByIdOrThrowException(id = oAuthDropboxUser.id!!)
                } catch (e: Exception) {
                    log.warn("oAuthDropboxService is saved to db: $oAuthDropboxUser")
                    oAuthDropboxService.save(entity = oAuthDropboxUser)
                }
                val oAuthUserResponse: UserResponseWrapperDto = oAuthDropboxService.authenticate(
                    jwtToken = request.getHeader("Authorization"), oAuthUser = oAuthDropboxUser)

                log.info("Finished processing auth for oAuthDropboxService: $oAuthDropboxUser")
                return mapOf(
                    "state" to state,
                    "code" to code,
                    "oAuthTokenResponse" to oAuthTokenResponse,
                    "oAuthUserResponse" to oAuthUserResponse
                )
            }
            OAuth2Services.INSTAGRAM -> {
                val oAuthTokenResponse: OAuthTokenResponse = oAuthInstagramService.getToken(code = code!!)
                var oAuthInstagramUser: OAuthInstagramUser = oAuthInstagramService.getUserInfo(accessToken = oAuthTokenResponse.access_token!!)
                oAuthInstagramUser = try {
                    oAuthInstagramService.getByIdOrThrowException(id = oAuthInstagramUser.id!!)
                } catch (e: Exception) {
                    log.warn("oAuthInstagramService is saved to db: $oAuthInstagramUser")
                    oAuthInstagramService.save(entity = oAuthInstagramUser)
                }
                val oAuthUserResponse: UserResponseWrapperDto = oAuthInstagramService.authenticate(
                    jwtToken = request.getHeader("Authorization"), oAuthUser = oAuthInstagramUser)

                log.info("Finished processing auth for oAuthInstagramService: $oAuthInstagramUser")
                return mapOf(
                    "code" to code,
                    "oAuthTokenResponse" to oAuthTokenResponse,
                    "oAuthUserResponse" to oAuthUserResponse
                )
            }
            OAuth2Services.PAYPAL -> {
                var oAuthTokenResponse: OAuthTokenResponse = oAuthPaypalService.getToken(code = code!!)
                oAuthTokenResponse = oAuthPaypalService.getTokenByRefreshToken(refreshToken = oAuthTokenResponse.refresh_token!!)
                var oAuthInstagramUser: OAuthPaypalUser = oAuthPaypalService.getUserInfo(accessToken = oAuthTokenResponse.access_token!!)
                oAuthInstagramUser = try {
                    oAuthPaypalService.getByIdOrThrowException(id = oAuthInstagramUser.id!!)
                } catch (e: Exception) {
                    log.warn("oAuthPaypalService is saved to db: $oAuthInstagramUser")
                    oAuthPaypalService.save(entity = oAuthInstagramUser)
                }
                val oAuthUserResponse: UserResponseWrapperDto = oAuthPaypalService.authenticate(
                    jwtToken = request.getHeader("Authorization"), oAuthUser = oAuthInstagramUser)

                log.info("Finished processing auth for oAuthPaypalService: $oAuthInstagramUser")
                return mapOf(
                    "code" to code,
                    "oAuthTokenResponse" to oAuthTokenResponse,
                    "oAuthUserResponse" to oAuthUserResponse
                )
            }
            OAuth2Services.DISCORD -> {
                val oAuthTokenResponse: OAuthTokenResponse = oAuthDiscordService.getToken(code = code!!)
                var oAuthDiscordUser: OAuthDiscordUser = oAuthDiscordService.getUserInfo(accessToken = oAuthTokenResponse.access_token!!)
                oAuthDiscordUser = try {
                    oAuthDiscordService.getByIdOrThrowException(id = oAuthDiscordUser.id!!)
                } catch (e: Exception) {
                    log.warn("oAuthDiscordService is saved to db: $oAuthDiscordUser")
                    oAuthDiscordService.save(entity = oAuthDiscordUser)
                }
                val oAuthUserResponse: UserResponseWrapperDto = oAuthDiscordService.authenticate(
                    jwtToken = request.getHeader("Authorization"), oAuthUser = oAuthDiscordUser)

                log.info("Finished processing auth for oAuthDiscordService: $oAuthDiscordUser")
                return mapOf(
                    "code" to code,
                    "oAuthTokenResponse" to oAuthTokenResponse,
                    "oAuthUserResponse" to oAuthUserResponse
                )
            }
            OAuth2Services.OKTA -> {
                val oAuthTokenResponse: OAuthTokenResponse = oAuthOktaService.getToken(code = code!!)
                var oAuthOktaUser: OAuthOktaUser = oAuthOktaService.getUserInfo(accessToken = oAuthTokenResponse.access_token!!)
                oAuthOktaUser = try {
                    oAuthOktaService.getByIdOrThrowException(id = oAuthOktaUser.id!!)
                } catch (e: Exception) {
                    log.warn("oAuthOktaService is saved to db: $oAuthOktaUser")
                    oAuthOktaService.save(entity = oAuthOktaUser)
                }
                val oAuthUserResponse: UserResponseWrapperDto = oAuthOktaService.authenticate(
                    jwtToken = request.getHeader("Authorization"), oAuthUser = oAuthOktaUser)

                log.info("Finished processing auth for oAuthOktaService: $oAuthOktaUser")
                return mapOf(
                    "code" to code,
                    "oAuthTokenResponse" to oAuthTokenResponse,
                    "oAuthUserResponse" to oAuthUserResponse
                )
            }
            OAuth2Services.REDDIT -> {
                val oAuthTokenResponse: OAuthTokenResponse = oAuthRedditService.getToken(code = code!!)
                var oAuthRedditUser: OAuthRedditUser = oAuthRedditService.getUserInfo(accessToken = oAuthTokenResponse.access_token!!)
                oAuthRedditUser = try {
                    oAuthRedditService.getByIdOrThrowException(id = oAuthRedditUser.id!!)
                } catch (e: Exception) {
                    log.warn("oAuthRedditService is saved to db: $oAuthRedditUser")
                    oAuthRedditService.save(entity = oAuthRedditUser)
                }
                val oAuthUserResponse: UserResponseWrapperDto = oAuthRedditService.authenticate(
                    jwtToken = request.getHeader("Authorization"), oAuthUser = oAuthRedditUser)

                log.info("Finished processing auth for oAuthRedditService: $oAuthRedditUser")
                return mapOf(
                    "code" to code,
                    "oAuthTokenResponse" to oAuthTokenResponse,
                    "oAuthUserResponse" to oAuthUserResponse
                )
            }
            OAuth2Services.TIKTOK -> {
                val oAuthTokenResponse: OAuthTokenResponse = oAuthTiktokService.getToken(code = code!!)
                var oAuthTiktokUser: OAuthTiktokUser = oAuthTiktokService.getUserInfo(accessToken = oAuthTokenResponse.access_token!!)
                oAuthTiktokUser = try {
                    oAuthTiktokService.getByIdOrThrowException(id = oAuthTiktokUser.id!!)
                } catch (e: Exception) {
                    log.warn("oAuthTiktokService is saved to db: $oAuthTiktokUser")
                    oAuthTiktokService.save(entity = oAuthTiktokUser)
                }
                val oAuthUserResponse: UserResponseWrapperDto = oAuthTiktokService.authenticate(
                    jwtToken = request.getHeader("Authorization"), oAuthUser = oAuthTiktokUser)

                log.info("Finished processing auth for oAuthTiktokService: $oAuthTiktokUser")
                return mapOf(
                    "code" to code,
                    "oAuthTokenResponse" to oAuthTokenResponse,
                    "oAuthUserResponse" to oAuthUserResponse
                )
            }
            OAuth2Services.BOX -> {
                val oAuthTokenResponse: OAuthTokenResponse = oAuthBoxService.getToken(code = code!!)
                var oAuthBoxUser: OAuthBoxUser = oAuthBoxService.getUserInfo(accessToken = oAuthTokenResponse.access_token!!)
                oAuthBoxUser = try {
                    oAuthBoxService.getByIdOrThrowException(id = oAuthBoxUser.id!!)
                } catch (e: Exception) {
                    log.warn("oAuthBoxService is saved to db: $oAuthBoxUser")
                    oAuthBoxService.save(entity = oAuthBoxUser)
                }
                val oAuthUserResponse: UserResponseWrapperDto = oAuthBoxService.authenticate(
                    jwtToken = request.getHeader("Authorization"), oAuthUser = oAuthBoxUser)

                log.info("Finished processing auth for oAuthBoxService: $oAuthBoxUser")
                return mapOf(
                    "code" to code,
                    "oAuthTokenResponse" to oAuthTokenResponse,
                    "oAuthUserResponse" to oAuthUserResponse
                )
            }
            OAuth2Services.VIMEO -> {
                val oAuthTokenResponse: OAuthTokenResponse = oAuthVimeoService.getToken(code = code!!)
                var oAuthVimeoUser: OAuthVimeoUser = oAuthVimeoService.getUserInfo(accessToken = oAuthTokenResponse.access_token!!)
                oAuthVimeoUser = try {
                    oAuthVimeoService.getByIdOrThrowException(id = oAuthVimeoUser.id!!)
                } catch (e: Exception) {
                    log.warn("oAuthVimeoService is saved to db: $oAuthVimeoUser")
                    oAuthVimeoService.save(entity = oAuthVimeoUser)
                }
                val oAuthUserResponse: UserResponseWrapperDto = oAuthVimeoService.authenticate(
                    jwtToken = request.getHeader("Authorization"), oAuthUser = oAuthVimeoUser)

                log.info("Finished processing auth for oAuthVimeoService: $oAuthVimeoUser")
                return mapOf(
                    "code" to code,
                    "oAuthTokenResponse" to oAuthTokenResponse,
                    "oAuthUserResponse" to oAuthUserResponse
                )
            }
            OAuth2Services.GITLAB -> {
                val oAuthTokenResponse: OAuthTokenResponse = oAuthGitlabService.getToken(code = code!!)
                var oAuthGitlabUser: OAuthGitlabUser = oAuthGitlabService.getUserInfo(accessToken = oAuthTokenResponse.access_token!!)
                oAuthGitlabUser = try {
                    oAuthGitlabService.getByIdOrThrowException(id = oAuthGitlabUser.id!!)
                } catch (e: Exception) {
                    log.warn("oAuthGitlabService is saved to db: $oAuthGitlabUser")
                    oAuthGitlabService.save(entity = oAuthGitlabUser)
                }
                val oAuthUserResponse: UserResponseWrapperDto = oAuthGitlabService.authenticate(
                    jwtToken = request.getHeader("Authorization"), oAuthUser = oAuthGitlabUser)

                log.info("Finished processing auth for oAuthGitlabService: $oAuthGitlabUser")
                return mapOf(
                    "code" to code,
                    "oAuthTokenResponse" to oAuthTokenResponse,
                    "oAuthUserResponse" to oAuthUserResponse
                )
            }
            OAuth2Services.ASANA -> {
                val oAuthTokenResponse: OAuthTokenResponse = oAuthAsanaService.getToken(code = code!!)
                var oAuthAsanaUser: OAuthAsanaUser = oAuthAsanaService.getUserInfo(oAuthTokenResponse = oAuthTokenResponse)
                oAuthAsanaUser = try {
                    oAuthAsanaService.getByIdOrThrowException(id = oAuthAsanaUser.id!!)
                } catch (e: Exception) {
                    log.warn("oAuthAsanaService is saved to db: $oAuthAsanaUser")
                    oAuthAsanaService.save(entity = oAuthAsanaUser)
                }
                val oAuthUserResponse: UserResponseWrapperDto = oAuthAsanaService.authenticate(
                    jwtToken = request.getHeader("Authorization"), oAuthUser = oAuthAsanaUser)

                log.info("Finished processing auth for oAuthAsanaService: $oAuthAsanaUser")
                return mapOf(
                    "code" to code,
                    "oAuthTokenResponse" to oAuthTokenResponse,
                    "oAuthUserResponse" to oAuthUserResponse
                )
            }
            OAuth2Services.FOURSQUARE -> {
                val oAuthTokenResponse: OAuthTokenResponse = oAuthFourSquareService.getToken(code = code!!)
                var oAuthFoursquareUser: OAuthFoursquareUser = oAuthFourSquareService.getUserInfo(oAuthTokenResponse = oAuthTokenResponse)
                oAuthFoursquareUser = try {
                    oAuthFourSquareService.getByIdOrThrowException(id = oAuthFoursquareUser.id!!)
                } catch (e: Exception) {
                    log.warn("oAuthFourSquareService is saved to db: $oAuthFoursquareUser")
                    oAuthFourSquareService.save(entity = oAuthFoursquareUser)
                }
                val oAuthUserResponse: UserResponseWrapperDto = oAuthFourSquareService.authenticate(
                    jwtToken = request.getHeader("Authorization"), oAuthUser = oAuthFoursquareUser)

                log.info("Finished processing auth for oAuthFourSquareService: $oAuthFoursquareUser")
                return mapOf(
                    "code" to code,
                    "oAuthTokenResponse" to oAuthTokenResponse,
                    "oAuthUserResponse" to oAuthUserResponse
                )
            }
            else -> throw Exception("")
        }
    }
}
