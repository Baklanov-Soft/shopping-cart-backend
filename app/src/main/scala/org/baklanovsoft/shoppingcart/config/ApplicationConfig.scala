package org.baklanovsoft.shoppingcart.config

case class ApplicationConfig(
    database: DatabaseConfig,
    admin: AdminConfig,
    http: HttpConfig
)

case class DatabaseConfig(
    host: String = "db",
    port: Int = 5432,
    user: String = "postgres",
    password: String = "postgres",
    database: String,
    migrateOnStart: Boolean = true
)

case class AdminConfig(
    name: String = "admin",
    password: String = "admin"
)

case class HttpConfig(
    host: String = "0.0.0.0",
    port: Int = 8080,
    corsAllowedOrigins: List[String] = List.empty
)
