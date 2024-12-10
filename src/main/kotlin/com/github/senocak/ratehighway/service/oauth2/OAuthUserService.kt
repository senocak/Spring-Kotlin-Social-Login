package com.github.senocak.ratehighway.service.oauth2

import com.github.senocak.ratehighway.domain.dto.UserResponseWrapperDto
import com.github.senocak.ratehighway.domain.OAuthBaseUser
import com.github.senocak.ratehighway.domain.Role
import com.github.senocak.ratehighway.domain.User
import com.github.senocak.ratehighway.domain.dto.OAuthTokenResponse
import com.github.senocak.ratehighway.exception.NotFoundException
import com.github.senocak.ratehighway.exception.ServerException
import com.github.senocak.ratehighway.security.JwtTokenProvider
import com.github.senocak.ratehighway.service.MessageSourceService
import com.github.senocak.ratehighway.service.RoleService
import com.github.senocak.ratehighway.service.UserService
import com.github.senocak.ratehighway.util.OmaErrorMessageType
import com.github.senocak.ratehighway.util.RandomStringGenerator
import com.github.senocak.ratehighway.util.RoleName
import com.github.senocak.ratehighway.util.logger
import com.github.senocak.ratehighway.util.toDTO
import org.slf4j.Logger
import org.slf4j.MDC
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import java.util.Date
import java.util.Optional
import java.util.UUID
import kotlin.collections.ArrayList

interface OAuthUserService<E, R> {
    // CrudRepository
    fun save(entity: E): E
    fun saveAll(entities: Iterable<E>): Iterable<E>
    fun findById(id: String): E?
    fun existsById(id: String): Boolean
    fun findAll(): Iterable<E>
    fun findAllById(ids: Iterable<String>): Iterable<E>
    fun count(): Long
    fun deleteById(id: String)
    fun delete(entity: E)
    fun deleteAllById(ids: Iterable<String>): Unit
    fun deleteAll(entities: Iterable<E>): Unit
    fun deleteAll(): Unit
    // JpaSpecificationExecutor
    fun findOne(spec: Specification<E>): Optional<E>
    fun findAll(spec: Specification<E>): List<E>
    fun findAll(spec: Specification<E>, pageable: Pageable): Page<E>
    fun findAll(spec: Specification<E>, sort: Sort): List<E>
    fun count(spec: Specification<E>): Long
    fun exists(spec: Specification<E>): Boolean
    // Custom
    fun getByIdOrThrowException(id: String): E
    fun getByIdOrEmailOrThrowException(id: String, email: String): E
}

abstract class OAuthUserServiceImpl<E, R>(
    private val repository: R,
    private val messageSourceService: MessageSourceService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val roleService: RoleService,
    private val passwordEncoder: PasswordEncoder,
): OAuthUserService<E, R> where E: OAuthBaseUser, R: JpaRepository<E, String>, R: JpaSpecificationExecutor<E> {
    protected val log: Logger by logger()

    /**
     * This function overrides the save method in a superclass or interface, and saves an entity of type E into a repository.
     * @param entity the entity to be saved into the repository
     * @return the entity that was saved into the repository
     */
    override fun save(entity: E): E = repository.save(entity)

    /**
     * This function overrides the saveAll method and saves all the provided entities
     * to the repository using the saveAll method of the repository.
     * @param entities an iterable of entities to be saved to the repository.
     * @return an iterable of saved entities.
     */
    override fun saveAll(entities: Iterable<E>): Iterable<E> = repository.saveAll(entities)

    /**
     * Retrieves an object from the data source by its unique ID.
     * @param id the string ID of the object to retrieve
     * @return the object retrieved from the data source, or null if no matching object is found
     * @throws Exception if an error occurs while retrieving the object from the data source
     */
    override fun findById(id: String): E? = repository.findById(id).orElseGet { null }

    /**
     * This function overrides the existsById method and checks whether an entity with
     * the specified id exists in the repository using the existsById method of the repository.
     * @param id the id of the entity to be checked for existence in the repository.
     * @return a boolean value indicating whether the entity with the specified id exists in the repository.
     */
    override fun existsById(id: String): Boolean = repository.existsById(id)

    /**
     * Returns a list of all objects of type E from the data source.
     * @return a list of all objects of type E retrieved from the data source
     * @throws Exception if an error occurs while retrieving the objects from the data source
     */
    override fun findAll(): List<E> = repository.findAll()

    /**
     * This function overrides the findAllById method and retrieves all entities with the specified
     * ids from the repository using the findAllById method of the repository.
     * @param ids an iterable of ids for the entities to be retrieved from the repository.
     * @return an iterable of retrieved entities.
     */
    override fun findAllById(ids: Iterable<String>): Iterable<E> = repository.findAllById(ids)

    /**
     * Returns the total count of objects of type E in the data source.
     * @return the number of objects of type E in the data source
     * @throws Exception if an error occurs while counting the objects in the data source
     */
    override fun count(): Long = repository.count()

    /**
     * Deletes an object of type E from the data source by its unique ID.
     * @param id the string ID of the object to be deleted
     * @throws Exception if an error occurs while retrieving or deleting the object from the data source
     */
    override fun deleteById(id: String): Unit = repository.deleteById(id)

    /**
     * Deletes an object of type E from the data source.
     * @param entity the object of type E to be deleted
     * @throws Exception if an error occurs while deleting the object from the data source
     */
    override fun delete(entity: E): Unit = repository.delete(entity)

    /**
     * Overrides the deleteAllById method of the parent class/interface and deletes all records with the given IDs from the repository.
     * @param ids An iterable of String type that contains the IDs of the records to be deleted.
     * @return Nothing (Unit)
     * @throws Exception if the deletion process encounters an error.
     */
    override fun deleteAllById(ids: Iterable<String>): Unit = repository.deleteAllById(ids)

    /**
     * Overrides the deleteAll method of the parent class/interface and deletes all given entities from the repository.
     * @param entities An iterable of type E that contains the entities to be deleted.
     * @return Nothing (Unit)
     * @throws Exception if the deletion process encounters an error.
     */
    override fun deleteAll(entities: Iterable<E>): Unit = repository.deleteAll(entities)

    /**
     * Overrides the deleteAll method of the parent class/interface and deletes all records from the repository.
     * @return Nothing (Unit)
     * @throws Exception if the deletion process encounters an error.
     */
    override fun deleteAll(): Unit = repository.deleteAll()

    /**
     * Overrides the findOne method of the parent class/interface and retrieves an entity matching the given specification.
     * @param spec A Specification of type E that defines the criteria for selecting the entity.
     * @return An Optional containing the matching entity, or an empty Optional if no matching entity is found.
     * @throws Exception if the retrieval process encounters an error.
     */
    override fun findOne(spec: Specification<E>): Optional<E> = repository.findOne(spec)

    /**
     * Overrides the findAll method of the parent class/interface and retrieves a list of entities matching the given specification.
     * @param spec A Specification of type E that defines the criteria for selecting the entities.
     * @return A List of entities matching the given specification, or an empty list if no matching entities are found.
     * @throws Exception if the retrieval process encounters an error.
     */
    override fun findAll(spec: Specification<E>): List<E> = repository.findAll(spec)

    /**
     * Returns a page of objects of type E that match the given specification.
     * @param spec the specification used to filter the objects
     * @param pageable the pagination information used to limit the number of objects returned
     * @return a Page of objects of type E that match the given specification
     * @throws Exception if an error occurs while retrieving the objects from the data source
     */
    override fun findAll(spec: Specification<E>, pageable: Pageable): Page<E> = repository.findAll(spec, pageable)

    /**
     * Overrides the findAll method of the parent class/interface and retrieves a list of entities matching the given specification and sort order.
     * @param spec A Specification of type E that defines the criteria for selecting the entities.
     * @param sort A Sort object that defines the order in which the entities should be returned.
     * @return A List of entities matching the given specification and sort order, or an empty list if no matching entities are found.
     * @throws Exception if the retrieval process encounters an error.
     */
    override fun findAll(spec: Specification<E>, sort: Sort): List<E> = repository.findAll(spec, sort)

    /**
     * Overrides the count method of the parent class/interface and returns the number of entities matching the given specification.
     * @param spec A Specification of type E that defines the criteria for counting the entities.
     * @return The number of entities matching the given specification.
     * @throws Exception if the counting process encounters an error.
     */
    override fun count(spec: Specification<E>): Long = repository.count(spec)

    /**
     * Overrides the exists method of the parent class/interface and checks if there is at least one entity matching the given specification.
     * @param spec A Specification of type E that defines the criteria for checking the existence of the entity.
     * @return True if there is at least one entity matching the given specification, false otherwise.
     * @throws Exception if the checking process encounters an error.
     */
    override fun exists(spec: Specification<E>): Boolean = repository.exists(spec)

    /**
     * Retrieves an object from the data source by its unique ID.
     * @param id the string ID of the object to retrieve
     * @return the object retrieved from the data source, or null if no matching object is found
     * @throws Exception if an error occurs while retrieving the object from the data source
     */
    override fun getByIdOrThrowException(id: String): E {
        val findById: E? = findById(id = id)
        if (findById == null) {
            val message: String = messageSourceService.get(code = "oauth2_user_not_found", params = arrayOf(getClassName(), id))
            log.error(message)
            throw NotFoundException(variables = arrayOf(message))
        }
        return findById
    }

    /**
     * Retrieves an object from the data source by its unique ID or email
     * @param id the string ID of the object to retrieve
     * @param email the string email of the object to retrieve
     * @return the object retrieved from the data source, or null if no matching object is found
     * @throws Exception if an error occurs while retrieving the object from the data source
     */
    override fun getByIdOrEmailOrThrowException(id: String, email: String): E {
        val specification = Specification { root: Root<E>, query: CriteriaQuery<*>?, builder: CriteriaBuilder ->
            val predicates: MutableList<Predicate> = ArrayList()
            val predicateId: Predicate = builder.equal(builder.lower(root.get("id")), id)
            val predicateEmail: Predicate = builder.equal(builder.lower(root.get("email")), email.lowercase())
            predicates.add(element = builder.or(predicateId, predicateEmail))
            query?.where(*predicates.toTypedArray())?.distinct(true)?.restriction
        }

        val findById: Optional<E> = findOne(spec = specification)
        if (!findById.isPresent) {
            val message: String = messageSourceService.get(code = "oauth2_user_not_found", params = arrayOf(getClassName(), id))
            log.error(message)
            throw NotFoundException(variables = arrayOf(message))
        }
        return findById.get()
    }

    /**
     * Creates a HttpHeaders object with an "Authorization" header that contains the provided access token.
     * The access token is formatted as a "Bearer" token and added to the header.
     * @param accessToken The access token to be added to the "Authorization" header.
     * @return The HttpHeaders object with the "Authorization" header containing the access token.
     */
    protected fun createHeaderForToken(accessToken: String): HttpHeaders =
        HttpHeaders()
            .also { h: HttpHeaders ->
                h.add("Authorization", "Bearer $accessToken")
            }

    /**
     * String class name of the service
     */
    abstract fun getClassName(): String?

    /**
     * User operations for generic oauth2 entities to authenticate
     * @param entity Generic oauth2 entity
     * @return User entity
     */
    abstract fun getUser(entity: E): User

    abstract fun getToken(code: String): OAuthTokenResponse

    @Transactional(isolation = Isolation.SERIALIZABLE)
    open fun authenticate(jwtToken: String?, oAuthGoogleUser: E): UserResponseWrapperDto {
        val user: User?
        if (jwtToken != null) {
            jwtTokenProvider.validateToken(token = jwtToken)
            user = userService.findByEmail(email = jwtTokenProvider.getSubjectFromJWT(token = jwtToken))
            MDC.put("userId", "${user.id}")
            if(user.oAuthGoogleUser != null) {
                if(user.oAuthGoogleUser!!.id != oAuthGoogleUser.id) {
                    val message: String = messageSourceService.get(code = "this_account_is_linked_to_another_account")
                    log.error(message)
                    throw ServerException(omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT,
                        statusCode = HttpStatus.FORBIDDEN, variables = arrayOf(message))
                }
                val message: String = messageSourceService.get(code = "this_account_is_already_linked")
                log.error(message)
                throw ServerException(omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT,
                    statusCode = HttpStatus.FORBIDDEN, variables = arrayOf(message))
            }
            if (oAuthGoogleUser.user != user) {
                oAuthGoogleUser.user = user
                save(entity = oAuthGoogleUser)
            }
        }else{
            if (oAuthGoogleUser.user != null) {
                user = oAuthGoogleUser.user
                MDC.put("userId", "${user?.id}")
            }else {
                val email: String? = oAuthGoogleUser.email
                if (email == null) {
                    log.error("Email field can not be null to authenticate for user: ${oAuthGoogleUser.id}")
                    throw NotFoundException(variables = arrayOf(messageSourceService.get(code = "oauth2_email_not_found")))
                }
                if (userService.existsByEmail(email = email)) {
                    user = userService.findByEmail(email = email)
                } else {
                    val userRole: Role? = roleService.findByName(roleName = RoleName.ROLE_USER)
                    if (userRole == null) {
                        val msg: String = messageSourceService.get(code = "role_not_found")
                        log.error(msg)
                        throw ServerException(omaErrorMessageType = OmaErrorMessageType.MANDATORY_INPUT_MISSING,
                            statusCode = HttpStatus.BAD_REQUEST, variables = arrayOf(msg))
                    }
                    val randomStringGenerator: String = RandomStringGenerator(length = 10).next()
                    user = userService.save(user = User(email = email, roles = mutableListOf(userRole), password = passwordEncoder.encode(randomStringGenerator)))
                }
                oAuthGoogleUser.user = user
                MDC.put("userId", "${user.id}")
                save(entity = oAuthGoogleUser)
                log.info("oAuthGoogleUser updated id:${oAuthGoogleUser.id}, user: ${user.id}")
            }
        }
        return userService.generateUserWrapperResponse(userResponseDto = user!!.toDTO())
    }
}