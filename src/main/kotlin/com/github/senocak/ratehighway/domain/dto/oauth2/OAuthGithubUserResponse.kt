package com.github.senocak.ratehighway.domain.dto.oauth2

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.senocak.ratehighway.domain.dto.BaseDto
import io.swagger.v3.oas.annotations.media.Schema

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