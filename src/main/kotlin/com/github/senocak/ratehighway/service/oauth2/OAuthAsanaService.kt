package com.github.senocak.ratehighway.service.oauth2

import com.github.senocak.ratehighway.domain.OAuthAsanaUser
import com.github.senocak.ratehighway.domain.OAuthAsanaUserRepository
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
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.UUID

@Service
class OAuthAsanaService(
    @Qualifier(value = "restTemplateByPassSSL")
    private val restTemplate: RestTemplate,
    private val oAuthAsanaUserRepository: OAuthAsanaUserRepository,
    private val messageSourceService: MessageSourceService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val roleService: RoleService,
    private val oAuth2ClientProperties: OAuth2ClientProperties
): OAuthUserServiceImpl<OAuthAsanaUser, OAuthAsanaUserRepository>(
    repository = oAuthAsanaUserRepository,
    messageSourceService = messageSourceService,
    jwtTokenProvider = jwtTokenProvider,
    userService = userService,
    roleService = roleService,
    passwordEncoder = passwordEncoder
) {
    private val registration: OAuth2ClientProperties.Registration = oAuth2ClientProperties.registration["asana"] ?: throw Exception("Registration not found")
    private val provider: OAuth2ClientProperties.Provider = oAuth2ClientProperties.provider["asana"] ?: throw Exception("Provider not found")

    override fun getClassName(): String? = OAuthAsanaUser::class.simpleName

    override fun getUser(entity: OAuthAsanaUser): User {
        val userRole: Role? = roleService.findByName(roleName = RoleName.ROLE_USER)
        return User(email = entity.email!!, password = passwordEncoder.encode(entity.email!!), roles = mutableListOf(userRole!!))
    }

    /**
     * Retrieves an OAuth token from Asana using the provided authorization code.
     * @param code The authorization code to use for token retrieval.
     * @return An OAuthTokenResponse containing the access token and related information.
     */
    override fun getToken(code: String): OAuthTokenResponse {
        val url = "${provider.tokenUri}?client_id=${registration.clientId}&client_secret=${registration.clientSecret}&grant_type=authorization_code&code=$code&redirect_uri=${registration.redirectUri}"
        val response: ResponseEntity<OAuthTokenResponse> = restTemplate.exchange(url,
            HttpMethod.POST, HttpEntity(null, null), OAuthTokenResponse::class.java)
        return response.body
            ?: throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("null", getClassName()))
                .also { log.error("Body is returned as null, throwing ServerException") }
    }

    /**
     * Retrieves user information from Asana using the provided access token.
     * @param accessToken The access token to use for user info retrieval.
     * @return An OAuthAsanaUser object containing the user's information.
     */
    fun getUserInfo(oAuthTokenResponse: OAuthTokenResponse): OAuthAsanaUser {
        val headers: HttpHeaders = createHeaderForToken(token_type = oAuthTokenResponse.token_type!!, accessToken = oAuthTokenResponse.access_token!!)
        val response: ResponseEntity<AsanaUserRoot> = restTemplate.exchange(provider.userInfoUri,
            HttpMethod.GET, HttpEntity(null, headers), AsanaUserRoot::class.java)
        val data: OAuthAsanaUser = response.body?.data
            ?: throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("ex"))
                .also { log.error("Body is returned as null, throwing ServerException, $it") }
        data.id = data.gid
        return data
    }
    val link: String = "https://app.asana.com/-/oauth_authorize?client_id=${registration.clientId}&redirect_uri=${registration.redirectUri}&response_type=code&state=${UUID.randomUUID()}&scope=${registration.scope.joinToString(separator = " ")}"
}

class AsanaUserRoot {
    var data: OAuthAsanaUser? = null
}
