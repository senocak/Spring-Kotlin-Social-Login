package com.github.senocak.ratehighway.util

import ch.qos.logback.classic.Level
import com.github.senocak.ratehighway.KotlinApplication
import com.github.senocak.ratehighway.domain.OAuthDiscordUser
import com.github.senocak.ratehighway.domain.OAuthDropboxUser
import com.github.senocak.ratehighway.domain.dto.RoleResponse
import com.github.senocak.ratehighway.domain.dto.UserResponseDto
import com.github.senocak.ratehighway.domain.OAuthFacebookUser
import com.github.senocak.ratehighway.domain.OAuthGithubUser
import com.github.senocak.ratehighway.domain.OAuthGoogleUser
import com.github.senocak.ratehighway.domain.OAuthInstagramUser
import com.github.senocak.ratehighway.domain.OAuthLinkedinUser
import com.github.senocak.ratehighway.domain.OAuthOktaUser
import com.github.senocak.ratehighway.domain.OAuthPaypalUser
import com.github.senocak.ratehighway.domain.OAuthRedditUser
import com.github.senocak.ratehighway.domain.OAuthSlackUser
import com.github.senocak.ratehighway.domain.OAuthSpotifyUser
import com.github.senocak.ratehighway.domain.OAuthTiktokUser
import com.github.senocak.ratehighway.domain.OAuthTwitchUser
import com.github.senocak.ratehighway.domain.OAuthTwitterUser
import com.github.senocak.ratehighway.domain.Role
import com.github.senocak.ratehighway.domain.User
import com.github.senocak.ratehighway.domain.dto.OAuthDiscordUserResponse
import com.github.senocak.ratehighway.domain.dto.OAuthDropboxUserResponse
import com.github.senocak.ratehighway.domain.dto.OAuthFacebookUserResponse
import com.github.senocak.ratehighway.domain.dto.OAuthGithubUserResponse
import com.github.senocak.ratehighway.domain.dto.OAuthGoogleUserResponse
import com.github.senocak.ratehighway.domain.dto.OAuthInstagramUserResponse
import com.github.senocak.ratehighway.domain.dto.OAuthLinkedinUserResponse
import com.github.senocak.ratehighway.domain.dto.OAuthOktaUserResponse
import com.github.senocak.ratehighway.domain.dto.OAuthPaypalUserResponse
import com.github.senocak.ratehighway.domain.dto.OAuthRedditUserResponse
import com.github.senocak.ratehighway.domain.dto.OAuthSlackUserResponse
import com.github.senocak.ratehighway.domain.dto.OAuthSpotifyUserResponse
import com.github.senocak.ratehighway.domain.dto.OAuthTiktokUserResponse
import com.github.senocak.ratehighway.domain.dto.OAuthTwitchUserResponse
import com.github.senocak.ratehighway.domain.dto.OAuthTwitterUserResponse
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
    if (this.oAuthTwitterUser != null) dto.twitter = this.oAuthTwitterUser!!.toDTO()
    if (this.oAuthSpotifyUser != null) dto.spotify = this.oAuthSpotifyUser!!.toDTO()
    if (this.oAuthTwitchUser != null) dto.twitch = this.oAuthTwitchUser!!.toDTO()
    if (this.oAuthSlackUser != null) dto.slack = this.oAuthSlackUser!!.toDTO()
    if (this.oAuthDropboxUser != null) dto.dropbox = this.oAuthDropboxUser!!.toDTO()
    if (this.oAuthInstagramUser != null) dto.instagram = this.oAuthInstagramUser!!.toDTO()
    if (this.oAuthPaypalUser != null) dto.paypal = this.oAuthPaypalUser!!.toDTO()
    if (this.oAuthDiscordUser != null) dto.discord = this.oAuthDiscordUser!!.toDTO()
    if (this.oAuthOktaUser != null) dto.okta = this.oAuthOktaUser!!.toDTO()
    if (this.oAuthRedditUser != null) dto.reddit = this.oAuthRedditUser!!.toDTO()
    if (this.oAuthTiktokUser != null) dto.tiktok = this.oAuthTiktokUser!!.toDTO()
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
fun OAuthGithubUser.toDTO(): OAuthGithubUserResponse =
    OAuthGithubUserResponse()
        .also {
            it.name = this.name
            it.email = this.email
            it.username = this.login
            it.url = this.html_url
        }

/**
 * Converts an OAuthLinkedinUser object to an OAuthLinkedinUserResponse object.
 * @return An OAuthLinkedinUserResponse object that represents the input OAuthLinkedinUser object.
 */
fun OAuthLinkedinUser.toDTO(): OAuthLinkedinUserResponse =
    OAuthLinkedinUserResponse()
        .also {
            it.sub = this.sub
            it.name = this.name
            it.givenName = this.given_name
            it.familyName = this.family_name
            it.email = this.email
            it.emailVerified = this.email_verified
            it.picture = this.picture
            it.localeData = this.locale
        }

/**
 * Converts an OAuthFacebookUser object to an OAuthFacebookUserResponse object.
 * @return An OAuthFacebookUserResponse object that represents the input OAuthFacebookUser object.
 */
fun OAuthFacebookUser.toDTO(): OAuthFacebookUserResponse =
    OAuthFacebookUserResponse()
        .also {
            it.email = this.email
            it.name = this.name
            it.picture = this.picture
        }

/**
 * Converts an OAuthGoogleUser object to an OAuthGoogleUserResponse object.
 * @return An OAuthGoogleUserResponse object that represents the input OAuthGoogleUser object.
 */
fun OAuthGoogleUser.toDTO(): OAuthGoogleUserResponse =
    OAuthGoogleUserResponse()
        .also {
            it.email = this.email
            it.verified_email = this.verified_email
            it.name = this.name
            it.given_name = this.given_name
            it.link = this.link
            it.picture = this.picture
            it.locale_user = this.locale
            it.hd = this.hd
        }

fun OAuthTwitterUser.toDTO(): OAuthTwitterUserResponse =
    OAuthTwitterUserResponse()
        .also {
            it.email = this.email
            it.name = this.name
            it.description = this.description
            it.username = this.username
            it.verified_type = this.verified_type
            it.profile_image_url = this.profile_image_url
            it.protected = this.protected
        }

fun OAuthSpotifyUser.toDTO(): OAuthSpotifyUserResponse =
    OAuthSpotifyUserResponse()
        .also {
            it.email = this.email
            it.display_name = this.display_name
            it.country = this.country
        }

fun OAuthTwitchUser.toDTO(): OAuthTwitchUserResponse =
    OAuthTwitchUserResponse()
        .also {
            it.email = this.email
            it.display_name = this.display_name
            it.profile_image_url = this.profile_image_url
        }

fun OAuthSlackUser.toDTO(): OAuthSlackUserResponse =
    OAuthSlackUserResponse()
        .also {
            it.email = this.email
            it.name = "${this.given_name} ${this.family_name}"
            it.picture = this.picture
            it.localeSlack = this.locale
        }

fun OAuthDropboxUser.toDTO(): OAuthDropboxUserResponse =
    OAuthDropboxUserResponse()
        .also {
            it.email = this.email
            it.name = this.name
            it.country = this.country
            it.localeSlack = this.locale
            it.profile_photo_url = this.profile_photo_url
        }

fun OAuthInstagramUser.toDTO(): OAuthInstagramUserResponse =
    OAuthInstagramUserResponse()
        .also {
            it.email = this.email
            it.name = this.name
            it.profile_picture_url = this.profile_picture_url
            it.username = this.username
        }

fun OAuthPaypalUser.toDTO(): OAuthPaypalUserResponse =
    OAuthPaypalUserResponse()
        .also {
            it.sub = this.sub
            it.email = this.email
            it.name = this.name
            it.verified = this.verified
            it.email_verified = this.email_verified
            it.address = this.address
        }

fun OAuthDiscordUser.toDTO(): OAuthDiscordUserResponse =
    OAuthDiscordUserResponse()
        .also {
            it.username = this.username
            it.email = this.email
            it.global_name = this.global_name
            it.verified = this.verified
            it.avatar = this.avatar
            it.mfa_enabled = this.mfa_enabled
        }

fun OAuthOktaUser.toDTO(): OAuthOktaUserResponse =
    OAuthOktaUserResponse()
        .also {
            it.email = this.email
            it.name = "${this.firstName} ${this.lastName}"
            it.status = this.status
        }

fun OAuthRedditUser.toDTO(): OAuthRedditUserResponse =
    OAuthRedditUserResponse()
        .also {
            it.email = this.id
            it.name = this.name
            it.verified = this.verified
        }

fun OAuthTiktokUser.toDTO(): OAuthTiktokUserResponse =
    OAuthTiktokUserResponse()
        .also {
            it.email = this.id
            it.avatar_large_url = this.avatar_large_url
            it.avatar_url_100 = this.avatar_url_100
            it.follower_count = this.follower_count
            it.video_count = this.video_count
            it.avatar_url = this.avatar_url
            it.following_count = this.following_count
            it.is_verified = this.is_verified
            it.open_id = this.open_id
            it.profile_deep_link = this.profile_deep_link
            it.display_name = this.display_name
            it.union_id = this.union_id
            it.username = this.username
            it.bio_description = this.bio_description
            it.likes_count = this.likes_count
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
