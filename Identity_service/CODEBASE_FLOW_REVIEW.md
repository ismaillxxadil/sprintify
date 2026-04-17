# Sprintify Codebase Flow and Review

Note: this review was written before the security and controller fixes were applied. For the current change set and what was implemented, see [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md).

## 1. What this project is

This is a Spring Boot user/authentication service with:

- Signup and login using email/password
- Password hashing with BCrypt
- JWT token generation using JJWT
- PostgreSQL persistence with Spring Data JPA
- A simple admin controller for listing/deleting users

Main package root:

- `com.sprintify.sprintify`

Main app entry:

- `SprintifyApplication`

---

## 2. High-level architecture

The code follows a standard layered structure:

1. Controller layer

- `AuthController`: handles `/api/v1/auth/signup` and `/api/v1/auth/login`
- `AdminController`: handles `/api/v1/admin/users` endpoints

2. Service layer

- `AuthService`: signup/login business logic (normalize email, check duplicates, verify password, issue token)
- `DataInitializer`: creates default admin account on startup

3. Repository layer

- `UserRepository`: DB access for `User` entity

4. Security layer

- `SecurityConfig`: security rules and password encoder bean
- `JwtService`: token creation and parsing utilities

5. Exception layer

- `GlobalExceptionHandler`: transforms validation and `ResponseStatusException` into consistent JSON error responses

6. Data layer

- `User` entity mapped to `users` table
- `Role` enum (`USER`, `ADMIN`)

---

## 3. Runtime startup flow

When the app starts:

1. Spring Boot initializes beans and auto-configures MVC, JPA, Security.
2. `SecurityConfig` creates:

- `SecurityFilterChain`
- `PasswordEncoder` (`BCryptPasswordEncoder`)

3. `JwtService` reads `security.jwt.secret` and `security.jwt.expiration-ms` from properties.
4. JPA creates schema because:

- `spring.jpa.hibernate.ddl-auto=create`

5. `DataInitializer` runs `CommandLineRunner`:

- Checks whether `admin@sprintify.com` exists
- If missing, inserts an ADMIN user with password `admin123` (hashed)

Important side effect:

- Because ddl-auto is `create`, data is recreated on startup (destructive for persistent environments).

---

## 4. Database and entity model flow

### User entity

`User` fields:

- `id` (`Long`, identity)
- `email` (unique, not null)
- `password` (hashed, not null)
- `role` (`Role`, defaults to `USER`)
- `createdAt` set automatically via `@PrePersist`

### Repository usage

`UserRepository` methods used in flow:

- `existsByEmail(email)` during signup duplicate check
- `findByEmail(email)` during login and admin bootstrapping
- `findAll()` in admin dashboard endpoint
- `existsById(id)` and `deleteById(id)` in admin delete endpoint

---

## 5. Detailed HTTP flow

## 5.1 Signup flow (`POST /api/v1/auth/signup`)

1. Request arrives at `AuthController.signUp`.
2. `@Valid` validates `SignUpRequestDTO`:

- email required + valid format
- password required + 8..72 length

3. Controller calls `AuthService.signUp`.
4. Service normalizes email (`trim` + `lowercase`).
5. Service checks duplicate email with `existsByEmail`.
6. If duplicate: throws `ResponseStatusException(400, "Email is already taken")`.
7. Otherwise:

- Hash password using BCrypt
- Build new `User` (role defaults to `USER`)
- Save user

8. Service generates JWT with:

- subject = user email
- role claim = USER
- iat and exp

9. Returns `AuthResponseDTO(token, email, role)` with HTTP 201.

## 5.2 Login flow (`POST /api/v1/auth/login`)

1. Request arrives at `AuthController.login`.
2. `@Valid` validates `LoginRequestDTO`.
3. Controller calls `AuthService.login`.
4. Service normalizes email.
5. Service loads user via `findByEmail`.
6. If missing: throws 401 "Invalid email or password".
7. Service verifies password with `passwordEncoder.matches(raw, hashed)`.
8. If mismatch: throws same 401.
9. If valid: generates JWT with current role claim.
10. Returns `AuthResponseDTO` with HTTP 200.

## 5.3 Admin flow (`/api/v1/admin/...`)

### Get users (`GET /api/v1/admin/users`)

- Returns `List<User>` directly from `userRepository.findAll()`.

### Delete user (`DELETE /api/v1/admin/users/{id}`)

- Checks `existsById(id)`.
- If not found: returns 404 + message.
- If found: deletes row and returns success message.

---

## 6. Security flow (what actually happens)

Configured rules in `SecurityConfig`:

- `/api/v1/auth/**` is `permitAll`
- `/api/v1/admin/**` is also `permitAll`
- all other routes require authentication

Current JWT behavior:

- `JwtService` can create and parse tokens.
- But there is no authentication filter that reads `Authorization: Bearer <token>` and sets `SecurityContext`.

Practical result:

- Auth endpoints are open (expected).
- Admin endpoints are also open to everyone (critical issue).
- For other endpoints, JWT is not actively integrated into request authentication pipeline.

---

## 7. Validation and exception flow

### Validation errors

If DTO validation fails:

- Spring throws `MethodArgumentNotValidException`.
- `GlobalExceptionHandler` converts to:
- status = 400
- error = Bad Request
- message = Validation failed
- details = list like `field: error message`

### Business/auth errors

`AuthService` throws `ResponseStatusException` for:

- Duplicate email (400)
- Invalid credentials (401)

`GlobalExceptionHandler` returns a structured `ApiErrorResponse`.

---

## 8. Config and environment behavior

From `application.properties`:

- DB URL points to local Postgres on port 5332
- Default username/password are hardcoded
- JWT secret is hardcoded in source-controlled config
- SQL logging enabled

From `docker-compose.yml`:

- Spins up Postgres 16 with matching DB credentials
- Uses named volume for data

---

## 9. Bugs and risks found

Ordered by severity.

## 9.1 Critical: Admin endpoints are publicly accessible

Where:

- `SecurityConfig`: `.requestMatchers("/api/v1/admin/**").permitAll()`

Impact:

- Any unauthenticated caller can list and delete users.
- Full administrative data/control exposure.

Fix:

- Require role-based access (`hasRole("ADMIN")` or authority-based rule).
- Add JWT authentication filter (or configure resource server) so token is validated and roles are enforced.

## 9.2 Critical: Password hash leakage in admin list response

Where:

- `AdminController.getAllUsers()` returns `List<User>`.

Impact:

- API response includes `password` field from entity.
- Even hashed passwords should never be exposed in API responses.

Fix:

- Return dedicated safe DTO (id, email, role, createdAt only).
- Never serialize `password` in outward responses.

## 9.3 High: JWT generation exists but request authentication pipeline is incomplete

Where:

- `JwtService` exists, but no JWT request filter/UserDetails integration is present.

Impact:

- Tokens are issued but not properly enforced for protected resources.
- Security posture is inconsistent and brittle.

Fix:

- Add `OncePerRequestFilter` to parse Bearer tokens.
- Validate token, load user, populate `SecurityContextHolder`.
- Enforce role authorization for admin routes.

## 9.4 High: Destructive schema strategy in properties

Where:

- `spring.jpa.hibernate.ddl-auto=create`

Impact:

- Data loss risk on restart/redeploy.
- Unsafe for shared/testing/production persistence.

Fix:

- Use `validate` or controlled migrations (`Flyway`/`Liquibase`) for non-local environments.

## 9.5 Medium: Hardcoded secrets and credentials in versioned config

Where:

- DB credentials in `application.properties`
- JWT secret in `application.properties`
- Default admin credentials in `DataInitializer`

Impact:

- Secret leakage and credential reuse risk.

Fix:

- Move all secrets to environment variables or secret manager.
- Use profile-based config (`application-dev.properties`, `application-prod.properties`).
- Stop printing credentials in logs.

## 9.6 Medium: API/docs mismatch for admin delete endpoint

Where:

- `AdminController` expects `DELETE /users/{id}` with `Long` path variable.
- `request.http` shows `DELETE /api/v1/admin/users/?USER_ID` and comments mention UUID.

Impact:

- Consumers will call wrong route and fail.

Fix:

- Align docs and `request.http` with actual path-style endpoint and ID type.

## 9.7 Medium: Weak automated test coverage

Where:

- Only context-load test exists.

Impact:

- Regressions in security and auth logic can go unnoticed.

Fix:

- Add unit tests for `AuthService`.
- Add integration tests for signup/login and admin authorization behavior.
- Add negative-path tests (invalid credentials, duplicate email, invalid payload).

---

## 10. Improvement roadmap (recommended order)

1. Lock down security immediately

- Protect `/api/v1/admin/**` with ADMIN role checks.
- Implement JWT authentication filter / resource server setup.

2. Prevent sensitive data exposure

- Replace entity responses with safe DTOs.

3. Stabilize data lifecycle

- Replace `ddl-auto=create` with migration-based strategy.

4. Externalize secrets and credentials

- Use env vars and profile configs.

5. Fix API contract/documentation mismatch

- Correct `request.http` and docs for delete route and ID type.

6. Add comprehensive tests

- Focus on auth/security regressions first.

---

## 11. End-to-end flow summary

Current effective flow:

1. Client signs up/logs in -> receives JWT.
2. JWT can be parsed by utility class, but request pipeline does not enforce it for admin.
3. Admin endpoints are currently open and expose user records, including password hashes.
4. Global exception handling provides consistent error JSON for validation and service-level HTTP exceptions.

In short: the app has a good foundational structure (layering, DTO validation, centralized errors, hashing, token issuance), but security enforcement and response-shaping need immediate hardening before production use.
