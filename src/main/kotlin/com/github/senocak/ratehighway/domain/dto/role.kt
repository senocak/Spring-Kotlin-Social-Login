package com.github.senocak.ratehighway.domain.dto

import com.github.senocak.ratehighway.util.RoleName
import io.swagger.v3.oas.annotations.media.Schema

data class RoleResponse(
    @Schema(example = "ROLE_USER", description = "Name of the role", required = true, name = "name")
    var name: RoleName
): BaseDto() {
    override fun toString(): String = "RoleResponse(name=$name)"
}