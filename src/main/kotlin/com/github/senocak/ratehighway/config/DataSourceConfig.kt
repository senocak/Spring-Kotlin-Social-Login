package com.github.senocak.ratehighway.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
@ConfigurationProperties(prefix = "spring.datasource")
class DataSourceConfig {
    var url: String? = null
    var username: String? = null
    var password: String? = null
    var driverClassName: String? = null
    var hikari: HikariConfig? = null

    @Bean
    @Primary
    fun dataSource(): DataSource =
        when {
            url!!.contains(other = "jdbc:postgresql") -> DriverManagerDataSource()
                .also { db: DriverManagerDataSource ->
                    db.url = url
                    db.username = username
                    db.password = password
                }
            else -> throw RuntimeException("Not configured")
        }

    @Bean
    fun hikariDataSource(dataSource: DataSource): HikariDataSource =
        HikariDataSource(HikariConfig()
            .also { hds: HikariConfig ->
                hds.dataSource = dataSource
                hds.poolName = hikari?.poolName ?: "SpringKotlinJPAHikariCP"
                hds.minimumIdle = hikari?.minimumIdle ?: 5
                hds.maximumPoolSize = hikari?.maximumPoolSize ?: 20
                hds.maxLifetime = hikari?.maxLifetime ?: 2_000_000
                hds.idleTimeout = hikari?.idleTimeout ?: 30_000
                hds.connectionTimeout = hikari?.connectionTimeout ?: 30_000
                hds.transactionIsolation = hikari?.transactionIsolation ?: "TRANSACTION_READ_COMMITTED"
            }
        )

    override fun toString(): String = "DataSourceConfig(url=$url, username=$username, password=$password, driverClassName=$driverClassName)"
}
