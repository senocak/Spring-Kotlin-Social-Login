package com.github.senocak.ratehighway.domain.dto

import com.github.senocak.ratehighway.util.ValidEmail
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(
    @Schema(example = "anil@senocak.com", description = "Email of the user", required = true, name = "email", type = "String")
    @field:ValidEmail
    var email: String? = null,

    @Schema(description = "Password of the user", name = "password", type = "String", example = "password", required = true)
    @field:NotBlank
    @field:Size(min = 6, max = 20)
    var password: String? = null
): BaseDto()

data class RegisterRequest(
    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    @field:ValidEmail
    var email: String? = null,

    @Schema(example = "asenocak123", description = "Password of the user", required = true, name = "password", type = "String")
    @field:NotBlank
    @field:Size(min = 6, max = 20)
    var password: String? = null,
): BaseDto()
