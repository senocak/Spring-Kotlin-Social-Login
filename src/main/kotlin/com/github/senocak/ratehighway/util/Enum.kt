package com.github.senocak.ratehighway.util

import com.fasterxml.jackson.annotation.JsonValue
import com.github.senocak.ratehighway.exception.NotFoundException
import java.util.Optional

enum class RoleName(@JsonValue val role: String) {
    ROLE_USER(role = USER),
    ROLE_ADMIN(role = ADMIN);

    companion object {
        fun fromString(r: String): RoleName {
            for (rn: RoleName in entries) {
                if (rn.role == r || rn.name == r) {
                    return rn
                }
            }
            throw NotFoundException(variables = arrayOf("Role: $r"))
        }
    }
}

enum class OmaErrorMessageType(val messageId: String, val text: String) {
    BASIC_INVALID_INPUT(messageId = "SVC0001", text = "Invalid input value for message part %1"),
    GENERIC_SERVICE_ERROR(messageId = "SVC0002", text = "The following service error occurred: %1. %2"),
    GENERIC_ERROR(messageId = "SVC0003", text = "The following error occurred: %1"),
    EXTRA_INPUT_NOT_ALLOWED(messageId = "SVC0004", text = "Input %1 %2 not permitted in request"),
    MANDATORY_INPUT_MISSING(messageId = "SVC0005", text = "Mandatory input %1 %2 is missing from request"),
    UNAUTHORIZED(messageId = "SVC0006", text = "UnAuthorized Endpoint"),
    JSON_SCHEMA_VALIDATOR(messageId = "SVC0007", text = "Schema failed."),
    NOT_FOUND(messageId = "SVC0008", text = "Entry: {0} is not found");
}

enum class OAuth2Services(val service: String) {
    GOOGLE(service = "google"),
    FACEBOOK(service = "facebook"),
    GITHUB(service = "github"),
    LINKEDIN(service = "linkedin"),
    TWITTER(service = "twitter");

    companion object {
        fun fromString(service: String): OAuth2Services {
            for (oas: OAuth2Services in entries) {
                if (oas.service == service || oas.name == service) {
                    return oas
                }
            }
            throw NotFoundException(variables = arrayOf("OAuth2Services: $service"))
        }
    }
}
