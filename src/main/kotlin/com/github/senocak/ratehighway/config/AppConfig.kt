package com.github.senocak.ratehighway.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.github.senocak.ratehighway.security.AuthorizationInterceptor
import com.github.senocak.ratehighway.util.securitySchemeName
import com.github.senocak.ratehighway.util.corePoolSize
import com.github.senocak.ratehighway.util.fromProperties
import com.github.senocak.ratehighway.util.logger
import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.apache.catalina.connector.Connector
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.client5.http.io.HttpClientConnectionManager
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier
import org.apache.hc.core5.ssl.SSLContexts
import org.apache.hc.core5.ssl.TrustStrategy
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.KeyManagementException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.X509Certificate
import java.util.Locale
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSession

@EnableAsync
@Configuration
class AppConfig(
    private val authorizationInterceptor: AuthorizationInterceptor
): WebMvcConfigurer, WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    private val log: Logger by logger()

    override fun customize(factory: TomcatServletWebServerFactory) {
        factory.addConnectorCustomizers(
            TomcatConnectorCustomizer { connector: Connector ->
                connector.setProperty("maxThreads", "2000")
                connector.setProperty("acceptorThreadCount", "1")
            }
        )
    }

    /**
     * Override the configureAsyncSupport method from AsyncSupportConfigurer to set the task executor for asynchronous processing.
     * The task executor is defined as a ThreadPoolTaskExecutor with a core pool size of the corePoolSize value.
     * The max pool size is set to corePoolSize * 1_000.
     * The thread factory for the task executor is set with a name format of "async-thread-%d".
     * The queue capacity is set to unlimited with a value of -1.
     * The default timeout for async processing is set to 120_000 milliseconds.
     * A log message is printed displaying the core pool size, max pool size, keep alive seconds, queue capacity and queue size.
     */
    override fun configureAsyncSupport(configurer: AsyncSupportConfigurer) {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = corePoolSize
        executor.maxPoolSize = corePoolSize * 1_000
        executor.setThreadFactory(ThreadFactoryBuilder().setNameFormat("async-thread-%d").build())
        executor.queueCapacity = -1
        executor.initialize()
        configurer
            .setDefaultTimeout(120_000)
            .setTaskExecutor(executor)
        log.info("Core pool size: ${executor.corePoolSize}, max pool size: ${executor.maxPoolSize}, " +
                "keepAliveSeconds: ${executor.keepAliveSeconds}, queueCapacity: ${executor.queueCapacity}, " +
                "queueSize: ${executor.queueSize}")
    }

    /**
     * Configure simple automated controllers pre-configured with the response
     * status code and/or a view to render the response body. This is useful in
     * cases where there is no need for custom controller logic -- e.g. render a
     * home page, perform simple site URL redirects, return a 404 status with
     * HTML content, a 204 with no content, and more.
     */
    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addRedirectViewController("/", "/index.html")
        registry.addRedirectViewController("/swagger", "/swagger-ui/index.html")
    }

    /**
     * Marking the files as resource
     * @param registry -- Stores registrations of resource handlers for serving static resources
     */
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry
            .addResourceHandler("//**")
            .addResourceLocations("classpath:/static/")
    }

    /**
     * Add Spring MVC lifecycle interceptors for pre- and post-processing of controller method invocations
     * and resource handler requests.
     * @param registry -- List of mapped interceptors.
     */
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry
            .addInterceptor(authorizationInterceptor)
            .addPathPatterns("/v1/**")
        registry
            .addInterceptor(LocaleChangeInterceptor())
    }

    /**
     * Configure "global" cross origin request processing.
     * @param registry -- CorsRegistry assists with the registration of CorsConfiguration mapped to a path pattern.
     */
    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("HEAD", "OPTIONS", "GET", "POST", "PUT", "PATCH", "DELETE")
            .maxAge(3600)
    }

    /**
     * Path matching options such as whether to use parsed or String pattern matching
     * For example: matching /ping and /ping/ same endpoint
     */
    override fun configurePathMatch(configurer: PathMatchConfigurer) {
        configurer.setUseTrailingSlashMatch(true);
    }

    /**
     * Returns a custom OpenAPI specification.
     * The OpenAPI specification is a language-agnostic standard for describing REST APIs.
     * This method returns a custom OpenAPI specification that includes specific components and information.
     * The components added to the specification include a security scheme with the name specified in the AppConstants.securitySchemeName constant,
     * and the type of the security scheme is set to HTTP. The scheme is set to "bearer" and the bearer format is set to "JWT".
     * The information included in the specification includes the title, version, description, terms of service, and license.
     * The title is set to "Rest Api - Kotlin", the version is set to the value of the appVersion parameter, which is passed in as a value from the application's configuration file.
     * The description is set to "Fully completed spring boot project written with kotlin", the terms of service are set to "https://github.com/senocak",
     * and the license is set to Apache 2.0 with a link to "https://springdoc.org".
     * @param appVersion the version of the application to be included in the OpenAPI specification.
     * @return a custom OpenAPI specification.
     */
    @Bean
    fun customOpenAPI(
        @Value("\${spring.application.name}") title: String,
        @Value("\${server.port}") port: String
    ): OpenAPI =
        OpenAPI().components(
            Components().addSecuritySchemes(
                securitySchemeName,
                SecurityScheme()
                    .name(securitySchemeName)
                    .description("JWT auth description")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .`in`(SecurityScheme.In.HEADER)
                    .bearerFormat("JWT")
            )
        ).info(Info()
            .title(title)
            .version("appVersion".fromProperties())
            .description(title)
            .termsOfService("https://github.com/senocak")
            .license(License().name("Apache 2.0").url("https://springdoc.org"))
        )
        .servers(listOf(element = Server().url("http://localhost:$port/").description("Local Server")))

    /**
     * Returns a new instance of the SessionLocaleResolver with a custom default locale.
     * The SessionLocaleResolver is a convenient class for resolving the locale to be used for a user's session.
     * This method returns a new instance of the SessionLocaleResolver with a custom default locale, allowing clients to have
     * multiple independent instances of SessionLocaleResolver with different default locales.
     * The default locale is set using the value of the defaultLocale parameter, which is passed in as a value from the application's configuration file.
     * The default value for the default locale is "en".
     * @param defaultLocale the default locale to be used for the user's session.
     * @return a new instance of the SessionLocaleResolver with a custom default locale.
     */
    @Bean
    fun localeResolver(@Value("\${app.default-locale:en}") defaultLocale: String): LocaleResolver =
        SessionLocaleResolver()
            .also { l: SessionLocaleResolver ->
                l.setDefaultLocale(Locale.Builder().setLanguage(defaultLocale).build())
            }

    /**
     * Returns a new instance of the RestTemplate class.
     * The RestTemplate class is a convenient class for making RESTful web service calls.
     * This method returns a new instance of the RestTemplate class, allowing clients to make multiple independent
     * web service calls using different RestTemplate instances.
     * @return a new instance of the RestTemplate class.
     */
    @Bean
    @Primary
    fun restTemplate(): RestTemplate = RestTemplate()

    /**
     * Returns a new instance of the RestTemplate class by passing SSL
     * The RestTemplate class is a convenient class for making RESTful web service calls.
     * This method returns a new instance of the RestTemplate class, allowing clients to make multiple independent
     * web service calls using different RestTemplate instances.
     * @return a new instance of the RestTemplate class.
     */
    @Bean(name = ["restTemplateByPassSSL"])
    @Throws(NoSuchAlgorithmException::class, KeyManagementException::class, KeyStoreException::class)
    fun restTemplateByPassSSL(): RestTemplate {
        val acceptingTrustStrategy = TrustStrategy { x509Certificates: Array<X509Certificate?>?, authType: String? -> true }
        val sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build()
        val csf = DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier())
        val requestFactory = HttpComponentsClientHttpRequestFactory()
        val connectionManager: HttpClientConnectionManager = PoolingHttpClientConnectionManagerBuilder.create().setTlsSocketStrategy(csf).build()
        val httpClient: CloseableHttpClient = HttpClients.custom().setConnectionManager(connectionManager).build()
        requestFactory.httpClient = httpClient
        val restTemplate = RestTemplate(requestFactory)
        restTemplate.interceptors = arrayListOf<ClientHttpRequestInterceptor>(LoggingRequestInterceptor())
        return restTemplate
    }

    @Bean(name = ["sslTrustedRestTemplate"])
    fun sslTrustedRestTemplate(): RestTemplate {
        HttpsURLConnection.setDefaultHostnameVerifier { _: String?, _: SSLSession? -> true }
        val httpComponentsClientHttpRequestFactory = HttpComponentsClientHttpRequestFactory()
        val milliseconds = 100
        httpComponentsClientHttpRequestFactory.setReadTimeout(milliseconds)
        httpComponentsClientHttpRequestFactory.setConnectionRequestTimeout(milliseconds)
        httpComponentsClientHttpRequestFactory.setConnectTimeout(milliseconds)
        val restTemplate = RestTemplate(httpComponentsClientHttpRequestFactory)
        val stringHttpMessageConverter = StringHttpMessageConverter(StandardCharsets.UTF_8)
        stringHttpMessageConverter.setWriteAcceptCharset(false)
        restTemplate.messageConverters.add(0, stringHttpMessageConverter)
        restTemplate.interceptors = arrayListOf<ClientHttpRequestInterceptor>(LoggingRequestInterceptor())
        return restTemplate
    }

    internal class LoggingRequestInterceptor : ClientHttpRequestInterceptor {
        private val log: Logger by logger()

        override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
            log.info("""
            ===========================request begin================================================
            URI         : ${request.uri}
            Method      : ${request.method}
            Headers     : ${request.headers}
            Request body: ${String(bytes = body, charset = Charsets.UTF_8)}
            ==========================request end================================================
        """)
            val response: ClientHttpResponse = execution.execute(request, body)
            val responseBody: ByteArray = response.body.readBytes()
            log.info("""
            ============================response begin==========================================
            Status code  : ${response.statusCode}
            Status text  : ${response.statusText}
            Headers      : ${response.headers}
            Response body: ${String(bytes = responseBody, charset = Charsets.UTF_8)}
            =======================response end=================================================
        """)
            return CachedClientHttpResponse(response = response, cachedBody = responseBody)
        }

        private class CachedClientHttpResponse(
            private val response: ClientHttpResponse,
            private val cachedBody: ByteArray
        ) : ClientHttpResponse {
            override fun getStatusCode(): HttpStatusCode = response.statusCode
            //override fun getRawStatusCode() = response.rawStatusCode
            override fun getStatusText(): String = response.statusText
            override fun close(): Unit = response.close()
            override fun getHeaders(): HttpHeaders = response.headers
            override fun getBody(): InputStream = ByteArrayInputStream(cachedBody)
        }
    }

    /**
     * We use the PasswordEncoder that is defined in the Spring Security configuration to encode the password.
     * @return -- singleton instance of PasswordEncoder
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    /**
     * Returns a new instance of the ObjectMapper class with custom configurations.
     * The ObjectMapper class is a convenient class for converting between Java objects and JSON.
     * This method returns a new instance of the ObjectMapper class with custom configurations, allowing clients to have
     * multiple independent instances of ObjectMapper with different configurations.
     * The custom configurations applied to the ObjectMapper instance are as follows:
     * DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES is enabled to fail the deserialization process if an unknown property is found.
     * JavaTimeModule is registered to provide support for Java 8 time classes (LocalDate, LocalDateTime, etc.)
     * SerializationFeature.WRITE_DATES_AS_TIMESTAMPS is disabled to write dates as strings rather than timestamps.
     * @return a new instance of the ObjectMapper class with custom configurations.
     */
    @Bean
    fun objectMapper(): ObjectMapper =
        ObjectMapper()
            .also { om: ObjectMapper ->
                om.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                om.registerModule(JavaTimeModule())
                om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            }

    /**
     * Returns a new instance of the ThreadPoolTaskScheduler with custom configurations.
     * The ThreadPoolTaskScheduler is a convenient class for scheduling tasks to be executed asynchronously.
     * This method returns a new instance of the ThreadPoolTaskScheduler with custom configurations, allowing clients to have
     * multiple independent instances of ThreadPoolTaskScheduler with different configurations.
     * The custom configurations applied to the ThreadPoolTaskScheduler instance are as follows:
     * The pool size is set to the value of the corePoolSize variable.
     * The thread name prefix is set to "async-thread-".
     * @return a new instance of the ThreadPoolTaskScheduler with custom configurations.
     */
    @Bean(name = ["threadPoolTaskExecutor"])
    fun getThreadPoolTaskScheduler(): ThreadPoolTaskScheduler =
        ThreadPoolTaskScheduler()
            .also { tpts: ThreadPoolTaskScheduler ->
                tpts.poolSize = corePoolSize
                tpts.setThreadNamePrefix("async-thread-")
            }
}