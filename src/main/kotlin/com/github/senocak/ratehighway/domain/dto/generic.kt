package com.github.senocak.ratehighway.domain.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.Page
import java.util.Locale

@JsonIgnoreProperties(ignoreUnknown = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
abstract class BaseDto {
    @JsonIgnore
    @Schema(example = "tr", description = "Locale field", required = true, name = "locale", type = "Locale")
    var locale: Locale = LocaleContextHolder.getLocale()
}

@JsonPropertyOrder("statusCode", "error", "variables")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@JsonTypeName("exception")
class ExceptionDto : BaseDto() {
    var statusCode = 200
    var error: OmaErrorMessageTypeDto? = null
    var variables: Array<String?> = arrayOf(String())

    @JsonPropertyOrder("id", "text")
    class OmaErrorMessageTypeDto(val id: String? = null, val text: String? = null)
}

@JsonPropertyOrder("page", "pages", "total", "items")
open class PaginationResponse<T, P>(page: Page<T>, items: List<P>) : BaseDto() {
    @Schema(example = "1", description = "Page", required = true, name = "page", type = "String")
    var page: Int = page.number

    @Schema(example = "3", description = "Pages", required = true, name = "pages", type = "String")
    var pages: Int = page.totalPages

    @Schema(example = "10", description = "Token", required = true, name = "total", type = "String")
    var total: Long = page.totalElements

    @ArraySchema(schema = Schema(description = "items", required = true, type = "ListDto"))
    var items: List<P>? = items
}

data class SuccessResponse(
    @Schema(example = "Message is here", description = "Response message field", required = true, name = "message", type = "Integer")
    var message: String? = null,

    @Schema(example = "Response data is here", description = "Generic data field", name = "data", type = "Object")
    var data: Any? = null
): BaseDto()
