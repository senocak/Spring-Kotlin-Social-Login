package com.github.senocak.ratehighway.domain.dto.oauth2

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.senocak.ratehighway.domain.LocaleResponse
import com.github.senocak.ratehighway.domain.dto.BaseDto
import io.swagger.v3.oas.annotations.media.Schema

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

