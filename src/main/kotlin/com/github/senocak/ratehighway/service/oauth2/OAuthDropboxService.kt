package com.github.senocak.ratehighway.service.oauth2

import com.github.senocak.ratehighway.domain.OAuthDropboxUser
import com.github.senocak.ratehighway.domain.OAuthDropboxUserRepository
import com.github.senocak.ratehighway.domain.Role
import com.github.senocak.ratehighway.domain.User
import com.github.senocak.ratehighway.domain.dto.OAuthTokenResponse
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
class OAuthDropboxService(
    @Qualifier(value = "restTemplateByPassSSL")
    private val restTemplate: RestTemplate,
    private val oAuthDropboxUserRepository: OAuthDropboxUserRepository,
    private val messageSourceService: MessageSourceService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val roleService: RoleService,
    private val oAuth2ClientProperties: OAuth2ClientProperties
): OAuthUserServiceImpl<OAuthDropboxUser, OAuthDropboxUserRepository>(
    repository = oAuthDropboxUserRepository,
    messageSourceService = messageSourceService,
    jwtTokenProvider = jwtTokenProvider,
    userService = userService,
    roleService = roleService,
    passwordEncoder = passwordEncoder
) {
    private val registration: OAuth2ClientProperties.Registration = oAuth2ClientProperties.registration["dropbox"] ?: throw Exception("Registration not found")
    private val provider: OAuth2ClientProperties.Provider = oAuth2ClientProperties.provider["dropbox"] ?: throw Exception("Provider not found")

    override fun getClassName(): String? = OAuthDropboxUser::class.simpleName

    override fun getUser(entity: OAuthDropboxUser): User {
        val userRole: Role? = roleService.findByName(roleName = RoleName.ROLE_USER)
        return User(email = entity.email!!, password = passwordEncoder.encode(entity.email!!), roles = mutableListOf(userRole!!))
    }

    /**
     * Retrieves an OAuth token from Slack using the provided authorization code.
     * @param code The authorization code to use for token retrieval.
     * @return An OAuthTokenResponse containing the access token and related information.
     */
    override fun getToken(code: String): OAuthTokenResponse {
        val headers: HttpHeaders = HttpHeaders()
            .also { h: HttpHeaders ->
                h.contentType = MediaType.APPLICATION_FORM_URLENCODED
            }
        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("code", code)
        map.add("client_id", registration.clientId)
        map.add("redirect_uri", registration.redirectUri)
        map.add("client_secret", registration.clientSecret)
        map.add("grant_type", "authorization_code")

        val response: ResponseEntity<OAuthTokenResponse> = restTemplate.exchange(provider.tokenUri,
            HttpMethod.POST, HttpEntity(map, headers), OAuthTokenResponse::class.java)

        val body: OAuthTokenResponse = response.body ?:
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("null", getClassName())
        ).also { log.error("Body is returned as null, throwing ServerException") }
        return body
    }

    /**
     * Retrieves user information from Dropbox using the provided access token.
     * @param oAuthTokenResponse The access token and token type to use for user info retrieval.
     * @return An OAuthDropboxUser object containing the user's information.
     */
    override fun getUserInfo(oAuthTokenResponse: OAuthTokenResponse): OAuthDropboxUser {
        val headers: HttpHeaders = createHeaderForToken(token_type = oAuthTokenResponse.token_type, accessToken = oAuthTokenResponse.access_token!!)
        val response: ResponseEntity<OAuthDropboxUser> = restTemplate.exchange(provider.userInfoUri,
            HttpMethod.POST, HttpEntity(null, headers), OAuthDropboxUser::class.java)
        val body: OAuthDropboxUser = response.body
            ?: throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("ex"))
                .also { log.error("Body is returned as null, throwing ServerException, $it") }
        body.id = body.email
        return body
    }

    val link: String = "https://www.dropbox.com/oauth2/authorize?client_id=${registration.clientId}&response_type=code&token_access_type=online&redirect_uri=${registration.redirectUri}&state=${UUID.randomUUID()}"
}
