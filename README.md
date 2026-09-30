# Breeze Core

Breeze Core is the standalone Spring Boot Core Platform service for BreezeBuild.

## Stack

- Java 21
- Spring Boot 4
- Gradle
- PostgreSQL
- Flyway
- Clerk JWT authentication
- Clerk Java Backend API SDK for profile retrieval

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
CLERK_SECRET_KEY=sk_replace-me
```

`CLERK_SECRET_KEY` is needed only for the background Clerk profile-sync worker. Without it,
provisioning still succeeds and the outbox retains pending work; the worker logs one warning
and does not claim events. Keep the real key in the ignored `.env` or your secret manager.

## Profile-sync outbox

Provisioning commits the Core user and one initial profile-sync event together. A dedicated
Resilience4j `ThreadPoolBulkhead` claims events with PostgreSQL `SKIP LOCKED`, calls Clerk
outside the claim transaction, and retries failures with a lease and bounded backoff.
The bulkhead has no in-memory queue, so a busy pool leaves work in PostgreSQL. Configure it
with `BREEZE_OUTBOX_WORKER_THREADS` (default `1`). Optional tuning variables are
`BREEZE_OUTBOX_POLL_INTERVAL_MS` (`1000`), `BREEZE_OUTBOX_LEASE_SECONDS` (`60`),
`BREEZE_OUTBOX_MAX_ATTEMPTS` (`5`), and `BREEZE_OUTBOX_RETRY_BASE_SECONDS` (`5`).
Restart Core after changing these settings. Existing `PENDING` users created before this
migration receive an event on their next provisioning request.

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
- `newrelic.yml` is versioned, contains no secret, and enables agent-side log forwarding. Distributed tracing is currently disabled.
- New Relic agent diagnostic logs are written to the ignored `logs/` directory.

Set the license key only in the shell session that starts the JVM. Do not add it to source code or `newrelic.yml`.

```powershell
$env:NEW_RELIC_LICENSE_KEY = 'your-new-relic-license-key'
.\gradlew.bat bootJar
java -javaagent:observability/newrelic/newrelic.jar `
  -Dnewrelic.config.file=observability/newrelic/newrelic.yml `
  -jar build/libs/breeze-core-0.0.1-SNAPSHOT.jar
```

When the agent JAR and configuration are present, `bootRun` attaches the New Relic Java agent.
