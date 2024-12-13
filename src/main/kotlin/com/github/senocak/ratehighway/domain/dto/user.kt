package com.github.senocak.ratehighway.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.senocak.ratehighway.domain.User
import com.github.senocak.ratehighway.util.PasswordMatches
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size
import org.springframework.data.domain.Page

class UserResponseDto: BaseDto() {
    @JsonProperty("name")
    @Schema(example = "Lorem Ipsum", description = "Name of the user", required = true, name = "name", type = "String")
    var name: String? = null

    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    var email: String? = null

    @Schema(example = "1670083512184", description = "Creation timestamp", required = true, name = "createdAt", type = "Long")
    var createdAt: Long? = null

    @ArraySchema(schema = Schema(example = "ROLE_USER", description = "Roles of the user", required = true, name = "roles"))
    var roles: MutableList<RoleResponse>? = null

    @Schema(required = false)
    var google: OAuthGoogleUserResponse? = null

    @Schema(required = false)
    var github: OAuthGithubUserResponse? = null

    @Schema(required = false)
    var linkedin: OAuthLinkedinUserResponse? = null

    @Schema(required = false)
    var facebook: OAuthFacebookUserResponse? = null

    @Schema(required = false)
    var twitter: OAuthTwitterUserResponse? = null

    @Schema(required = false)
    var spotify: OAuthSpotifyUserResponse? = null

    @Schema(required = false)
    var twitch: OAuthTwitchUserResponse? = null

    @Schema(required = false)
    var slack: OAuthSlackUserResponse? = null

    @Schema(required = false)
    var dropbox: OAuthDropboxUserResponse? = null

    @Schema(required = false)
    var instagram: OAuthInstagramUserResponse? = null

    @Schema(required = false)
    var paypal: OAuthPaypalUserResponse? = null

    @Schema(required = false)
    var discord: OAuthDiscordUserResponse? = null

    @Schema(required = false)
    var okta: OAuthOktaUserResponse? = null

    @Schema(required = false)
    var reddit: OAuthRedditUserResponse? = null

    override fun toString(): String =
        "UserResponseDto(name=$name, email=$email, createdAt=$createdAt, roles=$roles, google=$google, github=$github," +
                "linkedin=$linkedin, facebook=$facebook, twitter=$twitter, spotify=$spotify, twitch=$twitch, slack=$slack," +
                "dropbox=$dropbox, instagram=$instagram, paypal=$paypal, discord=$discord, okta=$okta, reddit=$reddit)"
}

@JsonPropertyOrder("token", "refreshToken", "user")
class UserResponseWrapperDto(
    @field:JsonProperty("user")
    @Schema(required = true)
    var userResponseDto: UserResponseDto,

    @Schema(example = "eyJraWQiOiJ...", description = "Jwt Token", required = true, name = "token", type = "String")
    var token: String? = null,
): BaseDto() {
    override fun toString(): String = "UserResponseWrapperDto('userResponseDto':'$userResponseDto', 'token':'$token')"
}

class UserPaginationDto(pageModel: Page<User>, items: List<UserResponseDto>) :
    PaginationResponse<User, UserResponseDto>(page = pageModel, items = items)

@PasswordMatches
data class UpdateUserDto(
    @Schema(example = "Anil123", description = "Password", name = "password", type = "String")
    @field:Size(min = 6, max = 20)
    var password: String? = null,

    @Schema(example = "Anil123", description = "Password confirmation", name = "password", type = "String")
    @field:Size(min = 6, max = 20)
    @JsonProperty("password_confirmation")
    var passwordConfirmation: String? = null
): BaseDto()