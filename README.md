# Liquibase Automated Changelog Generation for Spring Boot with PostgreSQL

This repository demonstrates an automated Liquibase workflow for a Spring Boot
project using PostgreSQL. The primary goal is to automatically generate Liquibase
changelogs from JPA entity changes (via a GitHub Actions workflow), apply
migrations in CI, and let developers run Liquibase locally.

Why this project exists

When working with JPA entities it's easy for the Java model and the database
schema to drift. This project demonstrates a workflow that:

- Detects changes to JPA entities in pull requests
- Starts a temporary PostgreSQL instance in CI
- Applies current migrations (if any)
- Generates a diff (based on Hibernate / JPA mapping vs database) using Liquibase
- If changes are detected, auto-commits a new, numbered changelog under
  `src/main/resources/db/changelog/changes/` and creates a PR comment with 
  the changelog contents

This automates the mundane part of producing a starting changelog for schema
updates while still leaving review and edits to developers.

Important profiles and their behavior

- `create-db` (special purpose):
  - This profile intentionally does NOT run Liquibase. Instead, it relies on
    Hibernate's schema generation to create the initial database schema (for 
    quick DB creation or one-off setups).
  - Use this profile when you want Hibernate to create tables automatically (for
    example, in a fresh environment or to bootstrap a database before 
    generating changelogs).

- `local`:
  - The development profile that uses Liquibase to apply migrations on startup.
  - Typical local workflow: update or add changelogs under
    `src/main/resources/db/changelog`, then run the app with `local` to have 
    Liquibase apply those changes.

- `staging` / `prod`:
  - Profiles meant for non-local environments; they should contain production-ready
    datasource settings and Liquibase will apply migrations as part of startup 
    unless explicitly disabled.

Where changelogs live

- Master changelog: `src/main/resources/db/changelog/db.changelog-master.xml`
- Versioned changelogs: `src/main/resources/db/changelog/changes/` (named like
  `001-initial-schema.xml`)
- Seed data and SQL helpers: `src/main/resources/db/changelog/seed-data.xml`,
  `src/main/resources/db/sql/`

Key files
- `pom.xml` — Maven configuration with the Liquibase Maven plugin.
- `.github/workflows/liquibase-auto-changelog.yml` — CI workflow that detects JPA
  changes and auto-generates changelogs.
- `src/main/java/.../entity/Person.java` — example JPA entity used to demonstrate
  diffs and changelogs.

GitHub Actions workflow: behavior and expectations

This repository includes an automated workflow (`.github/workflows/liquibase-auto-changelog.yml`).
Here's what it does in short:

1. Triggers on pull requests to the `development` branch when files under
   `src/main/java/**/entity/**` or `src/main/java/**/model/**` change.
2. Spins up a PostgreSQL service in the job (containerized) and waits for it to
   become ready.
3. Applies existing Liquibase migrations against the temporary DB using Maven
   (`mvn liquibase:update`) so the DB reflects the current migration state.
4. Runs a Liquibase `diff`/`diffChangeLog` step using a `hibernate:` reference
   URL to compare the JPA model with the current DB and generate a temporary 
   changelog if there are differences.
5. If differences are detected, the job:
   - Renames the generated changelog to a numbered filename like `002-my-change.xml`
     and places it under `src/main/resources/db/changelog/changes/`.
   - Normalizes the changeset author and id, commits, and pushes the new changelog
     to the PR branch.
   - Posts a comment on the PR with the changelog contents and requests a review
     (and adds a `changelog-review-required` label).
6. If no changes are detected, it posts a short comment confirming entities are
   in sync.

What to expect and recommended review steps

- The workflow auto-generates a changelog that should be reviewed before merging.
  Auto-generated changelogs are a starting point — they often need adjustments:
  - Check constraints and nullability.
  - Split large changesets into smaller logical units if appropriate.
  - Update or maintain change set IDs, authors, and descriptions if you have a
    specific convention.
- Once reviewed, the changelog stays in `src/main/resources/db/changelog/changes/`
  and will be applied by Liquibase on subsequent runs.

Local development: practical commands

- Build the project:

```bash
mvn clean package
```

- Run locally with Liquibase-enabled `local` profile (recommended for development
  after you add or update changelogs):

```bash
# Using spring-boot:run
mvn -Dspring-boot.run.profiles=local spring-boot:run

# or run the packaged jar
java -jar target/liquibase-setup-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

- Run Hibernate-only `create-db` profile (creates schema from entities without
  Liquibase):

```bash
mvn -Dspring-boot.run.profiles=create-db spring-boot:run
```

- Run Liquibase Maven plugin against an environment (for example to generate a
  changelog or apply migrations manually):

```bash
# Apply migrations (make sure the profile or properties provide DB credentials)
mvn liquibase:update -Dspring.profiles.active=local

# Generate a changelog snapshot (example)
mvn liquibase:generateChangeLog -DoutputChangeLogFile=src/main/resources/db/changelog/changes/new-changelog.xml
```

Notes about credentials

- For local runs, update the appropriate `src/main/resources/application-*.properties`
  file with your DB URL, username, and password. For example, update 
  `application-local.properties` for development.
- In CI, the workflow uses a temporary PostgreSQL container with credentials
  configured in the job. Never commit production credentials into the 
  repository.

Maintaining the auto-generation workflow

- The workflow is a helpful starting point, but it should be treated as an
  assistive tool:
  - Regularly review generated changelogs.
  - Prefer small, focused entity changes to produce smaller, easier-to-review
    changelogs.
  - If your project uses advanced Postgres features (extensions, custom types),
    the auto-generated changelog may need manual edits.

Troubleshooting common issues

- If the workflow fails to generate a changelog:
  - Ensure the PR includes compiled classes or that the job classpath is configured
    correctly for Hibernate to inspect entities. The workflow uses Maven and 
    `liquibase-hibernate6` to load JPA mappings.
  - Look at the workflow logs — the `liquibase:diff` output typically explains
    the mismatch or any errors.
- If the `create-db` profile leaves the DB in a different state than Liquibase
  changes expect, run `mvn liquibase:update` against a fresh DB to reconcile 
  and capture the required changelogs.

Security and best practices

- Do not store production credentials in property files. Use environment variables
  or a secrets manager.
- Keep the CI job's database ephemeral and scoped to the job. The included workflow
  uses a PostgreSQL container for this reason.
