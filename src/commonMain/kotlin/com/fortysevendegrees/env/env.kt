package com.fortysevendegrees.env

import arrow.core.Either
import arrow.core.EitherNel
import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.raise.recover
import arrow.core.raise.zipOrAccumulate
import arrow.core.recover
import arrow.typeclasses.Semigroup
import com.fortysevendegrees.ServerError.ConfigurationError

data class Env(val postgres: Postgres, val http: Http) {
  data class Http(val host: String, val port: Int)
  data class Postgres(val host: String, val port: Int, val user: String, val databaseName: String, val password: String)
}

private fun Raise<String>.env(name: String): String =
  ensureNotNull(System.getenv(name)) { "\"$name\" configuration missing" }

private fun <A : Any> Raise<String>.env(name: String, transform: Raise<String>.(String) -> A?): A {
  val string = env(name)
  return ensureNotNull(transform(string)) { "\"$name\" configuration found with $string" }
}

fun Raise<ConfigurationError>.env(): Env =
  Either.zipOrAccumulate(
    either { postgres() },
    http()
  ) { postgres, http -> Env(postgres = postgres, http = http) }
    .recover { errors: NonEmptyList<String> ->
      val message = errors.joinToString(prefix = "Environment failed to load:\n", separator = "\n")
      raise(ConfigurationError(message))
    }.bind()

private fun http(): EitherNel<String, Env.Http> =
  Either.zipOrAccumulate(
    either<String, String> { env("HOST") },
    either { env("SERVER_PORT") { it.toIntOrNull() } }
  ) { host, port -> Env.Http(host, port) }


private fun Raise<NonEmptyList<String>>.postgres(): Env.Postgres =
  zipOrAccumulate(
    { env("POSTGRES_HOST") },
    { env("POSTGRES_PORT") { it.toIntOrNull() } },
    { env("POSTGRES_USERNAME") },
    { env("POSTGRES_PASSWORD") },
    { env("POSTGRES_DB_NAME") }
  ) { host, port, user, password, databaseName ->
    Env.Postgres(host, port, user, databaseName, password)
  }
