package com.github.senocak.ratehighway.domain

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.senocak.ratehighway.util.RoleName
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.AttributeConverter
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Converter
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.Lob
import jakarta.persistence.ManyToMany
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Type
import java.io.Serializable
import java.util.Date
import java.util.UUID

@MappedSuperclass
open class BaseDomain(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    var id: UUID? = null,
    @Column var createdAt: Date = Date(),
    @Column var updatedAt: Date = Date()
): Serializable

@MappedSuperclass
open class OAuthBaseUser {
    @Id @Column var id: String? = null // Because server needs to be string
    @Column var email: String? = null
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        referencedColumnName = "id",
        nullable = true,
//        foreignKey = ForeignKey(name = "fk_google_user_id")
    )
    var user: User? = null
}

@Entity
@Table(name = "facebookUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthFacebookUser: OAuthBaseUser() {
    @Column var name: String? = null
    @Column var picture: String? = null
}

@Entity
@Table(name = "githubUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthGithubUser: OAuthBaseUser() {
    @Column var login: String? = null
    @Column var node_id: String? = null
    @Column var avatar_url: String? = null
    @Column var gravatar_id: String? = null
    @Column var url: String? = null
    @Column var html_url: String? = null
    @Column var followers_url: String? = null
    @Column var following_url: String? = null
    @Column var gists_url: String? = null
    @Column var starred_url: String? = null
    @Column var subscriptions_url: String? = null
    @Column var organizations_url: String? = null
    @Column var repos_url: String? = null
    @Column var events_url: String? = null
    @Column var received_events_url: String? = null
    @Column var type: String? = null
    @Column var site_admin: Boolean = false
    @Column var name: String? = null
    @Column var company: String? = null
    @Column var blog: String? = null
    @Column var location: String? = null
    @Column var hireable: Boolean = false
    @Column var bio: String? = null
    @Column var twitter_username: String? = null
    @Column var public_repos: Long? = null
    @Column var public_gists: Long? = null
    @Column var followers: Long? = null
    @Column var following: Long? = null
    @Column var private_gists: Long? = null
    @Column var total_private_repos: Long? = null
    @Column var owned_private_repos: Long? = null
    @Column var disk_usage: Long? = null
    @Column var collaborators: Long? = null
    @Column var two_factor_authentication: Boolean? = false
    //@Column var createdAt: Boolean? = false
    //@Column var updatedAt: Boolean? = false

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "name", column = Column(name = "plan_name")),
        AttributeOverride(name = "collaborators", column = Column(name = "plan_collaborators"))
    )
    var plan: GithubPlanResponse? = null
}

@Embeddable
class GithubPlanResponse {
    var name: String? = null
    var space: Long? = null
    var collaborators: Long? = null
    var private_repos: Long? = null
}

@Entity
@Table(name = "googleUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthGoogleUser: OAuthBaseUser() {
    @Column var verified_email: String? = null
    @Column var name: String? = null
    @Column var given_name: String? = null
    @Column var link: String? = null
    @Column var picture: String? = null
    @Column var locale: String? = null
    @Column var hd: String? = null
}

@Entity
@Table(name = "linkedinUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthLinkedinUser: OAuthBaseUser() {
    @Column var sub: String? = null
    @Column var email_verified: Boolean? = null
    @Column var given_name: String? = null
    @Column var picture: String? = null
    @Column var name: String? = null
    @Column var family_name: String? = null
    @Embedded var locale: LocaleResponse? = null
}

class LocaleResponse {
    @Schema(example = "TR", description = "Country of the locale", required = true, name = "country", type = "String")
    var country: String? = null

    @Schema(example = "tr", description = "Language of the locale", required = true, name = "language", type = "String")
    var language: String? = null
}

@Entity
@Table(name = "twitterUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthTwitterUser: OAuthBaseUser() {
    @Column var name: String? = null
    @Column var description: String? = null
    @Column var pinned_tweet_id: String? = null
    @Column var verified_type: String? = null
    @Column var created_at: String? = null
    @Column var profile_image_url: String? = null
    @Column var verified: Boolean? = null
    @Column var username: String? = null
    @Column var protected: Boolean? = null
    @Column var most_recent_tweet_id: String? = null
    @Column var receives_your_dm: Boolean? = null
    @Embedded var public_metrics: PublicMetricsResponse? = null

    override fun toString(): String =
        "OAuthTwitterUser(name=$name, description=$description, pinned_tweet_id=$pinned_tweet_id, verified_type=$verified_type, created_at=$created_at, profile_image_url=$profile_image_url, verified=$verified, username=$username, protected=$protected, most_recent_tweet_id=$most_recent_tweet_id, receives_your_dm=$receives_your_dm, public_metrics=$public_metrics)"
}

class PublicMetricsResponse {
    @Schema(example = "1", description = "Followers Count", required = true, name = "followers_count", type = "String")
    var followers_count: Int? = null

    @Schema(example = "1", description = "Following Count", required = true, name = "following_count", type = "String")
    var following_count: Int? = null

    @Schema(example = "1", description = "Tweet Count", required = true, name = "tweet_count", type = "String")
    var tweet_count: Int? = null

    @Schema(example = "1", description = "Listed Count", required = true, name = "listed_count", type = "String")
    var listed_count: Int? = null

    @Schema(example = "1", description = "Like Count", required = true, name = "like_count", type = "String")
    var like_count: Int? = null

    @Schema(example = "1", description = "Media Count", required = true, name = "media_count", type = "String")
    var media_count: Int? = null
}

@Entity
@Table(name = "spotifyUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthSpotifyUser: OAuthBaseUser() {
    @Column var country: String? = null
    @Column var display_name: String? = null
    @Embedded var explicit_content: ExplicitContent? = null
    @Embedded var external_urls: ExternalUrl? = null
    @Embedded var followers: Follower? = null
    @Column var href: String? = null
    @Column
    @Convert(converter = ImageListConverter::class)
    var images: List<Image>? = null
    @Column var type: String? = null
    @Column var product: String? = null
    @Column var uri: String? = null
}
class ExplicitContent {
    var filter_enabled: Boolean? = null
    var filter_locked: Boolean? = null
}
class ExternalUrl {
    var spotify: String? = null
}
class Follower {
    @JsonProperty("href")
    var follower_href: String? = null
    var total: String? = null
}
class Image {
    var height: String? = null
    var url: String? = null
    var width: String? = null
}
@Converter
class ImageListConverter : AttributeConverter<List<Image>, String> {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<Image>?): String? =
        attribute?.let { objectMapper.writeValueAsString(it) }

    override fun convertToEntityAttribute(dbData: String): List<Image>? =
        objectMapper.readValue(dbData, object : TypeReference<List<Image>?>() {})
}

@Entity
@Table(name = "twitchUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthTwitchUser: OAuthBaseUser() {
    @Column var login: String? = null
    @Column var display_name: String? = null
    @Column var type: String? = null
    @Column var broadcaster_type: String? = null
    @Column var description: String? = null
    @Column var profile_image_url: String? = null
    @Column var offline_image_url: String? = null
    @Column var view_count: Int? = null
    @Column var created_at: String? = null
}

@Entity
@Table(name = "slackUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthSlackUser: OAuthBaseUser() {
    @Column var sub: String? = null
    @Column var email_verified: Boolean? = null
    @Column var date_email_verified: Int? = null
    @Column var name: String? = null
    @Column var picture: String? = null
    @Column var given_name: String? = null
    @Column var family_name: String? = null
    @Column var locale: String? = null
    @JsonProperty("https://slack.com/team_name") @Column var slackTeamName: String? = null
    @JsonProperty("https://slack.com/team_domain") @Column var slackTeamDomain: String? = null
}

@Entity
@Table(name = "dropboxUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthDropboxUser: OAuthBaseUser() {
    @Column var account_id: String? = null
    @Embedded var name: DropboxName? = null
    @Column var profile_photo_url: String? = null
    @Column var email_verified: Boolean? = null
    @Column var disabled: Boolean? = null
    @Column var country: String? = null
    @Column var locale: String? = null
    @Column var referral_link: String? = null
    @Column var is_paired: Boolean? = null
    @Embedded var account_type: DropboxAccountType? = null
    @Embedded var root_info: DropboxRootInfo? = null
}
class DropboxName {
    var given_name: String? = null
    var surname: String? = null
    var familiar_name: String? = null
    var display_name: String? = null
    var abbreviated_name: String? = null

    override fun toString(): String =
        "DropboxName(given_name=$given_name, surname=$surname, familiar_name=$familiar_name, display_name=$display_name," +
                "abbreviated_name=$abbreviated_name)"
}
class DropboxAccountType {
    @JsonProperty(".tag") var accountTypeTag: String? = null
}
class DropboxRootInfo {
    @JsonProperty(".tag") var rootInfoTag: String? = null
    var root_namespace_id: String? = null
    var home_namespace_id: String? = null
}

@Entity
@Table(name = "instagramUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthInstagramUser: OAuthBaseUser() {
    @Column @JsonProperty("user_id") var instagram_user_id: String? = null
    @Column var username: String? = null
    @Column var name: String? = null
    @Column var account_type: String? = null
    @Column @Lob var profile_picture_url: String? = null
    @Column var followers_count: Int? = null
    @Column var follows_count: Int? = null
    @Column var media_count: Int? = null
}

@Entity
@Table(name = "paypalUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthPaypalUser: OAuthBaseUser() {
    @Column @JsonProperty("user_id") var paypal_user_id: String? = null
    @Column var sub: String? = null
    @Column var name: String? = null
    @Column var given_name: String? = null
    @Column var middle_name: String? = null
    @Column var family_name: String? = null
    @Column var picture: String? = null
    @Column var gender: String? = null
    @Column var birthdate: String? = null
    @Column var zoneinfo: String? = null
    @Column var locale: String? = null
    @Column var phone_number: String? = null
    @Column var verified: String? = null
    @Column var email_verified: String? = null
    @Embedded var address: PayPalAddress? = null
    @Column var account_type: String? = null
    @Column var age_range: String? = null
}
class PayPalAddress {
    val postal_code: String? = null
    val country: String? = null
    val state: String? = null
    val street1: String? = null
    val city: String? = null
}

@Entity
@Table(name = "discordUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthDiscordUser: OAuthBaseUser() {
    @Column var username: String? = null
    @Column var avatar: String? = null
    @Column var discriminator: String? = null
    @Column var public_flags: Int? = null
    @Column var flags: Int? = null
    @Column var banner: String? = null
    @Column var accent_color: Int? = null
    @Column var global_name: String? = null
    @Column var avatar_decoration_data: String? = null
    @Column var banner_color: String? = null
    @Column var clan: String? = null
    @Column var primary_guild: String? = null
    @Column var mfa_enabled: Boolean? = null
    @Column var locale: String? = null
    @Column var premium_type: Int? = null
    @Column var verified: Boolean? = null
}

@Entity
@Table(name = "oktaUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthOktaUser: OAuthBaseUser() {
    @Column var status: String? = null
    @Column var firstName: String? = null
    @Column var lastName: String? = null
}

@Entity
@Table(name = "redditUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthRedditUser: OAuthBaseUser() {
    @Column var name: String? = null
    @Column var verified: Boolean? = null
}

@Entity
@Table(name = "tiktokUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthTiktokUser: OAuthBaseUser() {
    @Column @Lob var avatar_large_url: String? = null
    @Column @Lob var avatar_url_100: String? = null
    @Column var follower_count: Int? = null
    @Column var video_count: Int? = null
    @Column @Lob var avatar_url: String? = null
    @Column var following_count: Int? = null
    @Column var is_verified: Boolean? = null
    @Column var open_id: String? = null
    @Column var profile_deep_link: String? = null
    @Column var display_name: String? = null
    @Column var union_id: String? = null
    @Column var username: String? = null
    @Column var bio_description: String? = null
    @Column var likes_count: Int? = null
}

@Entity
@Table(name = "boxUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthBoxUser: OAuthBaseUser() {
    @Column var type: String? = null
    @Column var name: String? = null
    @Column var login: String? = null
    @Column var created_at: String? = null
    @Column var modified_at: String? = null
    @Column var language: String? = null
    @Column var timezone: String? = null
    @Column var space_amount: Long? = null
    @Column var space_used: Long? = null
    @Column var max_upload_size: Long? = null
    @Column var status: String? = null
    @Column var job_title: String? = null
    @Column var phone: String? = null
    @Column var address: String? = null
    @Column var avatar_url: String? = null
    @Column var notification_email: String? = null
}

@Entity
@Table(name = "vimeoUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthVimeoUser: OAuthBaseUser() {
    @Column var name: String? = null
    @Column var link: String? = null
    @Column var location: String? = null
    @Column var gender: String? = null
    @Column var bio: String? = null
    @Column var short_bio: String? = null
    @Column var created_time: String? = null
    @Column var resource_key: String? = null
    @Column var account: String? = null
    @Column @Embedded var pictures: OAuthVimeoUserPictures? = null
}
class OAuthVimeoUserPictures{
    var uri: String? = null
    var active: String? = null
    var type: String? = null
    var base_link: String? = null
    @JsonProperty("resource_key") var pictures_resource_key: String? = null
    var default_picture: String? = null

    @Convert(converter = OAuthVimeoUserPicturesConverter::class)
    @Lob
    var sizes: List<OAuthVimeoUserPicture>? = null
}
@Embeddable
class OAuthVimeoUserPicture{
    var width: String? = null
    var height: String? = null
    var link: String? = null
}
@Converter
class OAuthVimeoUserPicturesConverter : AttributeConverter<List<OAuthVimeoUserPicture>, String> {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<OAuthVimeoUserPicture>?): String? =
        attribute?.let { objectMapper.writeValueAsString(it) }

    override fun convertToEntityAttribute(dbData: String): List<OAuthVimeoUserPicture>? =
        objectMapper.readValue(dbData, object : TypeReference<List<OAuthVimeoUserPicture>?>() {})
}

@Entity
@Table(name = "gitlabUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthGitlabUser: OAuthBaseUser() {
    @Column var username: String? = null
    @Column var name: String? = null
    @Column var state: String? = null
    @Column var locked: Boolean? = null
    @Column var avatar_url: String? = null
    @Column var web_url: String? = null
    @Column var created_at: String? = null
    @Column var bio: String? = null
    @Column var location: String? = null
    @Column var public_email: String? = null
    @Column var skype: String? = null
    @Column var linkedin: String? = null
    @Column var twitter: String? = null
    @Column var discord: String? = null
    @Column var website_url: String? = null
    @Column var organization: String? = null
    @Column var job_title: String? = null
    @Column var pronouns: String? = null
    @Column var bot: Boolean? = null
    @Column var work_information: String? = null
    @Column var local_time: String? = null
    @Column var last_sign_in_at: String? = null
    @Column var confirmed_at: String? = null
    @Column var last_activity_on: String? = null
    @Column var theme_id: Int? = null
    @Column var color_scheme_id: Int? = null
    @Column var projects_limit: Int? = null
    @Column var current_sign_in_at: String? = null
    @Column var can_create_group: Boolean? = null
    @Column var can_create_project: Boolean? = null
    @Column var two_factor_enabled: Boolean? = null
    @Column var external: Boolean? = null
    @Column var private_profile: Boolean? = null
    @Column var commit_email: String? = null
}

@Entity
@Table(name = "asanaUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthAsanaUser: OAuthBaseUser() {
    @Column var gid: String? = null
    @Column var name: String? = null
    @Column @Embedded var photo: OAuthAsanaUserPhotos? = null
    @Column var resource_type: String? = null
    @Column @Convert(converter = OAuthAsanaUserWorkspacesConverter::class) var workspaces: List<OAuthAsanaUserWorkspaces>? = null
}
class OAuthAsanaUserPhotos {
    var image_21x21: String? = null
    var image_27x27: String? = null
    var image_36x36: String? = null
    var image_60x60: String? = null
    var image_128x128: String? = null
}
class OAuthAsanaUserWorkspaces {
    var gid: String? = null
    @JsonProperty("name") var workspace_name: String? = null
    var resource_type: String? = null
}
@Converter
class OAuthAsanaUserWorkspacesConverter : AttributeConverter<List<OAuthAsanaUserWorkspaces>, String> {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<OAuthAsanaUserWorkspaces>?): String? =
        attribute?.let { objectMapper.writeValueAsString(it) }

    override fun convertToEntityAttribute(dbData: String): List<OAuthAsanaUserWorkspaces>? =
        objectMapper.readValue(dbData, object : TypeReference<List<OAuthAsanaUserWorkspaces>?>() {})
}
@Entity
@Table(name = "foursquareUsers", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
class OAuthFoursquareUser: OAuthBaseUser() {
    @Column var firstName: String? = null
    @Column var lastName: String? = null
    @Column var privateProfile: Boolean? = null
    @Column var gender: String? = null
    @Column var countryCode: String? = null
    @Column var relationship: String? = null
    @Column var canonicalUrl: String? = null
    @Column @Embedded var photo: OAuthFoursquareUserPhoto? = null
    @Column var birthday: Int? = null
    @Column var homeCity: String? = null
    @Column var bio: String? = null
    @Column @Embedded var contact: OAuthFoursquareUserContact? = null
    @Column var type: String? = null
    @Column var createdAt: Int? = null
}
class OAuthFoursquareUserPhoto {
    var prefix: String? = null
    var suffix: String? = null
    var default: String? = null
}
class OAuthFoursquareUserContact {
    var verifiedPhone: String? = null
    @JsonProperty("email") var contact_email: String? = null
    var verifiedEmail: String? = null
}

@Entity
@Table(name = "roles")
class Role(@Column @Enumerated(EnumType.STRING) var name: RoleName? = null): BaseDomain()

@Entity
@Table(name = "users", uniqueConstraints = [
    UniqueConstraint(columnNames = ["username"]),
    UniqueConstraint(columnNames = ["email"])
])
class User(
    @Column var email: String? = null,
    @Column var password: String? = null,
    @Column var blockedAt: Date? = null,
    @JoinTable(name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    @ManyToMany(fetch = FetchType.LAZY)
    var roles: MutableList<Role> = ArrayList(),

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthGoogleUser: OAuthGoogleUser? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthGithubUser: OAuthGithubUser? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthLinkedinUser: OAuthLinkedinUser? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthFacebookUser: OAuthFacebookUser? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthTwitterUser: OAuthTwitterUser? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthSpotifyUser: OAuthSpotifyUser? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthTwitchUser: OAuthTwitchUser? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthSlackUser: OAuthSlackUser? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthDropboxUser: OAuthDropboxUser? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthInstagramUser: OAuthInstagramUser? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthPaypalUser: OAuthPaypalUser? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthDiscordUser: OAuthDiscordUser? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthOktaUser: OAuthOktaUser? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthRedditUser: OAuthRedditUser? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthTiktokUser: OAuthTiktokUser? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthBoxUser: OAuthBoxUser? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthVimeoUser: OAuthVimeoUser? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthGitlabUser: OAuthGitlabUser? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthAsanaUser: OAuthAsanaUser? = null,
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthFoursquareUser: OAuthFoursquareUser? = null,
): BaseDomain()
