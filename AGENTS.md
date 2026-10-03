# Breeze Core Agent Guide

## Project identity

`breeze-core` is the Core Platform service for BreezeBuild. BreezeBuild is an AI-assisted platform for generating, testing, reviewing, and deploying production-oriented Spring Boot applications.

The SplitEasy project at `../SplitEasy/core` (relative to the BreezeBuild repository root) is a read-only reference for Java, Spring Boot, PostgreSQL, transaction, migration, and testing conventions. BreezeBuild is a different product with a different domain. Do not copy SplitEasy-specific business logic, authentication, event contracts, service boundaries, or infrastructure assumptions.

This project is at an early stage. Implement only the layer and behavior requested. Do not create speculative abstractions, services, repositories, endpoints, or infrastructure.

`breeze-core` is a standalone Spring Boot application and a standalone Gradle build. It is not a composite build or a multi-module build. Do not restructure it to resemble SplitEasy's build layout.
Each BreezeBuild service has its own Git repository and build, with independent tests, configuration, and deployment as those are implemented. Cross-service API contracts should be explicit and verified when a feature spans repositories.

## Current platform context

- The browser application is a Next.js/React browser IDE.
- Browser traffic should eventually enter through a Spring Boot API Gateway/BFF.
- `breeze-core` owns platform metadata such as users, projects, GitHub connections, and approvals.
- Breeze Workspace owns each project's source-code workspace, Spring Boot project generation, files, and revisions. Builds and local/preview execution are later Workspace responsibilities.
- The Agentic service will use Workspace APIs to inspect and modify code. Temporal will orchestrate long-running flows such as project initialization.
- BreezeBuild is intentionally polyglot. Use the technology appropriate to each architectural component rather than copying SplitEasy's Java-only approach.
- The Agentic service is a separate Python/FastAPI service intended for LangGraph, tools, RAG/context, and LLM routing.
- The API Gateway/BFF, Core Platform, and Workspace are Java/Spring Boot services.
- Other architectural components remain in their own repositories unless the user explicitly changes that decision.
- PostgreSQL is the Core Platform system of record.
- Billing is out of scope for now.

Do not collapse these boundaries without an explicit request. During early local development, temporary direct calls may exist, but code should not make that coupling permanent.

## Java and package conventions

- Use Java 21 and the existing Spring Boot/Gradle configuration.
- The root package is `dev.chirag45.breeze_core`.
- JPA user/domain entities currently belong under `dev.chirag45.breeze_core.entities`.
- Shared domain enums belong under `dev.chirag45.breeze_core.enums`.
- Outbox enums belong under `dev.chirag45.breeze_core.enums.outbox`; other outbox code belongs under `dev.chirag45.breeze_core.outbox`.
- Prefer constructor injection.
- Keep controllers thin and keep transaction boundaries in application/service code.
- Use typed enums and value types instead of unvalidated strings where practical.
- Use `Instant` for persisted timestamps and PostgreSQL `timestamptz` in migrations.
- Use UUID primary keys unless a feature explicitly requires another identifier strategy.
- Every BreezeBuild-generated UUID, including database IDs, outbox IDs, and `X-Request-ID` correlation IDs, must be UUIDv7. Never use `UUID.randomUUID()` for a BreezeBuild-generated identifier. Accept an inbound correlation ID only when it is a valid UUIDv7; otherwise replace it with a new UUIDv7. External identifiers such as Clerk user IDs are not subject to this rule.
- Keep database and Java names explicit when ambiguity would make a migration or query harder to review.

## Authentication and local users

- Clerk is the authentication provider and token issuer. BreezeBuild must not issue a second user authentication token.
- Spring Security must validate Clerk session JWTs before protected code executes.
- Derive the Clerk user ID from the verified JWT `sub` claim. Never trust a Clerk user ID supplied in a request body.
- Maintain a local user record for BreezeBuild-owned context and relationships.
- `clerk_user_id` is immutable and unique and is the natural idempotency key for user provisioning.
- Provisioning must be idempotent and safe under concurrent requests. Enforce this with a database unique constraint, not only a read-before-write check.
- The initial request may create a minimal local user immediately. Clerk profile enrichment must not block authentication or the response.
- Do not persist Clerk passwords, session tokens, or raw JWTs.

## Profile synchronization

- Profile enrichment is asynchronous.
- Creating the local user and recording the profile-sync work must happen in the same database transaction.
- Webhooks may keep profile information current, but webhook delivery is asynchronous and must not be required for a user's first authenticated request.
- A failed or delayed profile sync must leave the local user usable with nullable profile fields and a clear sync status.

## Transactional outbox

- The outbox closes the crash gap between committing domain state and recording required asynchronous work.
- Delivery is at least once. Every handler and future consumer must be idempotent.
- Multi-node workers must claim disjoint batches with PostgreSQL row locking, normally `FOR UPDATE SKIP LOCKED`.
- Claim work in a short transaction, commit the claim, and perform remote calls outside that transaction.
- Claims require timeout/lease recovery so another node can reclaim work after a crash.
- Bound retries and retain a terminal/dead status with a useful, size-limited error.
- Do not add foreign keys from outbox rows to domain aggregates; asynchronous records may outlive an aggregate.
- Keep the outbox transport-agnostic. Kafka is not selected for BreezeBuild merely because SplitEasy may use it. SQS is a possible future transport, but no broker should be introduced until explicitly chosen.
- If a broker is added later, use the outbox event ID for deduplication and preserve idempotent consumption.

## PostgreSQL and migrations

- The local database is `breeze_core` at `localhost:5434` by default.
- Connection settings remain overridable through `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
- Flyway should own schema creation and evolution.
- Once migrations exist for a mapped schema, prefer Hibernate schema validation rather than automatic schema mutation.
- Put concurrency and integrity guarantees in PostgreSQL constraints and atomic SQL where appropriate.
- Test PostgreSQL-specific behavior such as JSONB, `SKIP LOCKED`, and concurrent claims against PostgreSQL rather than an in-memory substitute.

## Change discipline

- Preserve existing user changes and avoid unrelated refactors.
- Add only what the current task needs.
- Keep this repository as one standalone Java/Spring Boot application unless the user explicitly changes that direction.
- Do not add subprojects or composite-build wiring to `breeze-core`. Polyglot services should remain separate applications with their own builds and runtimes.
- Do not introduce Kafka, SQS, Kubernetes, Redis, or another dependency solely because it appears in an architecture direction or in SplitEasy.
- Do not copy secrets or environment files from the reference project.
- Prefer small, reviewable changes and verify them proportionally to their risk.
