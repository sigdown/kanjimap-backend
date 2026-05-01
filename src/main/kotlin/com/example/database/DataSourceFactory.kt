package com.example.database

import com.example.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

object DataSourceFactory {
    fun create(config: DatabaseConfig): HikariDataSource {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.jdbcUrl
            username = config.username
            password = config.password
            maximumPoolSize = config.maximumPoolSize
            driverClassName = "org.postgresql.Driver"
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        return HikariDataSource(hikariConfig)
    }
}
