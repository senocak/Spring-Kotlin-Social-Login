package com.github.senocak.ratehighway.service.oauth2

import com.github.senocak.ratehighway.domain.OAuthPaypalUser
import com.github.senocak.ratehighway.domain.OAuthPaypalUserRepository
import com.github.senocak.ratehighway.domain.dto.OAuthTokenResponse
import com.github.senocak.ratehighway.domain.Role
import com.github.senocak.ratehighway.domain.User
import com.github.senocak.ratehighway.exception.ServerException
import com.github.senocak.ratehighway.security.JwtTokenProvider
import com.github.senocak.ratehighway.service.MessageSourceService
import com.github.senocak.ratehighway.service.RoleService
import com.github.senocak.ratehighway.service.UserService
import com.github.senocak.ratehighway.util.OmaErrorMessageType
import com.github.senocak.ratehighway.util.RoleName
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.util.UUID

@Service
class OAuthPaypalService(
    @Qualifier(value = "restTemplateByPassSSL")
    private val restTemplate: RestTemplate,
    private val oAuthPaypalUserRepository: OAuthPaypalUserRepository,
    private val messageSourceService: MessageSourceService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val roleService: RoleService,
    private val oAuth2ClientProperties: OAuth2ClientProperties
): OAuthUserServiceImpl<OAuthPaypalUser, OAuthPaypalUserRepository>(
    repository = oAuthPaypalUserRepository,
    messageSourceService = messageSourceService,
    jwtTokenProvider = jwtTokenProvider,
    userService = userService,
    roleService = roleService,
    passwordEncoder = passwordEncoder
) {
    private val registration: OAuth2ClientProperties.Registration = oAuth2ClientProperties.registration["paypal"] ?: throw Exception("Registration not found")
    private val provider: OAuth2ClientProperties.Provider = oAuth2ClientProperties.provider["paypal"] ?: throw Exception("Provider not found")

    override fun getClassName(): String? = OAuthPaypalUser::class.simpleName

    override fun getUser(entity: OAuthPaypalUser): User {
        val userRole: Role? = roleService.findByName(roleName = RoleName.ROLE_USER)
        return User(email = entity.email!!, password = passwordEncoder.encode(entity.email!!), roles = mutableListOf(userRole!!))
    }

    /**
     * Retrieves an OAuth token from Instagram using the provided authorization code.
     * @param code The authorization code to use for token retrieval.
     * @return An OAuthTokenResponse containing the access token and related information.
     */
    override fun getToken(code: String): OAuthTokenResponse =
        getToken(token = code, type = "code", grant_type = "authorization_code")

    fun getTokenByRefreshToken(refreshToken: String): OAuthTokenResponse =
        getToken(token = refreshToken, type = "refresh_token", grant_type = "refresh_token")

    private fun getToken(token: String, type: String, grant_type: String): OAuthTokenResponse {
        val headers: HttpHeaders = HttpHeaders()
            .also { h: HttpHeaders ->
                h.contentType = MediaType.APPLICATION_FORM_URLENCODED
                h.setBasicAuth(registration.clientId, registration.clientSecret)
            }
        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add(type, token)
        map.add("grant_type", grant_type)

        val response: ResponseEntity<OAuthTokenResponse> = restTemplate.exchange(provider.tokenUri,
            HttpMethod.POST, HttpEntity(map, headers), OAuthTokenResponse::class.java)

        return response.body
            ?: throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("null", getClassName()))
                .also { log.error("Body is returned as null, throwing ServerException") }
    }

    /**
     * Retrieves user information from PayPal using the provided access token.
     * @param oAuthTokenResponse The access token and token type to use for user info retrieval.
     * @return An OAuthPaypalUser object containing the user's information.
     */
    override fun getUserInfo(oAuthTokenResponse: OAuthTokenResponse): OAuthPaypalUser {
        val headers: HttpHeaders = createHeaderForToken(token_type = oAuthTokenResponse.token_type, accessToken = oAuthTokenResponse.access_token!!)
        val response: ResponseEntity<OAuthPaypalUser> = restTemplate.exchange("${provider.userInfoUri}&access_token=${oAuthTokenResponse.access_token}",
            HttpMethod.GET, HttpEntity(null, headers), OAuthPaypalUser::class.java)
        val body: OAuthPaypalUser = response.body
            ?: throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("ex"))
                .also { log.error("Body is returned as null, throwing ServerException, $it") }
        body.id = body.email
        return body
    }

    val link: String = "https://www.sandbox.paypal.com/connect/?flowEntry=static&client_id=${registration.clientId}&response_type=code&scope=${registration.scope.joinToString(separator = " ")}&redirect_uri=${registration.redirectUri}&state=${UUID.randomUUID()}"
}