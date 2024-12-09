package com.github.senocak.ratehighway.service

import com.github.senocak.ratehighway.util.logger
import org.slf4j.Logger
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import java.util.Locale

@Service
class MessageSourceService(private val messageSource: ResourceBundleMessageSource){
    private val log: Logger by logger()

    fun get(
        code: String,
        params: Array<Any?>? = arrayOfNulls(0),
        locale: Locale? = LocaleContextHolder.getLocale()
    ): String =
        try {
            messageSource.setDefaultEncoding("UTF-8")
            messageSource.getMessage(code, params, locale!!)
        } catch (e: NoSuchMessageException) {
            log.warn("Translation message not found ($locale): $code")
            code
        }
}
