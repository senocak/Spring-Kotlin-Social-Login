package com.github.senocak.ratehighway.service.oauth2

import com.github.senocak.ratehighway.domain.OAuthTiktokUser
import com.github.senocak.ratehighway.domain.OAuthTiktokUserRepository
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
class OAuthTiktokService(
    @Qualifier(value = "restTemplateByPassSSL")
    private val restTemplate: RestTemplate,
    private val oAuthTiktokUserRepository: OAuthTiktokUserRepository,
    private val messageSourceService: MessageSourceService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val roleService: RoleService,
    private val oAuth2ClientProperties: OAuth2ClientProperties
): OAuthUserServiceImpl<OAuthTiktokUser, OAuthTiktokUserRepository>(
    repository = oAuthTiktokUserRepository,
    messageSourceService = messageSourceService,
    jwtTokenProvider = jwtTokenProvider,
    userService = userService,
    roleService = roleService,
    passwordEncoder = passwordEncoder
) {
    private val registration: OAuth2ClientProperties.Registration = oAuth2ClientProperties.registration["tiktok"] ?: throw Exception("Registration not found")
    private val provider: OAuth2ClientProperties.Provider = oAuth2ClientProperties.provider["tiktok"] ?: throw Exception("Provider not found")

    override fun getClassName(): String? = OAuthTiktokUser::class.simpleName

    override fun getUser(entity: OAuthTiktokUser): User {
        val userRole: Role? = roleService.findByName(roleName = RoleName.ROLE_USER)
        return User(email = entity.email!!, password = passwordEncoder.encode(entity.email!!), roles = mutableListOf(userRole!!))
    }

    /**
     * Retrieves an OAuth token from Tiktok using the provided authorization code.
     * @param code The authorization code to use for token retrieval.
     * @return An OAuthTokenResponse containing the access token and related information.
     */
    override fun getToken(code: String): OAuthTokenResponse {
        val headers: HttpHeaders = HttpHeaders()
            .also { h: HttpHeaders ->
                h.contentType = MediaType.APPLICATION_FORM_URLENCODED
            }
        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("client_key", registration.clientId)
        map.add("client_secret", registration.clientSecret)
        map.add("code", code)
        map.add("grant_type", "authorization_code")
        map.add("redirect_uri", registration.redirectUri)

        val response: ResponseEntity<OAuthTokenResponse> = restTemplate.exchange(provider.tokenUri,
            HttpMethod.POST, HttpEntity(map, headers), OAuthTokenResponse::class.java)

        return response.body
            ?: throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("null", getClassName()))
                .also { log.error("Body is returned as null, throwing ServerException") }
    }

    /**
     * Retrieves user information from Tiktok using the provided access token.
     * @param accessToken The access token to use for user info retrieval.
     * @return An OAuthTiktokUser object containing the user's information.
     */
    fun getUserInfo(accessToken: String): OAuthTiktokUser {
        val entity: HttpEntity<MultiValueMap<String, String>> = HttpEntity(null, createHeaderForToken(accessToken = accessToken))
        val response: ResponseEntity<TiktokRoot> = restTemplate.exchange(provider.userInfoUri,
            HttpMethod.GET, entity, TiktokRoot::class.java)
        val body: TiktokRoot = response.body
            ?: throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("ex"))
                .also { log.error("Body is returned as null, throwing ServerException, $it") }
        body.data?.user?.id = body.data?.user?.union_id
        body.data?.user?.email = body.data?.user?.username
        if (body.error?.code != "ok")
            log.error("Code is not ok, ${body.error?.message}")
        return body.data?.user ?: throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
            statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("ex"))
            .also { log.error("User is returned as null, throwing ServerException, $it") }
    }

    val link: String = "https://www.tiktok.com/v2/auth/authorize?client_key=${registration.clientId}&scope=${registration.scope.joinToString(separator = ",")}&response_type=code&redirect_uri=${registration.redirectUri}&state=${UUID.randomUUID()}"
}

private class TiktokRoot {
    val data: TiktokData? = null
    val error: TiktokError? = null
}

private class TiktokData {
    val user: OAuthTiktokUser? = null
}

private class TiktokError {
    var code: String? = null
    var message: String? = null
    var log_id: String? = null
}
