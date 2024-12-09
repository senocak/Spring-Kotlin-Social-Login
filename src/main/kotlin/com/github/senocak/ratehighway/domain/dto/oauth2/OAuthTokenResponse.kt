package com.github.senocak.ratehighway.domain.dto.oauth2

data class OAuthTokenResponse(
    var access_token: String? = null,
    var expires_in: Long? = null,
    var scope: String? = null,
    var token_type: String? = null,
    var id_token: String? = null,
)