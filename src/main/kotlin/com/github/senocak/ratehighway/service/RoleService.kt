package com.github.senocak.ratehighway.service

import com.github.senocak.ratehighway.domain.Role
import com.github.senocak.ratehighway.domain.RoleRepository
import com.github.senocak.ratehighway.util.RoleName
import org.springframework.stereotype.Service

@Service
class RoleService(private val roleRepository: RoleRepository) {

    /**
     * @param roleName -- enum variable to retrieve from db
     * @return -- Role object retrieved from db
     */
    fun findByName(roleName: RoleName): Role? = roleRepository.findByName(roleName).orElse(null)
}