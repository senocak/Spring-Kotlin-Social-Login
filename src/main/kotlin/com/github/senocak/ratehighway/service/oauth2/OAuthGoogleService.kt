package com.github.senocak.ratehighway.service.oauth2

import com.github.senocak.ratehighway.domain.dto.oauth2.OAuthTokenResponse
import com.github.senocak.ratehighway.domain.OAuthGoogleUser
import com.github.senocak.ratehighway.domain.Role
import com.github.senocak.ratehighway.domain.User
import com.github.senocak.ratehighway.exception.ServerException
import com.github.senocak.ratehighway.domain.OAuthGoogleUserRepository
import com.github.senocak.ratehighway.security.JwtTokenProvider
import com.github.senocak.ratehighway.service.MessageSourceService
import com.github.senocak.ratehighway.service.RoleService
import com.github.senocak.ratehighway.service.UserService
import com.github.senocak.ratehighway.util.OmaErrorMessageType
import com.github.senocak.ratehighway.util.RoleName
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
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

@Service
class OAuthGoogleService(
    @Qualifier(value = "restTemplateByPassSSL")
    private val restTemplate: RestTemplate,
    private val oAuthGoogleUserRepository: OAuthGoogleUserRepository,
    private val messageSourceService: MessageSourceService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val roleService: RoleService
): OAuthUserServiceImpl<OAuthGoogleUser, OAuthGoogleUserRepository>(
    repository = oAuthGoogleUserRepository,
    messageSourceService = messageSourceService,
    jwtTokenProvider = jwtTokenProvider,
    userService = userService,
    roleService = roleService,
    passwordEncoder = passwordEncoder
) {
    @Value("\${spring.security.oauth2.client.registration.google.clientId}") private lateinit var googleClientId: String
    @Value("\${spring.security.oauth2.client.registration.google.clientSecret}") private lateinit var googleClientSecret: String
    @Value("\${spring.security.oauth2.client.registration.google.redirectUri}") private lateinit var googleRedirectUri: String
    @Value("\${spring.security.oauth2.client.provider.google.tokenUri}") private lateinit var googleTokenUri: String
    @Value("\${spring.security.oauth2.client.provider.google.userInfoUri}") private lateinit var googleUserInfoUri: String

    override fun getClassName(): String? = OAuthGoogleUser::class.simpleName

    override fun getUser(entity: OAuthGoogleUser): User {
        val userRole: Role? = roleService.findByName(RoleName.ROLE_USER)
        return User(email = entity.email!!, password = passwordEncoder.encode(entity.email!!), roles = mutableListOf(userRole!!))
    }

    /**
     * Retrieves an OAuth token from Google using the provided authorization code.
     * @param code The authorization code to use for token retrieval.
     * @return An OAuthTokenResponse containing the access token and related information.
     */
    fun getGoogleToken(code: String): OAuthTokenResponse {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("code", code)
        map.add("client_id", googleClientId)
        map.add("client_secret", googleClientSecret)
        map.add("redirect_uri", googleRedirectUri)
        map.add("grant_type", "authorization_code")

        val response: ResponseEntity<OAuthTokenResponse> = restTemplate.exchange(googleTokenUri,
            HttpMethod.POST, HttpEntity(map, headers), OAuthTokenResponse::class.java)

        if (response.body == null) {
            log.error("Body is returned as null, throwing ServerException")
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("ex"))
        }
        return response.body!!
    }

    /**
     * Retrieves user information from Google using the provided access token.
     * @param accessToken The access token to use for user info retrieval.
     * @return An OAuthGoogleUser object containing the user's information.
     */
    fun getGoogleUserInfo(accessToken: String): OAuthGoogleUser {
        val response: ResponseEntity<OAuthGoogleUser> = restTemplate.getForEntity(
            "$googleUserInfoUri?alt=json&access_token=$accessToken", OAuthGoogleUser::class.java)

        return response.body
            ?: throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("ex"))
                .also { log.error("Body is returned as null, throwing ServerException $it") }
    }
}