package com.github.senocak.ratehighway.security

import com.github.senocak.ratehighway.util.logger
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component
import java.security.Key
import java.util.Date

@Component
class JwtTokenProvider(
    @Value("\${app.jwtSecret}") private val jwtSecret: String,
    @Value("\${app.jwtExpirationInMs}") private val jwtExpirationInMs: String,
) {
    private val log: Logger by logger()

    /**
     * Generates a JWT token for the input user with the specified roles.
     *
     * @param subject The username of the user to generate the token for.
     * @param roles The list of roles assigned to the user.
     * @return The generated JWT token.
     */
    fun generateJwtToken(subject: String, roles: List<String?>): String =
        generateToken(subject = subject, roles = roles, expirationInMs = jwtExpirationInMs.toLong())


    /**
     * Generates a token with the specified subject, roles, and expiration time.
     *
     * @param subject The subject of the token.
     * @param roles The list of roles assigned to the user.
     * @param expirationInMs The expiration time of the token in milliseconds.
     * @return The generated token.
     */
    private fun generateToken(subject: String, roles: List<String?>, expirationInMs: Long): String =
        HashMap<String, Any>()
            .also { it["roles"] = roles }
            .run {
                val now = Date()
                Jwts.builder()
                    .setClaims(this)
                    .setSubject(subject)
                    .setIssuedAt(now)
                    .setExpiration(Date(now.time + expirationInMs))
                    .signWith(signKey, SignatureAlgorithm.HS256)
                    .compact()
            }

    private val signKey: Key
        get() = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

    /**
     * Get the jws claims
     * @param token -- jwt token
     * @return -- expiration date
     */
    @Throws(
        ExpiredJwtException::class,
        UnsupportedJwtException::class,
        MalformedJwtException::class,
        io.jsonwebtoken.security.SignatureException::class,
        IllegalArgumentException::class
    )
    private fun getJwsClaims(token: String): Jws<Claims?> = Jwts.parserBuilder().setSigningKey(signKey).build().parseClaimsJws(token)

    /**
     * Parses the input JWT token and returns the username associated with the token.
     *
     * @param token The JWT token to parse.
     * @return The username associated with the input JWT token.
     */
    fun getSubjectFromJWT(token: String): String = getJwsClaims(token = token).body!!.subject

    /**
     * Validates the input token by verifying its signature and expiration time.
     *
     * @param token The token to validate.
     */
    fun validateToken(token: String) {
        try {
            getJwsClaims(token = token)
        } catch (ex: io.jsonwebtoken.security.SignatureException) {
            "Invalid JWT signature"
                .apply { log.error(this) }
                .run { throw AccessDeniedException(this) }
        } catch (ex: MalformedJwtException) {
            "Invalid JWT token"
                .apply { log.error(this) }
                .run { throw AccessDeniedException(this) }
        } catch (ex: ExpiredJwtException) {
            "Expired JWT token"
                .apply { log.error(this) }
                .run { throw AccessDeniedException(this) }
        } catch (ex: UnsupportedJwtException) {
            "Unsupported JWT token"
                .apply { log.error(this) }
                .run { throw AccessDeniedException(this) }
        } catch (ex: IllegalArgumentException) {
            "JWT claims string is empty."
                .apply { log.error(this) }
                .run { throw AccessDeniedException(this) }
        } catch (ex: AccessDeniedException) {
            ex.message
                .apply { log.error(this) }
                .run { throw AccessDeniedException(this) }
        } catch (ex: Exception) {
            "Undefined exception occurred: ${ex.message}"
                .apply { log.error(this) }
                .run { throw AccessDeniedException(this) }
        }
    }
}