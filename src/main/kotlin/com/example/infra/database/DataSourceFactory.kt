package com.example.database

import com.example.config.Config
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

object DataSourceFactory {
    fun create(config: Config): HikariDataSource {
        val database = config.database
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = database.jdbcUrl
            username = database.user
            password = database.password
            maximumPoolSize = database.maxPoolSize
            driverClassName = "org.postgresql.Driver"
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        return HikariDataSource(hikariConfig)
    }
}
