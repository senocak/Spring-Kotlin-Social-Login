package com.github.senocak.ratehighway.security

import com.github.senocak.ratehighway.domain.Role
import com.github.senocak.ratehighway.domain.User
import com.github.senocak.ratehighway.service.UserService
import com.github.senocak.ratehighway.util.RoleName
import com.github.senocak.ratehighway.util.logger
import org.slf4j.Logger
import org.slf4j.MDC
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CustomAuthenticationManager(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder
): AuthenticationManager {
    private val log: Logger by logger()

    @Transactional
    override fun authenticate(authentication: Authentication): Authentication {
        val user: User = userService.findByEmail(email = authentication.name)
        if (authentication.credentials != null){
            val matches: Boolean = passwordEncoder.matches(authentication.credentials.toString(), user.password)
            if (!matches) {
                log.error("AuthenticationCredentialsNotFoundException occurred for $user")
                throw AuthenticationCredentialsNotFoundException("Username or password invalid")
            }
        }
        val authorities: MutableCollection<SimpleGrantedAuthority> = ArrayList()
        authorities.add(SimpleGrantedAuthority(RoleName.ROLE_USER.role))
        user.roles
            .filter{ r: Role -> r.name != RoleName.ROLE_USER }
            .forEach{ r: Role ->
                run {
                    authorities.add(SimpleGrantedAuthority(RoleName.fromString(r = r.name!!.role).role))
                }
            }
        val loadUserByUsername: org.springframework.security.core.userdetails.User = userService.loadUserByUsername(username = authentication.name)
        val auth: Authentication = UsernamePasswordAuthenticationToken(loadUserByUsername, user.password, authorities)
        SecurityContextHolder.getContext().authentication = auth
        MDC.put("userId", "${user.id}")
        log.info("Authentication is set to SecurityContext for $user")
        return auth
    }
}