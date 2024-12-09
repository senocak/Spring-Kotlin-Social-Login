package com.github.senocak.ratehighway.security

import com.github.senocak.ratehighway.util.ADMIN
import com.github.senocak.ratehighway.util.USER

@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION
)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class Authorize(
    val roles: Array<String> = [
        ADMIN,
        USER,
    ]
)
