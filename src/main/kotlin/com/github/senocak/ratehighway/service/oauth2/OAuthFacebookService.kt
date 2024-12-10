package com.github.senocak.ratehighway.service.oauth2

import com.fasterxml.jackson.databind.JsonNode
import com.github.senocak.ratehighway.domain.dto.oauth2.OAuthTokenResponse
import com.github.senocak.ratehighway.domain.OAuthFacebookUser
import com.github.senocak.ratehighway.domain.Role
import com.github.senocak.ratehighway.domain.User
import com.github.senocak.ratehighway.exception.ServerException
import com.github.senocak.ratehighway.domain.OAuthFacebookUserRepository
import com.github.senocak.ratehighway.security.JwtTokenProvider
import com.github.senocak.ratehighway.service.MessageSourceService
import com.github.senocak.ratehighway.service.RoleService
import com.github.senocak.ratehighway.service.UserService
import com.github.senocak.ratehighway.util.OmaErrorMessageType
import com.github.senocak.ratehighway.util.RoleName
import com.github.senocak.ratehighway.util.toUUID
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
class OAuthFacebookService(
    @Qualifier(value = "restTemplateByPassSSL")
    private val restTemplate: RestTemplate,
    private val oAuthFacebookUserRepository: OAuthFacebookUserRepository,
    private val messageSourceService: MessageSourceService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val roleService: RoleService,
    private val passwordEncoder: PasswordEncoder
): OAuthUserServiceImpl<OAuthFacebookUser, OAuthFacebookUserRepository>(
    repository = oAuthFacebookUserRepository,
    messageSourceService = messageSourceService,
    jwtTokenProvider = jwtTokenProvider,
    userService = userService,
    roleService = roleService,
    passwordEncoder = passwordEncoder
) {
    @Value("\${spring.security.oauth2.client.registration.facebook.clientId}") private lateinit var facebookClientId: String
    @Value("\${spring.security.oauth2.client.registration.facebook.clientSecret}") private lateinit var facebookClientSecret: String
    @Value("\${spring.security.oauth2.client.registration.facebook.redirectUri}") private lateinit var facebookRedirectUri: String
    @Value("\${spring.security.oauth2.client.provider.facebook.tokenUri}") private lateinit var facebookTokenUri: String
    @Value("\${spring.security.oauth2.client.provider.facebook.userInfoUri}") private lateinit var facebookUserInfoUri: String

    override fun getClassName(): String? = OAuthFacebookUser::class.simpleName

    override fun getUser(entity: OAuthFacebookUser): User {
        val userRole: Role? = roleService.findByName(roleName = RoleName.ROLE_USER)
        return User(email = entity.email!!, password = passwordEncoder.encode(entity.email!!), roles = mutableListOf(userRole!!))
    }

    /**
     * Retrieves an OAuth token from LinkedIn using the provided authorization code.
     * @param code The authorization code to use for token retrieval.
     * @return An OAuthTokenResponse containing the access token and related information.
     */
    override fun getToken(code: String): OAuthTokenResponse {
        val headers: HttpHeaders = HttpHeaders().also { h: HttpHeaders -> h.contentType = MediaType.APPLICATION_FORM_URLENCODED }

        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("code", code)
        map.add("client_id", facebookClientId)
        map.add("client_secret", facebookClientSecret)
        map.add("redirect_uri", facebookRedirectUri)
        map.add("grant_type", "authorization_code")

        val response: ResponseEntity<OAuthTokenResponse> = restTemplate.exchange(facebookTokenUri,
            HttpMethod.POST, HttpEntity(map, headers), OAuthTokenResponse::class.java)

        if (response.body == null) {
            log.error("Body is returned as null, throwing ServerException")
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("ex", getClassName()))
        }
        return response.body!!
    }

    /**
     * Retrieves user information from Facebook using the provided access token.
     * @param accessToken The access token to use for user info retrieval.
     * @return An OAuthLinkedinUser object containing the user's information.
     */
    fun getFacebookUserInfo(accessToken: String): OAuthFacebookUser {
        val entity: HttpEntity<MultiValueMap<String, String>> = HttpEntity(LinkedMultiValueMap(),
            createHeaderForToken(accessToken))

        val response: ResponseEntity<JsonNode> = restTemplate.exchange(facebookUserInfoUri,
            HttpMethod.GET, entity, JsonNode::class.java)

        val body: JsonNode = response.body
            ?: throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("ex"))
                .also { log.error("Body is returned as null, throwing ServerException $it") }

        val oAuthFacebookUser = OAuthFacebookUser()
        oAuthFacebookUser.id = if(body.get("id") != null) body.get("id").asText() else null
        oAuthFacebookUser.name = if(body.get("name") != null) body.get("name").asText() else null
        oAuthFacebookUser.email = if(body.get("email") != null) body.get("email").asText() else null
        val pictureNode: JsonNode? = body.get("picture")
        if(pictureNode != null) {
            val pictureDataNode: JsonNode? = pictureNode.get("data")
            if (pictureDataNode != null) {
                val pictureUrlNode: JsonNode? = pictureDataNode.get("url")
                if (pictureUrlNode != null)
                    oAuthFacebookUser.picture = pictureUrlNode.asText()
            }
        }
        return oAuthFacebookUser
    }
}