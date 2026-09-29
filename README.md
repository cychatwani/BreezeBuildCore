# Breeze Core

Breeze Core is the standalone Spring Boot Core Platform service for BreezeBuild.

## Stack

- Java 21
- Spring Boot 4
- Gradle
- PostgreSQL
- Flyway
- Clerk JWT authentication

## Prerequisites

- Java 21
- PostgreSQL running locally (default port: `5434`)

## Local configuration

Create an ignored `.env` file in this directory. Do not commit real credentials.

```properties
DB_URL=jdbc:postgresql://localhost:5434/breeze_core
DB_USERNAME=postgres
DB_PASSWORD=replace-me
CLERK_ISSUER_URI=https://your-clerk-instance.clerk.accounts.dev
CLERK_JWK_SET_URI=https://your-clerk-instance.clerk.accounts.dev/.well-known/jwks.json
CLERK_ALLOWED_AUTHORIZED_PARTIES=http://localhost:3000
```

## Run locally

```powershell
.\gradlew.bat bootRun
```

## Run tests

```powershell
.\gradlew.bat test
```

## Observability stack

Breeze Core uses New Relic for infrastructure monitoring, log collection, and Java APM.

### New Relic Java APM agent

The Java agent is available under `observability/newrelic/`:

- `newrelic.jar` is the New Relic Java agent and is intentionally ignored by Git.
- `newrelic.yml` is versioned, contains no secret, and currently disables agent-side log forwarding and distributed tracing.
- New Relic agent diagnostic logs are written to the ignored `observability/newrelic/logs/` directory.

Set the license key only in the shell session that starts the JVM. Do not add it to source code or `newrelic.yml`.

```powershell
$env:NEW_RELIC_LICENSE_KEY = 'your-new-relic-license-key'
.\gradlew.bat bootJar
java -javaagent:observability/newrelic/newrelic.jar `
  -Dnewrelic.config.file=observability/newrelic/newrelic.yml `
  -jar build/libs/breeze-core-0.0.1-SNAPSHOT.jar
```

The New Relic Java agent is not yet automatically attached to `bootRun`.
