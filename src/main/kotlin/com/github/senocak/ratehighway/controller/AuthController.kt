package com.github.senocak.ratehighway.controller

import com.github.senocak.ratehighway.domain.dto.ExceptionDto
import com.github.senocak.ratehighway.domain.dto.LoginRequest
import com.github.senocak.ratehighway.domain.dto.RegisterRequest
import com.github.senocak.ratehighway.domain.dto.SuccessResponse
import com.github.senocak.ratehighway.domain.dto.UserResponseDto
import com.github.senocak.ratehighway.domain.dto.UserResponseWrapperDto
import com.github.senocak.ratehighway.domain.Role
import com.github.senocak.ratehighway.domain.User
import com.github.senocak.ratehighway.exception.NotFoundException
import com.github.senocak.ratehighway.exception.ServerException
import com.github.senocak.ratehighway.service.MessageSourceService
import com.github.senocak.ratehighway.service.RoleService
import com.github.senocak.ratehighway.service.UserService
import com.github.senocak.ratehighway.util.OmaErrorMessageType
import com.github.senocak.ratehighway.util.RoleName
import com.github.senocak.ratehighway.util.logger
import com.github.senocak.ratehighway.util.toDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.http.MediaType

@RestController
@RequestMapping(BaseController.v1AuthUrl)
@Tag(name = "Authentication", description = "Authentication API")
class AuthController(
    private val userService: UserService,
    private val roleService: RoleService,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val messageSourceService: MessageSourceService,
): BaseController() {
    private val log: Logger by logger()

    @PostMapping("/login")
    @Operation(summary = "Login Endpoint", tags = ["Authentication"],
        responses = [
            ApiResponse(responseCode = "201", description = "successful operation",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = UserResponseWrapperDto::class))]),
            ApiResponse(responseCode = "400", description = "Bad credentials",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class))]),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class))])
        ]
    )
    @Throws(ServerException::class)
    fun login(
        @Parameter(description = "Request body to login", required = true) @Validated @RequestBody loginRequest: LoginRequest,
        resultOfValidation: BindingResult
    ): ResponseEntity<UserResponseWrapperDto> {
        validate(resultOfValidation = resultOfValidation)
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken(loginRequest.email, loginRequest.password))
        val user: User = userService.findByEmail(email = loginRequest.email!!)
        if (user.blockedAt != null) {
            log.error("Account:${user.email} is not blocked!")
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT, statusCode = HttpStatus.FORBIDDEN,
                variables = arrayOf(messageSourceService.get(code = "this_account_is_blocked", params =  arrayOf(user.blockedAt))))
        }
        val login: UserResponseDto = user.toDTO()
        return ResponseEntity
            .status(HttpStatus.OK)
            .headers(userIdHeader(userId = "${user.id}"))
            .body(userService.generateUserWrapperResponse(userResponseDto = login))
    }

    @PostMapping("/register")
    @Operation(summary = "Register Endpoint", tags = ["Authentication"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = SuccessResponse::class))]),
            ApiResponse(responseCode = "400", description = "Bad credentials",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class))]),
            ApiResponse(responseCode = "500", description = "internal server error occurred",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class))])
        ]
    )
    @Throws(ServerException::class)
    fun register(
        @Parameter(description = "Request body to register", required = true) @Validated @RequestBody signUpRequest: RegisterRequest,
        resultOfValidation: BindingResult
    ): ResponseEntity<SuccessResponse> {
        validate(resultOfValidation = resultOfValidation)
        if (userService.existsByEmail(email = signUpRequest.email!!)) {
            log.error("Email Address:${signUpRequest.email} is already taken!")
            throw ServerException(omaErrorMessageType = OmaErrorMessageType.JSON_SCHEMA_VALIDATOR, statusCode = HttpStatus.BAD_REQUEST,
                variables = arrayOf(messageSourceService.get(code = "email_in_use")))
        }
        val userRole: Role = roleService.findByName(roleName = RoleName.ROLE_USER)
            ?: throw NotFoundException(variables = arrayOf(messageSourceService.get(code = "role_not_found")))
                .also { log.error("User Role is not found") }
        val user = User(
            email = signUpRequest.email!!,
            password = passwordEncoder.encode(signUpRequest.password),
            roles = mutableListOf(userRole),
        )

        val result: User = userService.save(user = user)
        log.info("User created. User: $result")
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse(messageSourceService.get(code = "register_success")))
    }
}