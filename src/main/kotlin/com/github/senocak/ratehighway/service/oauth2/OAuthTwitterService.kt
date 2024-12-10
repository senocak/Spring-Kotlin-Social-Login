package com.github.senocak.ratehighway.service.oauth2

import com.github.senocak.ratehighway.domain.dto.oauth2.OAuthTokenResponse
import com.github.senocak.ratehighway.domain.OAuthTwitterUser
import com.github.senocak.ratehighway.domain.Role
import com.github.senocak.ratehighway.domain.User
import com.github.senocak.ratehighway.exception.ServerException
import com.github.senocak.ratehighway.domain.OAuthTwitterUserRepository
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
import java.util.UUID

@Service
class OAuthTwitterService(
    @Qualifier(value = "restTemplateByPassSSL")
    private val restTemplate: RestTemplate,
    private val oAuthTwitterUserRepository: OAuthTwitterUserRepository,
    private val messageSourceService: MessageSourceService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val roleService: RoleService
): OAuthUserServiceImpl<OAuthTwitterUser, OAuthTwitterUserRepository>(
    repository = oAuthTwitterUserRepository,
    messageSourceService = messageSourceService,
    jwtTokenProvider = jwtTokenProvider,
    userService = userService,
    roleService = roleService,
    passwordEncoder = passwordEncoder
) {
    @Value("\${spring.security.oauth2.client.registration.twitter.client-id}") private lateinit var twitterClientId: String
    @Value("\${spring.security.oauth2.client.registration.twitter.client-secret}") private lateinit var twitterClientSecret: String
    @Value("\${spring.security.oauth2.client.registration.twitter.redirect-uri}") private lateinit var twitterRedirectUri: String
    @Value("\${spring.security.oauth2.client.registration.twitter.scope}") private lateinit var twitterScopes: List<String>
    @Value("\${spring.security.oauth2.client.provider.twitter.token-uri}") private lateinit var twitterTokenUri: String
    @Value("\${spring.security.oauth2.client.provider.twitter.userInfoUri}") private lateinit var twitterUserInfoUri: String

    override fun getClassName(): String? = OAuthTwitterUser::class.simpleName

    override fun getUser(entity: OAuthTwitterUser): User {
        val userRole: Role? = roleService.findByName(roleName = RoleName.ROLE_USER)
        return User(email = entity.email!!, password = passwordEncoder.encode(entity.email!!), roles = mutableListOf(userRole!!))
    }

    /**
     * Retrieves an OAuth token from Twitter using the provided authorization code.
     * @param code The authorization code to use for token retrieval.
     * @return An OAuthTokenResponse containing the access token and related information.
     */
    override fun getToken(code: String): OAuthTokenResponse {
        val headers: HttpHeaders = HttpHeaders()
            .also { h: HttpHeaders ->
                h.contentType = MediaType.APPLICATION_FORM_URLENCODED
                h.setBasicAuth(twitterClientId, twitterClientSecret)
            }

        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("code", code)
        map.add("client_id", twitterClientId)
        map.add("redirect_uri", twitterRedirectUri)
        map.add("grant_type", "authorization_code")
        map.add("code_verifier", "challenge")
        map.add("scope", twitterScopes.joinToString(separator = ","))

        val response: ResponseEntity<OAuthTokenResponse> = restTemplate.exchange(twitterTokenUri,
            HttpMethod.POST, HttpEntity(map, headers), OAuthTokenResponse::class.java)

        return response.body ?:
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("null", getClassName())
        ).also { log.error("Body is returned as null, throwing ServerException") }
    }

    /**
     * Retrieves user information from LinkedIn using the provided access token.
     * @param accessToken The access token to use for user info retrieval.
     * @return An OAuthLinkedinUser object containing the user's information.
     */
    fun getUserInfo(accessToken: String): OAuthTwitterUser {
        val entity: HttpEntity<MultiValueMap<String, String>> = HttpEntity(LinkedMultiValueMap(), createHeaderForToken(accessToken = accessToken))
        val response: ResponseEntity<OAuthTwitterUserWrapper> = restTemplate.exchange(twitterUserInfoUri,
            HttpMethod.GET, entity, OAuthTwitterUserWrapper::class.java)
        val body: OAuthTwitterUser = response.body?.data
            ?: throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("ex"))
                .also { log.error("Body is returned as null, throwing ServerException, $it") }
        body.id = "${UUID.randomUUID()}"
        body.email = body.username
        return body
    }

    internal class OAuthTwitterUserWrapper {
        val data: OAuthTwitterUser? = null
    }
}