package org.baklanovsoft.shoppingcart.config

case class ApplicationConfig(
    database: DatabaseConfig,
    admin: AdminConfig
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
