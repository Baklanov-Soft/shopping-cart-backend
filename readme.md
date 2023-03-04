# shopping-cart-backend

Usage:
`docker-compose up -d`

Note: `docker-compose.yml` is using `linux/amd64` arch by default, see other tags on docker hub

OpenAPI docs:
`localhost:8080/api/v1/docs`

Container repo:
https://hub.docker.com/repository/docker/denisnovac/shopping-cart-backend/general

## Test

```
For unit tests:
sbt:shopping-cart-backend> test 

For integration tests:
sbt:shopping-cart-backend> IntegrationTest/test
```

### Coverage

```
for unit tests:
$ sbt clean coverage test
$ sbt coverageReport

for integration tests:
$ sbt clean coverage it:test
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

