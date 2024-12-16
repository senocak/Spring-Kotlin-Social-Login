package com.github.senocak.ratehighway.service.oauth2

import com.fasterxml.jackson.databind.JsonNode
import com.github.senocak.ratehighway.domain.OAuthGithubUser
import com.github.senocak.ratehighway.domain.Role
import com.github.senocak.ratehighway.domain.User
import com.github.senocak.ratehighway.exception.ServerException
import com.github.senocak.ratehighway.domain.OAuthGithubUserRepository
import com.github.senocak.ratehighway.domain.dto.OAuthTokenResponse
import com.github.senocak.ratehighway.security.JwtTokenProvider
import com.github.senocak.ratehighway.service.MessageSourceService
import com.github.senocak.ratehighway.service.RoleService
import com.github.senocak.ratehighway.service.UserService
import com.github.senocak.ratehighway.util.OmaErrorMessageType
import com.github.senocak.ratehighway.util.RoleName
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
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

@Service
class OAuthGithubService(
    @Qualifier(value = "restTemplateByPassSSL")
    private val restTemplate: RestTemplate,
    private val oAuthGithubUserRepository: OAuthGithubUserRepository,
    private val messageSourceService: MessageSourceService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val roleService: RoleService,
    private val oAuth2ClientProperties: OAuth2ClientProperties
): OAuthUserServiceImpl<OAuthGithubUser, OAuthGithubUserRepository>(
    repository = oAuthGithubUserRepository,
    messageSourceService = messageSourceService,
    jwtTokenProvider = jwtTokenProvider,
    userService = userService,
    roleService = roleService,
    passwordEncoder = passwordEncoder
) {
    private val registration: OAuth2ClientProperties.Registration = oAuth2ClientProperties.registration["github"] ?: throw Exception("Registration not found")
    private val provider: OAuth2ClientProperties.Provider = oAuth2ClientProperties.provider["github"] ?: throw Exception("Provider not found")
    @Value("\${spring.security.oauth2.client.provider.github.userEmailUri}") private lateinit var githubEmailUri: String

    override fun getClassName(): String? = OAuthGithubUser::class.simpleName

    override fun getUser(entity: OAuthGithubUser): User {
        val userRole: Role? = roleService.findByName(RoleName.ROLE_USER)
        return User(email = entity.email!!, password = passwordEncoder.encode(entity.email!!), roles = mutableListOf(userRole!!))
    }

    /**
     * Retrieves an OAuth token from GitHub using the provided authorization code.
     * @param code The authorization code to use for token retrieval.
     * @return An OAuthTokenResponse containing the access token and related information.
     */
    override fun getToken(code: String): OAuthTokenResponse {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("code", code)
        map.add("client_id", registration.clientId)
        map.add("client_secret", registration.clientSecret)
        map.add("redirect_uri", registration.redirectUri)

        val response: ResponseEntity<String> = restTemplate.exchange(provider.tokenUri,
            HttpMethod.POST, HttpEntity(map, headers), String::class.java)

        if (response.body == null) {
            log.error("Body is returned as null, throwing ServerException")
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("ex"))
        }
        val body: String = response.body!!

        val split: List<String> = body.split("&")
        val accessToken: String = split[0].split("=")[1]
        val scope: String = split[1].split("=")[1]
        val tokenType: String = split[2].split("=")[1]
        return OAuthTokenResponse(access_token = accessToken, scope = scope, token_type = tokenType)
    }

    /**
     * Retrieves user information from GitHub using the provided access token.
     * @param oAuthTokenResponse The access token and token type to use for user info retrieval.
     * @return An OAuthGithubUser object containing the user's information.
     */
    override fun getUserInfo(oAuthTokenResponse: OAuthTokenResponse): OAuthGithubUser {
        val headers: HttpHeaders = createHeaderForToken(token_type = oAuthTokenResponse.token_type, accessToken = oAuthTokenResponse.access_token!!)
        val response: ResponseEntity<OAuthGithubUser> = restTemplate.exchange(provider.userInfoUri,
            HttpMethod.GET, HttpEntity(LinkedMultiValueMap<String, String>(), headers), OAuthGithubUser::class.java)
        val body: OAuthGithubUser = response.body
            ?: throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("ex"))
                .also { log.error("Body is returned as null, throwing ServerException $it") }
        body.email = getGithubUserEmail(accessToken = oAuthTokenResponse.access_token!!)
        return body
    }

    /**
     * Retrieves user email from Github using the provided access token.
     * @param accessToken The access token to use for user info retrieval.
     * @return An OAuthLinkedinUser object containing the user's information.
     */
    private fun getGithubUserEmail(accessToken: String): String? {
        val entity: HttpEntity<MultiValueMap<String, String>> = HttpEntity(LinkedMultiValueMap(),
            createHeaderForToken(accessToken = accessToken))

        val response: ResponseEntity<JsonNode> = restTemplate.exchange(githubEmailUri,
            HttpMethod.GET, entity, JsonNode::class.java)

        val body: JsonNode = response.body
            ?: throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("ex"))
                .also { log.error("Body is returned as null, throwing ServerException $it") }

        if (body.isArray) {
            for (objNode: JsonNode in body) {
                val primary: JsonNode = objNode.get("primary")
                if (primary.asBoolean())
                    return if (objNode.get("email") != null) objNode.get("email").asText() else null
            }
        }
        return null
    }

    val link: String
        get() = "https://github.com/login?client_id=${registration.clientId}&return_to=/login/oauth/authorize?client_id=${registration.clientId}&redirect_uri=${registration.redirectUri}&response_type=code&scope=${registration.scope.joinToString(separator = " ")}&state=i7heCBa70vIHTBJ23zAtS5s8u0ERlApyOce3xZaeTug%3D"
}