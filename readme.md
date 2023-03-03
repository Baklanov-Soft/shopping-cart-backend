# shopping-cart-backend

Usage:
`docker-compose up -d`

Note: `docker-compose.yml` is using `linux/amd64` arch by default, see other tags on docker hub

OpenAPI docs:
`localhost:8080/api/v1/docs`

Container repo:
https://hub.docker.com/repository/docker/denisnovac/shopping-cart-backend/general

## Build

This Scala project is using `sbt-assembly` plugin. To build it run `assembly` in sbt.

Artifact will be compiled as **JAR** in `target/scala-2.13`.

Build without sbt/jdk through docker:

```bash
docker run -it --rm -v "$(pwd)":/root --network host hseeberger/scala-sbt:eclipse-temurin-17.0.2_1.6.2_2.13.8 sbt

assembly
```

