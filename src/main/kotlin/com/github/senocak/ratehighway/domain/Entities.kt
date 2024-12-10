package com.github.senocak.ratehighway.domain

import com.github.senocak.ratehighway.util.RoleName
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.Column
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
import jakarta.persistence.ManyToMany
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
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
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY) var oAuthTwitterUser: OAuthTwitterUser? = null
): BaseDomain()
