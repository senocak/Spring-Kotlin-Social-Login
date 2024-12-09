package com.github.senocak.ratehighway.service

import com.github.senocak.ratehighway.domain.dto.RoleResponse
import com.github.senocak.ratehighway.domain.dto.UserResponseDto
import com.github.senocak.ratehighway.domain.dto.UserResponseWrapperDto
import com.github.senocak.ratehighway.domain.Role
import com.github.senocak.ratehighway.domain.User
import com.github.senocak.ratehighway.exception.NotFoundException
import com.github.senocak.ratehighway.exception.ServerException
import com.github.senocak.ratehighway.domain.UserRepository
import com.github.senocak.ratehighway.security.JwtTokenProvider
import com.github.senocak.ratehighway.util.RoleName
import com.github.senocak.ratehighway.util.logger
import org.slf4j.Logger
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val messageSourceService: MessageSourceService,
    private val tokenProvider: JwtTokenProvider
): UserDetailsService {
    private val log: Logger by logger()

    /**
     * Retrieves all User entities from the database.
     *
     * @return a mutable iterable containing all User entities in the database.
     *
     * The function retrieves all User entities from the database using the userRepository's findAll() method and returns
     * a mutable iterable containing the entities.
     */
    fun findAll(): MutableIterable<User> = userRepository.findAll()

    /**
     * Retrieves a User entity from the database by its ID.
     *
     * @param id: the ID of the User entity to retrieve.
     * @return the retrieved User entity.
     * @throws NotFoundException if no User entity is found with the provided ID.
     *
     * The function retrieves a User entity from the database using the userRepository's findById() method and the provided ID.
     * If no User entity is found, the function throws a NotFoundException with a descriptive error message obtained from
     * the messageSourceService. Otherwise, the function returns the retrieved User entity.
     */
    @Throws(NotFoundException::class)
    fun findById(id: UUID): User =
        userRepository.findById(id)
            .orElseThrow { NotFoundException(variables = arrayOf(messageSourceService.get(code = "user_not_found"))) }

    /**
     * Checks if a User entity with the provided email address exists in the database.
     *
     * @param email: the email address to check.
     * @return a boolean indicating whether a User entity with the provided email address exists in the database.
     *
     * The function checks if a User entity with the provided email address exists in the database using the userRepository's
     * existsByEmail() method and returns a boolean value indicating the result.
     */
    fun existsByEmail(email: String): Boolean = userRepository.existsByEmail(email = email)

    /**
     * Retrieves a User entity from the database by its email address.
     *
     * @param email: the email address of the User entity to retrieve.
     * @return the retrieved User entity, or null if no User entity is found.
     * @throws NotFoundException if no User entity is found with the provided email address.
     *
     * The function retrieves a User entity from the database using the userRepository's findByEmail() method and the provided email address.
     * If no User entity is found, the function throws a NotFoundException with a descriptive error message obtained from
     * the messageSourceService. Otherwise, the function returns the retrieved User entity.
     */
    @Throws(NotFoundException::class)
    fun findByEmail(email: String): User =
        userRepository.findByEmail(email = email)
            ?: throw NotFoundException(variables = arrayOf(messageSourceService.get(code = "user_not_found")))

    /**
     * Deletes a User entity from the database.
     *
     * @param user: the User entity to delete.
     * @return None.
     *
     * The function deletes the provided User entity from the database using the userRepository's delete() method.
     * It does not return any value upon successful deletion.
     */
    fun delete(user: User): Unit = userRepository.delete(user)

    /**
     * Saves a User entity to the database.
     *
     * @param user: the User entity to persist.
     * @return the persisted User entity.
     *
     * The function saves the provided User entity to the database using the userRepository's save() method.
     * It returns the saved User entity upon successful save operation.
     */
    fun save(user: User): User = userRepository.save(user)

    /**
     * Loads a user by their username and returns a Spring Security User object.
     *
     * @param username: the username of the user to load.
     * @return a Spring Security User object representing the loaded user.
     * @throws UsernameNotFoundException if the user is not found in the database.
     *
     * The function first retrieves a User entity from the database using the findByUsername() method of the userRepository.
     * If no user is found with the provided username, the function throws a UsernameNotFoundException with a descriptive error message.
     * If the user is found, a Spring Security User object is created using the retrieved User entity's properties and returned.
     */
    @Transactional
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): org.springframework.security.core.userdetails.User {
        val user: User = findByEmail(email = username)
        val authorities: List<GrantedAuthority> = user.roles
            .map { r: Role -> SimpleGrantedAuthority(RoleName.fromString(r.name.toString()).name) }
            .toList()
        return org.springframework.security.core.userdetails.User(user.email, user.password, authorities)
    }

    /**
     * Retrieves the currently logged-in user.
     *
     * @return the currently logged-in User entity.
     * @throws ServerException if the user is not authenticated.
     *
     * The function retrieves the currently logged-in user from the SecurityContextHolder. If no user is found in the context,
     * the function throws an AuthenticationCredentialsNotFoundException. If the user is found, it is cast to a User entity and returned.
     */
    @Throws(ServerException::class)
    fun loggedInUser(): User {
        val username: String = (SecurityContextHolder.getContext().authentication.principal as org.springframework.security.core.userdetails.User).username
        return findByEmail(email = username)
    }

    /**
     * Generates a wrapper response object for the specified [userResponseDto].
     *
     * @param userResponseDto the user response DTO to wrap
     *
     * @return a new user response wrapper DTO containing the specified user response DTO
     */
    fun generateUserWrapperResponse(userResponseDto: UserResponseDto): UserResponseWrapperDto {
        val roles: List<String> = userResponseDto.roles!!
            .map { r: RoleResponse -> RoleName.fromString(r = r.name.name).name }
            .toList()
        val jwtToken: String = tokenProvider.generateJwtToken(subject = userResponseDto.email!!, roles = roles)
        val userResponseWrapperDto = UserResponseWrapperDto(userResponseDto = userResponseDto, token = jwtToken)
        log.info("UserWrapperResponse is generated. UserWrapperResponse: $userResponseWrapperDto")
        return userResponseWrapperDto
    }
}