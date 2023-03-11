# shopping-cart-backend

Project of shopping cart made while reading **Practical FP in Scala** by Gabriel Volpe, second edition.

Usage: `docker-compose up -d`

Note: `docker-compose.yml` is using `linux/amd64` arch by default, see other tags on docker hub.

OpenAPI docs are exposed on route: `localhost:8080/api/v1/docs`

### Environment variables for container:

- Database:
    - DATABASE_HOST - host of postgres, should be equal to service name if runned in the same docker compose file;
    - DATABASE_PORT - port of postgres;
    - DATABASE_DB - name of the database, should be created before running the service;
    - DATABASE_USER - name of database user;
    - DATABASE_PASSWORD - password of database user;
    - DATABASE_MIGRATE - if set to `false` - app won't do the migration at the start.
- Service:
    - ADMIN_NAME - name of the initial admin user (will be created after migrations);
    - ADMIN_PASSWORD - password of the initial user.

Container repo:
https://hub.docker.com/repository/docker/denisnovac/shopping-cart-backend/general

## Test

```
For unit tests:
sbt:shopping-cart-backend> test 

For integration tests:
sbt:shopping-cart-backend> IntegrationTest/test

To test only one integration test always include shared resource:
sbt:shopping-cart-backend> IntegrationTest/testOnly *SharedPostgresContainer *UsersServiceSpec
```

### Coverage

```bash
$ sbt clean coverage test

# no clean here because it will delete unit test coverage results
$ sbt coverage it:test 

$ sbt coverageReport
```

Report will be in `app/target/scala-2.13/scoverage-report`

## Build

This Scala project is using `sbt-assembly` plugin. To build it run in sbt:

```
sbt:shopping-cart-backend> project app
sbt:shopping-cart-backend> assembly
```

Artifact will be compiled as **JAR** in `app/target/scala-2.13`.

Build without sbt/jdk through docker:

```bash
docker run -it --rm -v "$(pwd)":/root --network host hseeberger/scala-sbt:eclipse-temurin-17.0.2_1.6.2_2.13.8 sbt

project app
assembly
```

