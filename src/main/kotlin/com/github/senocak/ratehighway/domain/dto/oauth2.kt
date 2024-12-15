package com.github.senocak.ratehighway.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.senocak.ratehighway.domain.DropboxName
import com.github.senocak.ratehighway.domain.LocaleResponse
import com.github.senocak.ratehighway.domain.OAuthVimeoUserPictures
import com.github.senocak.ratehighway.domain.PayPalAddress
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
    var restricted_to: List<String>? = null, // for box
)

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

class OAuthPaypalUserResponse: BaseDto() {
    @Schema(example = "sub", description = "Sub of the user", required = true, name = "sub", type = "String")
    var sub: String? = null

    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    var email: String? = null

    @Schema(description = "Name of the user", required = true, name = "name", type = "String")
    var name: String? = null

    @Schema(example = "true", description = "Verified of the user", required = true, name = "verified", type = "String")
    var verified: String? = null

    @Schema(example = "true", description = "Email verified of the user", required = true, name = "email_verified", type = "String")
    var email_verified: String? = null

    @Schema(description = "address of the user", required = true, name = "address", type = "PayPalAddress")
    var address: PayPalAddress? = null

    override fun toString(): String =
        "OAuthPaypalUserResponse(sub=$sub, email=$email, name=$name, verified=$verified, email_verified=$email_verified, address=$address)"
}

class OAuthDiscordUserResponse: BaseDto() {
    @Schema(example = "lorem", description = "Username of the user", required = true, name = "username", type = "String")
    var username: String? = null

    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    var email: String? = null

    @Schema(description = "Name of the user", required = true, name = "name", type = "String")
    var global_name: String? = null

    @Schema(description = "Avatar of the user", required = true, name = "avatar", type = "String")
    var avatar: String? = null

    @Schema(example = "true", description = "Verified of the user", required = true, name = "verified", type = "Boolean")
    var verified: Boolean? = null

    @Schema(example = "true", description = "Mfa enabled of the user", required = true, name = "mfa_enabled", type = "Boolean")
    var mfa_enabled: Boolean? = null

    override fun toString(): String =
        "OAuthDiscordUserResponse(username=$username, email=$email, global_name=$global_name, avatar=$avatar, verified=$verified, mfa_enabled=$mfa_enabled)"
}

class OAuthOktaUserResponse: BaseDto() {
    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    var email: String? = null

    @Schema(description = "Name of the user", required = true, name = "name", type = "String")
    var name: String? = null

    @Schema(example = "ACTIVE", description = "Status of the user", required = true, name = "status", type = "String")
    var status: String? = null

    override fun toString(): String = "OAuthOktaUserResponse(email=$email, name=$name, status=$status)"
}

class OAuthRedditUserResponse: BaseDto() {
    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    var email: String? = null

    @Schema(description = "Name of the user", required = true, name = "name", type = "String")
    var name: String? = null

    @Schema(example = "true", description = "Verified of the user", required = true, name = "verified", type = "Boolean")
    var verified: Boolean? = null

    override fun toString(): String = "OAuthOktaUserResponse(email=$email, name=$name, verified=$verified)"
}

class OAuthTiktokUserResponse: BaseDto() {
    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    var email: String? = null

    @Schema(description = "Avatar large url of the user", required = true, name = "avatar_large_url", type = "String")
    var avatar_large_url: String? = null

    @Schema(description = "Avatar large url 100x100 of the user", required = true, name = "avatar_url_100", type = "String")
    var avatar_url_100: String? = null

    @Schema(example = "1", description = "Follower count of the user", required = true, name = "follower_count", type = "Int")
    var follower_count: Int? = null

    @Schema(example = "1", description = "Video count of the user", required = true, name = "video_count", type = "String")
    var video_count: Int? = null

    @Schema(description = "Avatar url of the user", required = true, name = "avatar_url", type = "String")
    var avatar_url: String? = null

    @Schema(example = "1", description = "Following count of the user", required = true, name = "following_count", type = "String")
    var following_count: Int? = null

    @Schema(example = "true", description = "Is verified of the user", required = true, name = "is_verified", type = "Boolean")
    var is_verified: Boolean? = null

    @Schema(example = "dasda123sd", description = "Open id of the user", required = true, name = "open_id", type = "String")
    var open_id: String? = null

    @Schema(example = "https://vm.tiktok.com/ZMkY4msCg/", description = "Profile deep link of the user", required = true, name = "profile_deep_link", type = "String")
    var profile_deep_link: String? = null

    @Schema(example = "Lorem", description = "Display name of the user", required = true, name = "display_name", type = "String")
    var display_name: String? = null

    @Schema(example = "faed212fsa", description = "Union id of the user", required = true, name = "union_id", type = "String")
    var union_id: String? = null

    @Schema(example = "lorem", description = "Username of the user", required = true, name = "username", type = "String")
    var username: String? = null

    @Schema(example = "lorem ipsum", description = "Bio description of the user", required = true, name = "bio_description", type = "String")
    var bio_description: String? = null

    @Schema(example = "1", description = "Likes count of the user", required = true, name = "likes_count", type = "String")
    var likes_count: Int? = null

    override fun toString(): String =
        "OAuthTiktokUserResponse(email=$email, avatar_large_url=$avatar_large_url, avatar_url_100=$avatar_url_100, follower_count=$follower_count, video_count=$video_count, avatar_url=$avatar_url, following_count=$following_count, is_verified=$is_verified, open_id=$open_id, profile_deep_link=$profile_deep_link, display_name=$display_name, union_id=$union_id, username=$username, bio_description=$bio_description, likes_count=$likes_count)"
}

class OAuthBoxUserResponse: BaseDto() {
    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    var email: String? = null

    @Schema(description = "Type url of the user", required = true, name = "type", type = "String")
    var type: String? = null

    @Schema(example = "Anıl", description = "Name of the user", required = true, name = "name", type = "String")
    var name: String? = null

    @Schema(description = "Avatar url of the user", required = true, name = "avatar_url", type = "String")
    var avatar_url: String? = null

    override fun toString(): String =
        "OAuthBoxUserResponse(email=$email, type=$type, name=$name, avatar_url=$avatar_url)"
}

class OAuthVimeoUserResponse: BaseDto() {
    @Schema(description = "Type url of the user", required = true, name = "account", type = "String")
    var account: String? = null

    @Schema(example = "Anıl", description = "Name of the user", required = true, name = "name", type = "String")
    var name: String? = null

    @Schema(description = "Url of the user", required = true, name = "link", type = "String")
    var link: String? = null

    @Schema(description = "Picture urls", required = true, name = "picture", type = "String")
    var pictures: OAuthVimeoUserPictures? = null

    override fun toString(): String = "OAuthBoxUserResponse(account=$account, name=$name, link=$link, pictures=$pictures)"
}
