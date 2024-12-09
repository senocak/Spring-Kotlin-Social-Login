package com.github.senocak.ratehighway.service.oauth2

import com.github.senocak.ratehighway.domain.dto.oauth2.OAuthTokenResponse
import com.github.senocak.ratehighway.domain.OAuthLinkedinUser
import com.github.senocak.ratehighway.domain.Role
import com.github.senocak.ratehighway.domain.User
import com.github.senocak.ratehighway.exception.ServerException
import com.github.senocak.ratehighway.domain.OAuthLinkedinUserRepository
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
class OAuthLinkedinService(
    @Qualifier(value = "restTemplateByPassSSL")
    private val restTemplate: RestTemplate,
    private val oAuthLinkedinUserRepository: OAuthLinkedinUserRepository,
    private val messageSourceService: MessageSourceService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val roleService: RoleService
): OAuthUserServiceImpl<OAuthLinkedinUser, OAuthLinkedinUserRepository>(
    repository = oAuthLinkedinUserRepository,
    messageSourceService = messageSourceService,
    jwtTokenProvider = jwtTokenProvider,
    userService = userService,
    roleService = roleService,
    passwordEncoder = passwordEncoder
) {
    @Value("\${spring.security.oauth2.client.registration.linkedin.clientId}") private lateinit var linkedinClientId: String
    @Value("\${spring.security.oauth2.client.registration.linkedin.clientSecret}") private lateinit var linkedinClientSecret: String
    @Value("\${spring.security.oauth2.client.registration.linkedin.redirectUri}") private lateinit var linkedinRedirectUri: String
    @Value("\${spring.security.oauth2.client.provider.linkedin.tokenUri}") private lateinit var linkedinTokenUri: String
    @Value("\${spring.security.oauth2.client.provider.linkedin.userInfoUri}") private lateinit var linkedinUserInfoUri: String

    override fun getClassName(): String? = OAuthLinkedinUser::class.simpleName

    override fun getUser(entity: OAuthLinkedinUser): User {
        val userRole: Role? = roleService.findByName(roleName = RoleName.ROLE_USER)
        return User(email = entity.email!!, password = passwordEncoder.encode(entity.email!!), roles = mutableListOf(userRole!!))
    }

    /**
     * Retrieves an OAuth token from Linkedin using the provided authorization code.
     * @param code The authorization code to use for token retrieval.
     * @return An OAuthTokenResponse containing the access token and related information.
     */
    fun getLinkedinToken(code: String): OAuthTokenResponse {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("code", code)
        map.add("client_id", linkedinClientId)
        map.add("client_secret", linkedinClientSecret)
        map.add("redirect_uri", linkedinRedirectUri)
        map.add("grant_type", "authorization_code")

        val response: ResponseEntity<OAuthTokenResponse> = restTemplate.exchange(linkedinTokenUri,
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
    fun getLinkedinUserInfo(accessToken: String): OAuthLinkedinUser {
        val entity: HttpEntity<MultiValueMap<String, String>> = HttpEntity(LinkedMultiValueMap(), createHeaderForToken(accessToken = accessToken))
        val response: ResponseEntity<OAuthLinkedinUser> = restTemplate.exchange(linkedinUserInfoUri,
            HttpMethod.GET, entity, OAuthLinkedinUser::class.java)
        val body: OAuthLinkedinUser = response.body
            ?: throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("ex"))
                .also { log.error("Body is returned as null, throwing ServerException, $it") }
        body.id = "${UUID.randomUUID()}"
        return body
    }
}