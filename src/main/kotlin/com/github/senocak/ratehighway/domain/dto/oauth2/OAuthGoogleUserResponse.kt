package com.github.senocak.ratehighway.domain.dto.oauth2

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.senocak.ratehighway.domain.dto.BaseDto
import io.swagger.v3.oas.annotations.media.Schema

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