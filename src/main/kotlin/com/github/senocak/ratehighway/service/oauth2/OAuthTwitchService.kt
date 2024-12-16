package com.github.senocak.ratehighway.service.oauth2

import com.fasterxml.jackson.databind.JsonNode
import com.github.senocak.ratehighway.domain.OAuthTwitchUser
import com.github.senocak.ratehighway.domain.OAuthTwitchUserRepository
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
class OAuthTwitchService(
    @Qualifier(value = "restTemplateByPassSSL")
    private val restTemplate: RestTemplate,
    private val oAuthTwitchUserRepository: OAuthTwitchUserRepository,
    private val messageSourceService: MessageSourceService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val roleService: RoleService,
    private val oAuth2ClientProperties: OAuth2ClientProperties
): OAuthUserServiceImpl<OAuthTwitchUser, OAuthTwitchUserRepository>(
    repository = oAuthTwitchUserRepository,
    messageSourceService = messageSourceService,
    jwtTokenProvider = jwtTokenProvider,
    userService = userService,
    roleService = roleService,
    passwordEncoder = passwordEncoder
) {
    private val registration: OAuth2ClientProperties.Registration = oAuth2ClientProperties.registration["twitch"] ?: throw Exception("Registration not found")
    private val provider: OAuth2ClientProperties.Provider = oAuth2ClientProperties.provider["twitch"] ?: throw Exception("Provider not found")

    override fun getClassName(): String? = OAuthTwitchUser::class.simpleName

    override fun getUser(entity: OAuthTwitchUser): User {
        val userRole: Role? = roleService.findByName(roleName = RoleName.ROLE_USER)
        return User(email = entity.email!!, password = passwordEncoder.encode(entity.email!!), roles = mutableListOf(userRole!!))
    }

    /**
     * Retrieves an OAuth token from Twitch using the provided authorization code.
     * @param code The authorization code to use for token retrieval.
     * @return An OAuthTokenResponse containing the access token and related information.
     */
    override fun getToken(code: String): OAuthTokenResponse {
        val headers: HttpHeaders = HttpHeaders()
            .also { h: HttpHeaders ->
                h.contentType = MediaType.APPLICATION_FORM_URLENCODED
                h.setBasicAuth(registration.clientId, registration.clientSecret)
            }
        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("code", code)
        map.add("client_id", registration.clientId)
        map.add("client_secret", registration.clientSecret)
        map.add("grant_type", "authorization_code")
        map.add("redirect_uri", registration.redirectUri)

        val response: ResponseEntity<JsonNode> = restTemplate.exchange(provider.tokenUri,
            HttpMethod.POST, HttpEntity(map, headers), JsonNode::class.java)

        val body: JsonNode = response.body ?:
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("null", getClassName())
        ).also { log.error("Body is returned as null, throwing ServerException") }
        return OAuthTokenResponse(
            access_token = body["access_token"].asText(),
            expires_in = body["expires_in"].asLong(),
            scope = body["scope"].toList().joinToString { it.asText() },
            token_type = body["token_type"].asText(),
            id_token = body["id_token"].asText(),
            refresh_token = body["refresh_token"].asText()
        )
    }

    /**
     * Retrieves user information from LinkedIn using the provided access token.
     * @param oAuthTokenResponse The access token and token type to use for user info retrieval.
     * @return An OAuthLinkedinUser object containing the user's information.
     */
    override fun getUserInfo(oAuthTokenResponse: OAuthTokenResponse): OAuthTwitchUser {
        val headers: HttpHeaders = createHeaderForToken(token_type = oAuthTokenResponse.token_type, accessToken = oAuthTokenResponse.access_token!!)
            .also { h: HttpHeaders ->
                h.add("client-id", registration.clientId)
            }
        val response: ResponseEntity<OAuthTwitchUserWrapper> = restTemplate.exchange(provider.userInfoUri,
            HttpMethod.GET, HttpEntity(LinkedMultiValueMap<String, String>(), headers), OAuthTwitchUserWrapper::class.java)
        val body: OAuthTwitchUserWrapper = response.body
            ?: throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("ex"))
                .also { log.error("Body is returned as null, throwing ServerException, $it") }
        return body.data.first()
    }

    internal class OAuthTwitchUserWrapper(val data: List<OAuthTwitchUser>)

    val link: String = "https://id.twitch.tv/oauth2/authorize?response_type=code&client_id=${registration.clientId}&redirect_uri=${registration.redirectUri}&scope=${registration.scope.joinToString(separator = " ")}&state=${UUID.randomUUID()}&nonce=${UUID.randomUUID()}"
}
