package com.github.senocak.ratehighway.domain

import com.github.senocak.ratehighway.util.RoleName
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface RoleRepository: JpaRepository<Role, UUID>, PagingAndSortingRepository<Role, UUID> {
    fun findByName(roleName: RoleName): Optional<Role>
}

@Repository
interface UserRepository: JpaRepository<User, UUID>, PagingAndSortingRepository<User, UUID>, JpaSpecificationExecutor<User> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}

@Repository
interface OAuthGoogleUserRepository: JpaRepository<OAuthGoogleUser, String>, JpaSpecificationExecutor<OAuthGoogleUser>

@Repository
interface OAuthGithubUserRepository: JpaRepository<OAuthGithubUser, String>, JpaSpecificationExecutor<OAuthGithubUser>

@Repository
interface OAuthLinkedinUserRepository: JpaRepository<OAuthLinkedinUser, String>, JpaSpecificationExecutor<OAuthLinkedinUser>

@Repository
interface OAuthFacebookUserRepository: JpaRepository<OAuthFacebookUser, String>, JpaSpecificationExecutor<OAuthFacebookUser>

@Repository
interface OAuthTwitterUserRepository: JpaRepository<OAuthTwitterUser, String>, JpaSpecificationExecutor<OAuthTwitterUser>

@Repository
interface OAuthSpotifyUserRepository: JpaRepository<OAuthSpotifyUser, String>, JpaSpecificationExecutor<OAuthSpotifyUser>

@Repository
interface OAuthTwitchUserRepository: JpaRepository<OAuthTwitchUser, String>, JpaSpecificationExecutor<OAuthTwitchUser>

@Repository
interface OAuthSlackUserRepository: JpaRepository<OAuthSlackUser, String>, JpaSpecificationExecutor<OAuthSlackUser>

@Repository
interface OAuthDropboxUserRepository: JpaRepository<OAuthDropboxUser, String>, JpaSpecificationExecutor<OAuthDropboxUser>

@Repository
interface OAuthInstagramUserRepository: JpaRepository<OAuthInstagramUser, String>, JpaSpecificationExecutor<OAuthInstagramUser>

@Repository
interface OAuthPaypalUserRepository: JpaRepository<OAuthPaypalUser, String>, JpaSpecificationExecutor<OAuthPaypalUser>

@Repository
interface OAuthDiscordUserRepository: JpaRepository<OAuthDiscordUser, String>, JpaSpecificationExecutor<OAuthDiscordUser>

@Repository
interface OAuthOktaUserRepository: JpaRepository<OAuthOktaUser, String>, JpaSpecificationExecutor<OAuthOktaUser>

@Repository
interface OAuthRedditUserRepository: JpaRepository<OAuthRedditUser, String>, JpaSpecificationExecutor<OAuthRedditUser>

@Repository
interface OAuthTiktokUserRepository: JpaRepository<OAuthTiktokUser, String>, JpaSpecificationExecutor<OAuthTiktokUser>
