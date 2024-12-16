package com.github.senocak.ratehighway.service.oauth2

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.senocak.ratehighway.domain.OAuthOktaUser
import com.github.senocak.ratehighway.domain.OAuthOktaUserRepository
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
class OAuthOktaService(
    @Qualifier(value = "restTemplateByPassSSL")
    private val restTemplate: RestTemplate,
    private val oAuthOktaUserRepository: OAuthOktaUserRepository,
    private val messageSourceService: MessageSourceService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val roleService: RoleService,
    private val oAuth2ClientProperties: OAuth2ClientProperties
): OAuthUserServiceImpl<OAuthOktaUser, OAuthOktaUserRepository>(
    repository = oAuthOktaUserRepository,
    messageSourceService = messageSourceService,
    jwtTokenProvider = jwtTokenProvider,
    userService = userService,
    roleService = roleService,
    passwordEncoder = passwordEncoder
) {
    private val registration: OAuth2ClientProperties.Registration = oAuth2ClientProperties.registration["okta"] ?: throw Exception("Registration not found")
    private val provider: OAuth2ClientProperties.Provider = oAuth2ClientProperties.provider["okta"] ?: throw Exception("Provider not found")

    override fun getClassName(): String? = OAuthOktaUser::class.simpleName

    override fun getUser(entity: OAuthOktaUser): User {
        val userRole: Role? = roleService.findByName(roleName = RoleName.ROLE_USER)
        return User(email = entity.email!!, password = passwordEncoder.encode(entity.email!!), roles = mutableListOf(userRole!!))
    }

    /**
     * Retrieves an OAuth token from Okta using the provided authorization code.
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
     * Retrieves user information from Discord using the provided access token.
     * @param oAuthTokenResponse The access token and token type to use for user info retrieval.
     * @return An OAuthDiscordUser object containing the user's information.
     */
    override fun getUserInfo(oAuthTokenResponse: OAuthTokenResponse): OAuthOktaUser {
        val headers: HttpHeaders = createHeaderForToken(token_type = oAuthTokenResponse.token_type, accessToken = oAuthTokenResponse.access_token!!)
        val response: ResponseEntity<DTO.Root> = restTemplate.exchange(provider.userInfoUri,
            HttpMethod.GET, HttpEntity(null, headers), DTO.Root::class.java)
        val body: DTO.Root = response.body
            ?: throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                statusCode = HttpStatus.FORBIDDEN, variables = arrayOf("ex"))
                .also { log.error("Body is returned as null, throwing ServerException, $it") }
        return OAuthOktaUser().also {
            it.id = body.id
            it.status = body.status
            it.firstName = body.profile.firstName
            it.lastName = body.profile.lastName
            it.email = body.profile.email
        }
    }

    val link: String = "https://trial-7049139.okta.com/oauth2/v1/authorize?client_id=${registration.clientId}&response_type=code&response_mode=fragment&scope=${registration.scope.joinToString(separator = " ")}&redirect_uri=${registration.redirectUri}&nonce=${UUID.randomUUID()}&state=${UUID.randomUUID()}"
}
object DTO {
    data class Root(
        val id: String,
        val status: String,
        val created: String,
        val activated: String,
        val statusChanged: String,
        val lastLogin: String,
        val lastUpdated: String,
        val passwordChanged: String,
        val type: Type,
        val profile: Profile,
        val credentials: Credentials,
        @JsonProperty("_links")
        val links: Links,
    )

    data class Type(val id: String, )
    data class Profile(
        val firstName: String,
        val lastName: String,
        val mobilePhone: Any?,
        val secondEmail: Any?,
        val login: String,
        val email: String,
    )
    data class Credentials(val password: Map<String, Any>, val provider: Provider)
    data class Provider(val type: String, val name: String)
    data class Links(
        val suspend: Suspend,
        val schema: Schema,
        val resetPassword: ResetPassword,
        val forgotPassword: ForgotPassword,
        val expirePassword: ExpirePassword,
        val changeRecoveryQuestion: ChangeRecoveryQuestion,
        val self: Self,
        val resetFactors: ResetFactors,
        val type: Type2,
        val changePassword: ChangePassword,
        val deactivate: Deactivate,
    )
    data class Suspend(val href: String, val method: String)
    data class Schema(val href: String)
    data class ResetPassword(val href: String, val method: String)
    data class ForgotPassword(val href: String, val method: String)
    data class ExpirePassword(val href: String, val method: String)
    data class ChangeRecoveryQuestion(val href: String, val method: String)
    data class Self(val href: String)
    data class ResetFactors(val href: String, val method: String)
    data class Type2(val href: String)
    data class ChangePassword(val href: String, val method: String)
    data class Deactivate(val href: String, val method: String)
}