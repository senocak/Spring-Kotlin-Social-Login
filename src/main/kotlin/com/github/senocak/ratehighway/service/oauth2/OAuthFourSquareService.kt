package com.github.senocak.ratehighway.service.oauth2

import com.github.senocak.ratehighway.domain.OAuthFoursquareUser
import com.github.senocak.ratehighway.domain.OAuthFoursquareUserRepository
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

@Service
class OAuthFourSquareService(
    @Qualifier(value = "restTemplateByPassSSL")
    private val restTemplate: RestTemplate,
    private val oAuthFoursquareUserRepository: OAuthFoursquareUserRepository,
    private val messageSourceService: MessageSourceService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val roleService: RoleService,
    private val oAuth2ClientProperties: OAuth2ClientProperties
): OAuthUserServiceImpl<OAuthFoursquareUser, OAuthFoursquareUserRepository>(
    repository = oAuthFoursquareUserRepository,
    messageSourceService = messageSourceService,
    jwtTokenProvider = jwtTokenProvider,
    userService = userService,
    roleService = roleService,
    passwordEncoder = passwordEncoder
) {
    private val registration: OAuth2ClientProperties.Registration = oAuth2ClientProperties.registration["foursquare"] ?: throw Exception("Registration not found")
    private val provider: OAuth2ClientProperties.Provider = oAuth2ClientProperties.provider["foursquare"] ?: throw Exception("Provider not found")

    override fun getClassName(): String? = OAuthFoursquareUser::class.simpleName

    override fun getUser(entity: OAuthFoursquareUser): User {
        val userRole: Role? = roleService.findByName(roleName = RoleName.ROLE_USER)
        return User(email = entity.email!!, password = passwordEncoder.encode(entity.email!!), roles = mutableListOf(userRole!!))
    }

    /**
     * Retrieves an OAuth token from Foursquare using the provided authorization code.
     * @param code The authorization code to use for token retrieval.
     * @return An OAuthTokenResponse containing the access token and related information.
     */
    override fun getToken(code: String): OAuthTokenResponse {
        val headers: HttpHeaders = HttpHeaders()
            .also { h: HttpHeaders ->
                h.contentType = MediaType.APPLICATION_FORM_URLENCODED
            }
        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("client_id", registration.clientId)
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
     * Retrieves user information from Foursquare using the provided access token.
     * @param oAuthTokenResponse The token object to use for user info retrieval.
     * @return An OAuthFoursquareUser object containing the user's information.
     */
    fun getUserInfo(oAuthTokenResponse: OAuthTokenResponse): OAuthFoursquareUser {
        val headers: HttpHeaders = createHeaderForToken(token_type = oAuthTokenResponse.token_type, accessToken = oAuthTokenResponse.access_token!!)
        val response: ResponseEntity<OAuthFoursquareUserWrapper> = restTemplate.exchange(provider.userInfoUri,
            HttpMethod.GET, HttpEntity(null, headers), OAuthFoursquareUserWrapper::class.java)
        val data: OAuthFoursquareUser = response.body?.response?.user
            ?: throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("ex"))
                .also { log.error("Body is returned as null, throwing ServerException, $it") }
        data.email = data.contact!!.contact_email
        return data
    }
    val link: String = "https://foursquare.com/oauth2/authenticate?client_id=${registration.clientId}&response_type=code&redirect_uri=${registration.redirectUri}"
}
private class OAuthFoursquareUserWrapper {
    var meta: OAuthFoursquareUserMeta? = null
    var notifications: List<OAuthFoursquareUserNotification>? = null
    var response: OAuthFoursquareUserResponse? = null
}
private class OAuthFoursquareUserMeta {
    var code: String? = null
    var requestId: String? = null
}
private class OAuthFoursquareUserNotification {
    var type: String? = null
    var item: OAuthFoursquareUserNotificationItem? = null
}
private class OAuthFoursquareUserNotificationItem {
    var unreadCount: Int? = null
}
private class OAuthFoursquareUserResponse {
    var user: OAuthFoursquareUser? = null
}