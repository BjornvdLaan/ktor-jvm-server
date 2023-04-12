package com.fortysevendegrees.env

import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.fx.coroutines.ResourceScope
import com.fortysevendegrees.ServerError.PostgresError
import com.fortysevendegrees.sqldelight.NativePostgres
import com.zaxxer.hikari.HikariDataSource

suspend fun ResourceScope.postgres(config: Env.Postgres): Either<PostgresError, NativePostgres> = either {
    val driver = catch({
        install({
            val ds = HikariDataSource()
            ds.jdbcUrl = "jdbc:postgresql://${config.host}:${config.port}/${config.databaseName}"
            ds.driverClassName = "org.postgresql.Driver"
            ds.username = config.user
            ds.password = config.password
            ds.asJdbcDriver()
        }) { driver, _ -> driver.close() }
    }) { illegal: IllegalArgumentException -> raise(PostgresError(illegal)) }
    NativePostgres(driver).also {
        NativePostgres.Schema.create(driver).await()
    }
}
