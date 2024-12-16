package com.github.senocak.ratehighway.controller

import com.github.senocak.ratehighway.domain.dto.ExceptionDto
import com.github.senocak.ratehighway.domain.dto.UpdateUserDto
import com.github.senocak.ratehighway.domain.dto.UserResponseDto
import com.github.senocak.ratehighway.domain.dto.UserResponseWrapperDto
import com.github.senocak.ratehighway.domain.User
import com.github.senocak.ratehighway.exception.NotFoundException
import com.github.senocak.ratehighway.exception.ServerException
import com.github.senocak.ratehighway.security.Authorize
import com.github.senocak.ratehighway.service.MessageSourceService
import com.github.senocak.ratehighway.service.UserService
import com.github.senocak.ratehighway.util.ADMIN
import com.github.senocak.ratehighway.util.OAuth2Services
import com.github.senocak.ratehighway.util.OmaErrorMessageType
import com.github.senocak.ratehighway.util.USER
import com.github.senocak.ratehighway.util.logger
import com.github.senocak.ratehighway.util.securitySchemeName
import com.github.senocak.ratehighway.util.toDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.IOException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder

@Validated
@RestController
@Authorize
@RequestMapping(BaseController.v1UserUrl)
@Tag(name = "User", description = "User Controller")
class UserController(
    private val userService: UserService,
    private val messageSourceService: MessageSourceService,
    private val passwordEncoder: PasswordEncoder,
): BaseController() {
    private val log: Logger by logger()

    @Throws(ServerException::class)
    @Operation(summary = "Get me", tags = ["User"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = UserResponseWrapperDto::class))]),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class))])
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN, USER])]
    )
    @GetMapping("/me")
    fun me(): ResponseEntity<UserResponseWrapperDto> {
        val user: User = userService.loggedInUser()
        val userResponseDto: UserResponseDto = user.toDTO()
        log.info("User retrieved me endpoint: $userResponseDto")
        return ResponseEntity
            .status(HttpStatus.OK)
            .headers(userIdHeader(userId = "${user.id}"))
            .body(UserResponseWrapperDto(userResponseDto))
    }

    @PatchMapping("/me")
    @Operation(summary = "Update user by username", tags = ["User"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = HashMap::class))]),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class))])
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN, USER])]
    )
    @Throws(ServerException::class)
    fun patchMe(request: HttpServletRequest,
                @Parameter(description = "Request body to update", required = true) @Validated @RequestBody userDto: UpdateUserDto,
                resultOfValidation: BindingResult
    ): ResponseEntity<UserResponseWrapperDto> {
        validate(resultOfValidation = resultOfValidation)
        var user: User = userService.loggedInUser()
        user = validateUserUpdate(userDto = userDto, user = user)
        user = userService.save(user = user)
        val userResponseDto: UserResponseDto = user.toDTO()
        log.info("User retrieved me endpoint: {}", userResponseDto)
        return ResponseEntity
            .status(HttpStatus.OK)
            .headers(userIdHeader(userId = "${user.id}"))
            .body(UserResponseWrapperDto(userResponseDto = userResponseDto))
    }

    private fun validateUserUpdate(userDto: UpdateUserDto, user: User): User {
        log.info("UpdateUserDto validation started. Body: $userDto, user: $user")
        val password: String? = userDto.password
        val passwordConfirmation: String? = userDto.passwordConfirmation
        if (!password.isNullOrEmpty()) {
            if (passwordConfirmation.isNullOrEmpty()) {
                val message: String = messageSourceService.get(code = "password_confirmation_not_provided")
                log.error(message)
                throw ServerException(omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT,
                    variables = arrayOf(message), statusCode = HttpStatus.BAD_REQUEST)
            }
            if (passwordConfirmation != password) {
                val message: String = messageSourceService.get(code = "password_and_confirmation_not_matched")
                log.error(message)
                throw ServerException(omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT,
                    variables = arrayOf(message), statusCode = HttpStatus.BAD_REQUEST)
            }
            user.password = passwordEncoder.encode(password)
        }
        log.info("UpdateUserDto validated. Body: $userDto, user: $user")
        return user
    }

    @DeleteMapping("/oauth2/{service}")
    @Operation(summary = "Delete OAuth2 Relation Endpoint", tags = ["User"],
        responses = [
            ApiResponse(responseCode = "204", description = "successful operation",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)]),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class))])
        ],
        security = [SecurityRequirement(name = securitySchemeName, scopes = [ADMIN, USER])]
    )
    @Throws(IOException::class)
    fun removeOAuth2(
        @Parameter(description = "Identifier of the Service", `in` = ParameterIn.PATH) @PathVariable service: String
    ): ResponseEntity<Unit> {
        val user: User = userService.loggedInUser()
        val oAuth2Service: OAuth2Services = OAuth2Services.fromString(service = service)
        val oauth2NotFound: String = messageSourceService.get(code = "oauth2_not_found", params = arrayOf(oAuth2Service.service))
        when (oAuth2Service) {
            OAuth2Services.GOOGLE -> {
                if (user.oAuthGoogleUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthGoogleUser!!.user = null
            }
            OAuth2Services.FACEBOOK -> {
                if (user.oAuthFacebookUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthFacebookUser!!.user = null
            }
            OAuth2Services.GITHUB -> {
                if (user.oAuthGithubUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthGithubUser!!.user = null
            }
            OAuth2Services.LINKEDIN -> {
                if (user.oAuthLinkedinUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthLinkedinUser!!.user = null
            }
            OAuth2Services.TWITTER -> {
                if (user.oAuthTwitterUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthTwitterUser!!.user = null
            }
            OAuth2Services.SPOTIFY -> {
                if (user.oAuthSpotifyUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthSpotifyUser!!.user = null
            }
            OAuth2Services.TWITCH -> {
                if (user.oAuthTwitchUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthTwitchUser!!.user = null
            }
            OAuth2Services.SLACK -> {
                if (user.oAuthSlackUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthSlackUser!!.user = null
            }
            OAuth2Services.DROPBOX -> {
                if (user.oAuthDropboxUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthDropboxUser!!.user = null
            }
            OAuth2Services.INSTAGRAM -> {
                if (user.oAuthInstagramUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthInstagramUser!!.user = null
            }
            OAuth2Services.PAYPAL -> {
                if (user.oAuthPaypalUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthPaypalUser!!.user = null
            }
            OAuth2Services.DISCORD -> {
                if (user.oAuthDiscordUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthDiscordUser!!.user = null
            }
            OAuth2Services.OKTA -> {
                if (user.oAuthOktaUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthOktaUser!!.user = null
            }
            OAuth2Services.REDDIT -> {
                if (user.oAuthRedditUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthRedditUser!!.user = null
            }
            OAuth2Services.TIKTOK -> {
                if (user.oAuthTiktokUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthTiktokUser!!.user = null
            }
            OAuth2Services.BOX -> {
                if (user.oAuthBoxUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthBoxUser!!.user = null
            }
            OAuth2Services.VIMEO -> {
                if (user.oAuthVimeoUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthVimeoUser!!.user = null
            }
            OAuth2Services.GITLAB -> {
                if (user.oAuthGitlabUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthGitlabUser!!.user = null
            }
            OAuth2Services.ASANA -> {
                if (user.oAuthAsanaUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthAsanaUser!!.user = null
            }
            OAuth2Services.FOURSQUARE -> {
                if (user.oAuthFoursquareUser == null) {
                    log.error(oauth2NotFound)
                    throw NotFoundException(variables = arrayOf(oauth2NotFound))
                }
                user.oAuthFoursquareUser!!.user = null
            }
        }
        userService.save(user = user)
        return ResponseEntity.noContent().build()
    }
}