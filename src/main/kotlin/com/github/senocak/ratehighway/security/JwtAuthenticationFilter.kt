package com.github.senocak.ratehighway.security

import com.github.senocak.ratehighway.exception.RestExceptionHandler
import com.github.senocak.ratehighway.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.ratehighway.domain.dto.ExceptionDto
import com.github.senocak.ratehighway.util.TOKEN_HEADER_NAME
import com.github.senocak.ratehighway.util.TOKEN_PREFIX
import com.github.senocak.ratehighway.util.logger
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * Filter class that aims to guarantee a single execution per request dispatch, on any servlet container.
 * @return -- an JwtAuthenticationFilter instance
 */
@Component
class JwtAuthenticationFilter(
    private val tokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val objectMapper: ObjectMapper,
    private val authenticationManager: AuthenticationManager,
    private val restExceptionHandler: RestExceptionHandler
): OncePerRequestFilter() {
    private val log: Logger by logger()

    /**
     * Guaranteed to be just invoked once per request within a single request thread.
     *
     * @param request -- Request information for HTTP servlets.
     * @param response -- It is where the servlet can write information about the data it will send back.
     * @param filterChain -- An object provided by the servlet container to the developer giving a view into the invocation chain of a filtered request for a resource.
     * @throws ServletException -- Throws ServletException
     * @throws IOException -- Throws IOException
     */
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        try {
            val bearerToken: String? = request.getHeader(TOKEN_HEADER_NAME)
            if (bearerToken != null && StringUtils.hasText(bearerToken) && bearerToken.startsWith(prefix = TOKEN_PREFIX)) {
                val jwt: String = bearerToken.substring(startIndex = 7)
                tokenProvider.validateToken(token = jwt)
                val userName: String = tokenProvider.getSubjectFromJWT(token = jwt)
                val userDetails: UserDetails = userService.loadUserByUsername(username = userName)
                val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.authorities)
                usernamePasswordAuthenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                authenticationManager.authenticate(usernamePasswordAuthenticationToken)
                log.trace("SecurityContext created")
            }
        } catch (ex: Exception) {
            val responseEntity: ResponseEntity<ExceptionDto> = restExceptionHandler.handleUnAuthorized(ex = RuntimeException(ex.message))
            //val responseEntity: ResponseEntity<ExceptionDto> = RestExceptionHandler().handleUnAuthorized(RuntimeException(ex.message))
            response.writer.write(objectMapper.writeValueAsString(responseEntity.body))
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json"
            log.error("Could not set user authentication in security context. Exception: ${ExceptionUtils.getStackTrace(ex)}")
            return
        }
        response.setHeader("Access-Control-Allow-Origin", "*")
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT")
        response.setHeader("Access-Control-Allow-Headers",
            "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With")
        response.setHeader("Access-Control-Expose-Headers",
            "Content-Type, Access-Control-Expose-Headers, Authorization, X-Requested-With")
        filterChain.doFilter(request, response)
        log.trace("Filtering accessed for remote address: ${request.remoteAddr}")
    }
}