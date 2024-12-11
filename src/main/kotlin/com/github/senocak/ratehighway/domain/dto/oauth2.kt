package com.github.senocak.ratehighway.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.senocak.ratehighway.domain.DropboxName
import com.github.senocak.ratehighway.domain.LocaleResponse
import io.swagger.v3.oas.annotations.media.Schema

data class OAuthTokenResponse(
    var access_token: String? = null,
    var expires_in: Long? = null,
    var scope: String? = null,
    var token_type: String? = null,
    var id_token: String? = null,
    var refresh_token: String? = null,
    var account_id: String? = null, // for slack
    var uid: String? = null, // for dropbox
    var user_id: String? = null, // for instagram
    var permissions: List<String>? = null, // for instagram
)

@JsonPropertyOrder("email", "name", "picture")
class OAuthFacebookUserResponse: BaseDto() {

    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    var email: String? = null

    @Schema(example = "Anıl", description = "Name of the user", required = true, name = "name", type = "String")
    var name: String? = null

    @Schema(example = "https://lh3.googleusercontent.com/a/AEdFTp6zIi4E1-SCPvvjI-9PfCDNxH8kDOqxGLGEokx0=s96-c",
        description = "Facebook picture url", required = true, name = "picture", type = "String")
    var picture: String? = null

    override fun toString(): String = "OAuthFacebookUserResponse(email=$email, name=$name, picture=$picture)"
}

@JsonPropertyOrder("name", "email")
class OAuthGithubUserResponse: BaseDto() {

    @Schema(example = "Anıl", description = "Name of the user", required = true, name = "name", type = "String")
    var name: String? = null

    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    var email: String? = null

    @Schema(example = "senocak", description = "Username of the user", required = true, name = "username", type = "String")
    var username: String? = null

    @Schema(example = "https://github.com/senocak", description = "Url of the user", required = true, name = "url", type = "String")
    var url: String? = null

    override fun toString(): String = "OAuthGithubUserResponse(name=$name, email=$email, username=$username, url=$url)"
}


@JsonPropertyOrder("email", "verified_email", "name", "given_name", "link", "picture", "locale", "hd")
class OAuthGoogleUserResponse: BaseDto() {

    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    var email: String? = null

    @Schema(example = "true", description = "Is email verified", required = true, name = "verified_email", type = "String")
    var verified_email: String? = null

    @Schema(example = "Anıl", description = "Name of the user", required = true, name = "name", type = "String")
    var name: String? = null

    @Schema(example = "Anıl", description = "Given name of the user", required = true, name = "given_name", type = "String")
    var given_name: String? = null

    @Schema(example = "https://plus.google.com/107603524648170943430", description = "Google link of the user", required = true, name = "link", type = "String")
    var link: String? = null

    @Schema(example = "https://lh3.googleusercontent.com/a/AEdFTp6zIi4E1-SCPvvjI-9PfCDNxH8kDOqxGLGEokx0=s96-c",
        description = "Google picture url", required = true, name = "picture", type = "String")
    var picture: String? = null

    @JsonProperty("locale")
    @Schema(example = "en-GB", description = "User locale", required = true, name = "locale", type = "String")
    var locale_user: String? = null

    @Schema(example = "bilgimedya.com.tr",
        description = "Hd of user", required = true, name = "hd", type = "String")
    var hd: String? = null

    override fun toString(): String = "OAuthGoogleUserResponse(email=$email, verified_email=$verified_email, name=$name," +
            "given_name=$given_name, link=$link, picture=$picture, locale_user=$locale_user, hd=$hd)"
}

@JsonPropertyOrder("sub", "emailVerified", "localeData", "givenName", "picture", "name", "familyName", "email")
class OAuthLinkedinUserResponse: BaseDto() {

    @Schema(example = "QQ01pihP4J", description = "Sub of the user", required = true, name = "sub", type = "String")
    var sub: String? = null

    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    var email: String? = null

    @Schema(example = "Anıl Şenocak", description = "Given name of the user", required = true, name = "given_name", type = "String")
    var givenName: String? = null

    @Schema(example = "true", description = "Email Verified of the user", required = true, name = "emailVerified", type = "Boolean")
    var emailVerified: Boolean? = null

    @Schema(example = "Anıl Şenocak", description = "Given name of the user", required = true, name = "given_name", type = "String")
    var picture: String? = null

    @Schema(example = "Anıl", description = "Given name of the user", required = true, name = "name", type = "String")
    var name: String? = null

    @Schema(example = "Şenocak", description = "Family name of the user", required = true, name = "familyName", type = "String")
    var familyName: String? = null

    var localeData: LocaleResponse? = null

    override fun toString(): String =
        "OAuthLinkedinUserResponse(sub=$sub, email=$email, givenName=$givenName, emailVerified=$emailVerified," +
                "picture=$picture, name=$name, familyName=$familyName, locale_data=$localeData)"
}

class OAuthTwitterUserResponse: BaseDto() {

    @Schema(example = "Anıl", description = "Given name of the user", required = true, name = "name", type = "String")
    var name: String? = null

    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    var email: String? = null

    @Schema(example = "Lorem Ipsum", description = "Description of the user", required = true, name = "description", type = "String")
    var description: String? = null

    @Schema(example = "lorem", description = "Username of the user", required = true, name = "username", type = "String")
    var username: String? = null

    @Schema(example = "", description = "Verified type of the user", required = false, name = "verified_type", type = "String")
    var verified_type: String? = null

    @Schema(example = "Anıl Şenocak", description = "Given name of the user", required = true, name = "given_name", type = "String")
    var profile_image_url: String? = null

    @Schema(example = "false", description = "Family name of the user", required = false, name = "protected", type = "Boolean")
    var protected: Boolean? = null

    override fun toString(): String =
        "OAuthTwitterUserResponse(name=$name, email=$email, description=$description, username=$username, verified_type=$verified_type, profile_image_url=$profile_image_url, protected=$protected)"
}

class OAuthSpotifyUserResponse: BaseDto() {
    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    var email: String? = null

    @Schema(example = "Lorem Ipsum", description = "Display name of the user", required = true, name = "display_name", type = "String")
    var display_name: String? = null

    @Schema(example = "TR", description = "Country of the user", required = true, name = "country", type = "String")
    var country: String? = null

    override fun toString(): String =
        "OAuthSpotifyUserResponse(email=$email, display_name=$display_name, country=$country)"
}

class OAuthTwitchUserResponse: BaseDto() {
    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    var email: String? = null

    @Schema(example = "Lorem Ipsum", description = "Display name of the user", required = true, name = "display_name", type = "String")
    var display_name: String? = null

    @Schema(example = "Anıl Şenocak", description = "Given name of the user", required = true, name = "given_name", type = "String")
    var profile_image_url: String? = null

    override fun toString(): String =
        "OAuthTwitchUserResponse(email=$email, display_name=$display_name, profile_image_url=$profile_image_url)"
}

class OAuthSlackUserResponse: BaseDto() {
    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    var email: String? = null

    @Schema(example = "Lorem Ipsum", description = "Name of the user", required = true, name = "name", type = "String")
    var name: String? = null

    @Schema(example = "http://...", description = "Picture of the user", required = true, name = "given_name", type = "String")
    var picture: String? = null

    @Schema(example = "TR", description = "Locale of the user", required = true, name = "locale", type = "String")
    @JsonProperty("locale")
    var localeSlack: String? = null

    override fun toString(): String =
        "OAuthSlackUserResponse(email=$email, name=$name, picture=$picture, localeSlack=$localeSlack)"
}

class OAuthDropboxUserResponse: BaseDto() {
    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    var email: String? = null

    @Schema(description = "Name of the user", required = true, name = "name", type = "DropboxName")
    var name: DropboxName? = null

    @Schema(description = "Picture of the user", required = true, name = "profile_photo_url", type = "String")
    var profile_photo_url: String? = null

    @Schema(example = "TR", description = "Country of the user", required = true, name = "country", type = "String")
    var country: String? = null

    @Schema(example = "TR", description = "Locale of the user", required = true, name = "locale", type = "String")
    @JsonProperty("locale")
    var localeSlack: String? = null

    override fun toString(): String =
        "OAuthSlackUserResponse(email=$email, name=$name, country=$country, localeSlack=$localeSlack)"
}

class OAuthInstagramUserResponse: BaseDto() {
    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    var email: String? = null

    @Schema(description = "Name of the user", required = true, name = "name", type = "DropboxName")
    var name: String? = null

    @Schema(description = "Picture of the user", required = true, name = "profile_picture_url", type = "String")
    var profile_picture_url: String? = null

    @Schema(example = "lorem", description = "Username of the user", required = true, name = "username", type = "String")
    var username: String? = null

    override fun toString(): String =
        "OAuthInstagramUserResponse(email=$email, name=$name, profile_picture_url=$profile_picture_url, username=$username)"
}
