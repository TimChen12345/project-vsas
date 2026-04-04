# VSAS — Virtual Scroll Archive System

A **Spring Boot** web app for a digital scroll library: browse, search, preview, upload, and download scrolls. It includes user registration and sign-in, JWT-based authentication, admin user management, and scroll statistics.

---

## Requirements

| Software | Notes |
|----------|--------|
| **JDK 17+** | Matches `java.version` in `pom.xml` |
| **Maven 3.8+** | Build and run |
| **MySQL 8.x** | Persistence; reachable from the machine where the app runs |

---

## 1. Prepare the MySQL database

1. Start the MySQL server.
2. Create a database (name must match configuration; default below):

```sql
CREATE DATABASE soft2412_assignment2
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

3. Default connection settings are in `src/main/resources/application.yml`:

- Host: `localhost:3306`
- Database: `soft2412_assignment2`
- Username: `root`

If your setup differs, edit `spring.datasource.url` and `username` in `application.yml`, or override them using your preferred mechanism (e.g. environment variables).

---

## 2. Local secrets (required)

The committed `application.yml` **does not** include the MySQL password or the JWT signing secret. Add a local file **`application-local.yml`** (listed in `.gitignore` so it is never pushed).

**Steps:**

1. Copy the example:

```bash
cp src/main/resources/application-local.example.yml src/main/resources/application-local.yml
```

2. Edit `application-local.yml`:

- **`spring.datasource.password`**: your MySQL user password (e.g. for `root`).
- **`jwt.secret`**: a **long, random** string used to sign and verify JWTs.  
  - Example: `openssl rand -base64 48`  
  - Do not use a very short value or startup may fail.

On launch, Spring loads `application-local.yml` via `spring.config.import`. Without this file (or equivalent properties), database access and JWT will not work correctly.

---

## 3. Build and run

From the project root:

```bash
mvn clean package -DskipTests
java -jar target/assignment2-1.0.0-SNAPSHOT.jar
```

For development:

```bash
mvn spring-boot:run
```

Default HTTP port: **8080** (`server.port` in `application.yml`).

---

## 4. Using the app (browser)

After the server starts, open:

| URL | Description |
|-----|-------------|
| [http://localhost:8080/](http://localhost:8080/) | Landing page |
| [http://localhost:8080/library.html](http://localhost:8080/library.html) | Main library UI (list, filters, preview, upload/download, account, admin) |
| [http://localhost:8080/register.html](http://localhost:8080/register.html) | Sign up (new accounts are **USER**) |
| [http://localhost:8080/login.html](http://localhost:8080/login.html) | Sign in; redirects to the library |

### Built-in administrator (created on first startup)

If no user named `admin` exists, the app seeds a default admin at startup:

| Field | Value |
|-------|--------|
| Username | `admin` |
| Password | `admin123` |
| ID key | `SYS-ADMIN` |

**Security:** Change this password or disable seeding before deploying to the internet or shared environments. Do not rely on default credentials.

### Roles (summary)

- **Anonymous guests**: list and filter scrolls, **preview**; **cannot** upload or **download**.
- **Signed-in USER / ADMIN**: upload, download, edit/delete **own** scrolls; update profile and password in the library UI.
- **ADMIN**: additionally **Admin** tab — user list, create user, delete users (not self), scroll statistics.

---

## 5. Other notes

- **Schema**: JPA `ddl-auto: update` creates/updates tables on startup (fine for coursework/dev; use Flyway/Liquibase in production).
- **Upload limits**: see `spring.servlet.multipart` in `application.yml` (defaults ~32MB file / 36MB request).
- **Legacy data**: startup includes a fix for old role values in the database; usually no manual step is needed.

---

## 6. Testing & coverage

The project includes unit and integration tests (JUnit 5, Spring Boot Test, Mockito; in-memory **H2** for tests). **JaCoCo** is configured on `mvn verify`.

With the default JaCoCo scope (DTOs, JPA entities, and the Spring Boot main class excluded from the gate), **statement coverage (JaCoCo line coverage) is above 90%**, and **branch coverage is above 70%**.

```bash
mvn test              # run tests
mvn verify            # tests + coverage report + threshold check
```

HTML report: `target/site/jacoco/index.html`.

---

## 7. Tech stack

Java 17 · Spring Boot 3 · Spring Security · JWT (JJWT) · Spring Data JPA · MySQL · Maven · vanilla HTML/CSS/JavaScript frontend

---

**Troubleshooting:** Confirm the database exists, credentials in `application-local.yml` are correct, `jwt.secret` is long enough, and port 8080 is free.
