package com.github.senocak.ratehighway.exception

import com.github.senocak.ratehighway.util.OmaErrorMessageType
import org.springframework.http.HttpStatus

abstract class RestException(msg: String, t: Throwable? = null): Exception(msg, t)

open class ServerException(var omaErrorMessageType: OmaErrorMessageType, open var variables: Array<String?>, var statusCode: HttpStatus):
    RestException("OmaErrorMessageType: $omaErrorMessageType, variables: $variables, statusCode: $statusCode")

class NotFoundException(override var variables: Array<String?>):
    ServerException(omaErrorMessageType = OmaErrorMessageType.NOT_FOUND, variables = variables, statusCode = HttpStatus.NOT_FOUND)

class ConfigException(t: Throwable):
    RestException("ConfigException: ${t.message}")