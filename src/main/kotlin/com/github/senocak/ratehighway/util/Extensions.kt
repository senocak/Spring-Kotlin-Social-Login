package com.github.senocak.ratehighway.util

import ch.qos.logback.classic.Level
import com.github.senocak.ratehighway.KotlinApplication
import com.github.senocak.ratehighway.domain.LocaleResponse
import com.github.senocak.ratehighway.domain.dto.RoleResponse
import com.github.senocak.ratehighway.domain.dto.UserResponseDto
import com.github.senocak.ratehighway.domain.dto.oauth2.OAuthFacebookUserResponse
import com.github.senocak.ratehighway.domain.dto.oauth2.OAuthGithubUserResponse
import com.github.senocak.ratehighway.domain.dto.oauth2.OAuthGoogleUserResponse
import com.github.senocak.ratehighway.domain.dto.oauth2.OAuthLinkedinUserResponse
import com.github.senocak.ratehighway.domain.OAuthFacebookUser
import com.github.senocak.ratehighway.domain.OAuthGithubUser
import com.github.senocak.ratehighway.domain.OAuthGoogleUser
import com.github.senocak.ratehighway.domain.OAuthLinkedinUser
import com.github.senocak.ratehighway.domain.Role
import com.github.senocak.ratehighway.domain.User
import com.github.senocak.ratehighway.exception.ServerException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.text.Normalizer
import java.util.Objects
import java.util.Properties
import java.util.UUID
import java.util.regex.Pattern

/**
 * Converts a User object to a UserResponseDto object.
 * The function creates a new UserResponseDto object and populates its fields with the information from the User object.
 * If the topic parameter is true, the function also populates the topic information in the UserResponseDto object.
 * If the roles parameter is true, the function also populates the role information in the UserResponseDto object.
 *
 * @param roles: a boolean indicating whether to include the user's role information in the response.
 * @return a UserResponseDto object representing the User.
 */
fun User.toDTO(roles: Boolean = true): UserResponseDto {
    val dto = UserResponseDto()
    dto.email = this.email
    dto.createdAt = this.createdAt.time
    dto.roles = if(roles) this.roles.map { r: Role -> r.toDTO()}.toMutableList() else null
    if (this.oAuthGoogleUser != null) dto.google = this.oAuthGoogleUser!!.toDTO()
    if (this.oAuthGithubUser != null) dto.github = this.oAuthGithubUser!!.toDTO()
    if (this.oAuthLinkedinUser != null) dto.linkedin = this.oAuthLinkedinUser!!.toDTO()
    if (this.oAuthFacebookUser != null) dto.facebook = this.oAuthFacebookUser!!.toDTO()
    return dto
}

/**
 * Converts a Role object to a RoleResponse object.
 * @return A RoleResponse object that represents the input Role object.
 */
fun Role.toDTO(): RoleResponse = RoleResponse(name = this.name!!)

/**
 * Converts an OAuthGithubUser object to an OAuthGithubUserResponse object.
 *
 * @return An OAuthGithubUserResponse object that represents the input OAuthGithubUser object.
 */
fun OAuthGithubUser.toDTO(): OAuthGithubUserResponse {
    val dto = OAuthGithubUserResponse()
    dto.name = this.name
    dto.email = this.email
    dto.username = this.login
    dto.url = this.html_url
    return dto
}

/**
 * Converts an OAuthLinkedinUser object to an OAuthLinkedinUserResponse object.
 *
 * @return An OAuthLinkedinUserResponse object that represents the input OAuthLinkedinUser object.
 */
fun OAuthLinkedinUser.toDTO(): OAuthLinkedinUserResponse {
    val dto = OAuthLinkedinUserResponse()
    dto.sub = this.sub
    dto.name = this.name
    dto.givenName = this.given_name
    dto.familyName = this.family_name
    dto.email = this.email
    dto.emailVerified = this.email_verified
    dto.picture = this.picture
    dto.localeData = this.locale
    return dto
}

/**
 * Converts an OAuthFacebookUser object to an OAuthFacebookUserResponse object.
 *
 * @return An OAuthFacebookUserResponse object that represents the input OAuthFacebookUser object.
 */
fun OAuthFacebookUser.toDTO(): OAuthFacebookUserResponse {
    val dto = OAuthFacebookUserResponse()
    dto.email = this.email
    dto.name = this.name
    dto.picture = this.picture
    return dto
}

/**
 * Converts an OAuthGoogleUser object to an OAuthGoogleUserResponse object.
 *
 * @return An OAuthGoogleUserResponse object that represents the input OAuthGoogleUser object.
 */
fun OAuthGoogleUser.toDTO(): OAuthGoogleUserResponse {
    val dto = OAuthGoogleUserResponse()
    dto.email = this.email
    dto.verified_email = this.verified_email
    dto.name = this.name
    dto.given_name = this.given_name
    dto.link = this.link
    dto.picture = this.picture
    dto.locale_user = this.locale
    dto.hd = this.hd
    return dto
}

fun String.toUUID(): UUID = UUID.fromString(this)

fun <R : Any> R.logger(): Lazy<Logger> = lazy {
    LoggerFactory.getLogger((if (this.javaClass.kotlin.isCompanion) this.javaClass.enclosingClass else this.javaClass).name)
}

fun String?.logger(): ch.qos.logback.classic.Logger =
    LoggerFactory.getLogger(this ?: ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME)
            as ch.qos.logback.classic.Logger

/**
 * Validate the existence of a package in the classpath.
 * @return true if the package exists in the classpath, false otherwise.
 */
fun String.validatePackageExistence(): Boolean =
    KotlinApplication::class.java.classLoader
        .getResources("${this.replace(oldValue = ".", newValue = "/")}/")
        .hasMoreElements()

fun Logger.changeLevel(loglevel: String) {
    val logger: ch.qos.logback.classic.Logger = this as ch.qos.logback.classic.Logger
    logger.level = Level.toLevel(loglevel)
    this.debug("Logging level: ${this.name}")
    this.info("Logging level: ${this.name}")
    this.warn("Logging level: ${this.name}")
    this.error("Logging level: ${this.name}")
}

fun String.changeLevel(loglevel: String) {
    val log: ch.qos.logback.classic.Logger = this.logger()
    if (!this.validatePackageExistence()) {
        log.error("packageName: '$this' not found.")
        throw ServerException(omaErrorMessageType = OmaErrorMessageType.NOT_FOUND,
            variables = arrayOf("packageName: $this"), statusCode = HttpStatus.NOT_FOUND)
    }
    if (Objects.nonNull(loglevel)) {
        log.level = Level.toLevel(loglevel)
    }
    println(message = "Logging level: ${log.level}")
    log.trace("This is a trace message.")
    log.debug("This is a debug message.")
    log.info("This is an info message.")
    log.warn("This is a warn message.")
    log.error("This is an error message.")
}

fun loadBuildInfo(): Properties =
    Properties()
        .also { p: Properties ->
            p.load(KotlinApplication::class.java.getResourceAsStream("/build-info.properties"))
        }

fun String.fromProperties(): String = loadBuildInfo().getProperty(this)

/**
 * @return -- sluggable string variable
 */
fun String.toSlug(): String {
    val nonLatin: Pattern = Pattern.compile("[^\\w-]")
    val whiteSpace: Pattern = Pattern.compile("[\\s]")
    val noWhiteSpace: String = whiteSpace.matcher(this).replaceAll("-")
    val normalized: String = Normalizer.normalize(noWhiteSpace, Normalizer.Form.NFD)
    return nonLatin.matcher(normalized).replaceAll("")
}
