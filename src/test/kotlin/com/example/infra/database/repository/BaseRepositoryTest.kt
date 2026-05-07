package com.example.infra.database.repository

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.TestInstance
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.PostgreSQLContainer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseRepositoryTest {

    companion object {
        val postgres = PostgreSQLContainer<Nothing>("postgres:16").apply {
            withDatabaseName("app_test")
            withUsername("app")
            withPassword("app")
        }
    }

    @BeforeAll
    fun initializeDb() {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable, "Docker is not available for Testcontainers")

        postgres.start()

        org.jetbrains.exposed.v1.jdbc.Database.connect(
            url = postgres.jdbcUrl,
            user = postgres.username,
            password = postgres.password,
            driver = "org.postgresql.Driver",
        )

        Flyway.configure()
            .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
            .load()
            .migrate()
    }

    @AfterAll
    fun shutdownDb() {
        if (postgres.isRunning) {
            postgres.stop()
        }
    }

    @BeforeEach
    fun cleanupDatabase() {
        transaction {
            exec(
                """
                TRUNCATE TABLE
                    word,
                    kanji,
                    learning_block,
                    app_user
                RESTART IDENTITY CASCADE;
                """.trimIndent()
            )
        }
    }
}
