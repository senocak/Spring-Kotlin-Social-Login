package com.github.senocak.ratehighway.domain.dto.oauth2

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.senocak.ratehighway.domain.dto.BaseDto
import io.swagger.v3.oas.annotations.media.Schema

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