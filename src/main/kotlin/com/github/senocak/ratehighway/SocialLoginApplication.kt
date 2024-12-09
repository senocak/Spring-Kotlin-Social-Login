package com.github.senocak.ratehighway

import com.github.senocak.ratehighway.domain.Role
import com.github.senocak.ratehighway.domain.RoleRepository
import com.github.senocak.ratehighway.domain.User
import com.github.senocak.ratehighway.domain.UserRepository
import com.github.senocak.ratehighway.util.RoleName
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.event.EventListener
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Date

val log: Logger = LoggerFactory.getLogger("main")

@SpringBootApplication
class KotlinApplication(
    private val roleRepository: RoleRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${spring.jpa.hibernate.ddl-auto:update}") private val ddl: String
){
    @EventListener(value = [ApplicationReadyEvent::class])
    fun init(event: ApplicationReadyEvent) {
        log.info("[${event.applicationContext.id}] is started in ${event.timeTaken.seconds} seconds")
        MDC.put("userId", "init")
        System.getenv().forEach { (key: String, value: String) -> log.info("Env: $key -> $value") }
        if (ddl == "create" || ddl == "create-update" ||
            (ddl == "update" && (roleRepository.count() < 1 || userRepository.count() < 1))) {
            log.info("DB persisting is started...")
            roleRepository.deleteAll()
            userRepository.deleteAll()
            log.info("Everything is deleted in db...")

            val roleUser: Role = roleRepository.save(Role(name = RoleName.ROLE_USER))
            val roleAdmin: Role = roleRepository.save(Role(name = RoleName.ROLE_ADMIN))
            val password: String = passwordEncoder.encode("asenocak")
            var userUserRole: User = userRepository.save(User(email = "anil1@senocak.com", password = password, roles = mutableListOf(roleUser)))
            var userAdminRole: User = userRepository.save(User(email = "anil2@senocak.com", password = password, roles = mutableListOf(roleUser, roleAdmin)))
            log.info("DB persisting is completed...")
        }
        MDC.remove("userId")
    }
}

fun main(args: Array<String>) {
    SpringApplicationBuilder(KotlinApplication::class.java)
        .bannerMode(Banner.Mode.CONSOLE)
        .logStartupInfo(true)
        .listeners(
            ApplicationListener {
                    event: ApplicationEvent ->
                log.info("######## ApplicationEvent> ${event.javaClass.canonicalName}")
            }
        )
        .build()
        .run(*args)
}