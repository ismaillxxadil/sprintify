# Security, JWT, and Exception Handling - Complete Explanation

This document explains the actual implemented code flow in detail, not just the concept.
It covers:

- How JWT tokens are created during signup and login.
- How requests are authenticated and authorized in Spring Security.
- How exceptions are turned into HTTP error responses.
- Which file is responsible for each behavior.

---

## 1) Security-Related Files and Their Roles

### Core security wiring

- [src/main/java/com/sprintify/sprintify/config/SecurityConfig.java](src/main/java/com/sprintify/sprintify/config/SecurityConfig.java)
  - Builds the Spring Security filter chain.
  - Defines route authorization rules.
  - Registers custom 401 and 403 handlers.
  - Registers the JWT filter.
  - Exposes the BCrypt password encoder bean.

- [src/main/java/com/sprintify/sprintify/security/JwtAuthenticationFilter.java](src/main/java/com/sprintify/sprintify/security/JwtAuthenticationFilter.java)
  - Reads the Authorization header.
  - Validates Bearer token.
  - Loads user by email from database.
  - Creates Authentication and stores it in SecurityContext.

- [src/main/java/com/sprintify/sprintify/security/JwtService.java](src/main/java/com/sprintify/sprintify/security/JwtService.java)
  - Generates JWT tokens.
  - Extracts email and role claims.
  - Validates signature and expiration.

### Auth API entry points

- [src/main/java/com/sprintify/sprintify/controller/AuthController.java](src/main/java/com/sprintify/sprintify/controller/AuthController.java)
  - Public endpoints:
    - POST /api/v1/auth/signup
    - POST /api/v1/auth/login

- [src/main/java/com/sprintify/sprintify/service/AuthService.java](src/main/java/com/sprintify/sprintify/service/AuthService.java)
  - Signup logic (duplicate email check, password hashing, save user, issue token).
  - Login logic (find user, verify password, issue token).

### Protected resources

- [src/main/java/com/sprintify/sprintify/controller/UserController.java](src/main/java/com/sprintify/sprintify/controller/UserController.java)
  - Authenticated endpoint: GET /api/v1/users/profile

- [src/main/java/com/sprintify/sprintify/service/UserService.java](src/main/java/com/sprintify/sprintify/service/UserService.java)
  - Loads profile by authenticated email.

- [src/main/java/com/sprintify/sprintify/controller/AdminController.java](src/main/java/com/sprintify/sprintify/controller/AdminController.java)
  - Admin-only endpoints under /api/v1/admin/**

- [src/main/java/com/sprintify/sprintify/service/AdminService.java](src/main/java/com/sprintify/sprintify/service/AdminService.java)
  - Admin business operations.

### Exception handling and error models

- [src/main/java/com/sprintify/sprintify/exception/GlobalExceptionHandler.java](src/main/java/com/sprintify/sprintify/exception/GlobalExceptionHandler.java)
  - Converts application exceptions to structured JSON responses.

- [src/main/java/com/sprintify/sprintify/dto/ApiErrorResponse.java](src/main/java/com/sprintify/sprintify/dto/ApiErrorResponse.java)
  - Standard error payload shape:
    - status
    - error
    - message

### User model + persistence involved in auth

- [src/main/java/com/sprintify/sprintify/entity/User.java](src/main/java/com/sprintify/sprintify/entity/User.java)
  - Stores email, hashed password, role, createdAt.

- [src/main/java/com/sprintify/sprintify/entity/Role.java](src/main/java/com/sprintify/sprintify/entity/Role.java)
  - USER / ADMIN enum.

- [src/main/java/com/sprintify/sprintify/repository/UserRepository.java](src/main/java/com/sprintify/sprintify/repository/UserRepository.java)
  - findByEmail, existsByEmail and other JPA operations.

### Configuration values used by security

- [src/main/resources/application.properties](src/main/resources/application.properties)
  - security.jwt.secret
  - security.jwt.expiration-ms

- [src/main/java/com/sprintify/sprintify/service/DataInitializer.java](src/main/java/com/sprintify/sprintify/service/DataInitializer.java)
  - Seeds an ADMIN user on startup (admin@sprintify.com / admin123, hashed).

---

## 2) SecurityConfig - How the app security is built

In [src/main/java/com/sprintify/sprintify/config/SecurityConfig.java](src/main/java/com/sprintify/sprintify/config/SecurityConfig.java), the method securityFilterChain configures everything:

1. CSRF disabled
   - csrf(AbstractHttpConfigurer::disable)
   - Appropriate for stateless JWT APIs.

2. Stateless session policy
   - sessionCreationPolicy(STATELESS)
   - Server does not keep login sessions.
   - Every request must carry its own JWT.

3. Authorization rules
   - /api/v1/auth/** -> permitAll
   - /api/v1/admin/** -> hasRole("ADMIN")
   - /api/v1/users/** -> authenticated
   - any other route -> authenticated

4. Security exception handlers
   - authenticationEntryPoint() returns JSON 401 when authentication is required but missing/invalid.
   - accessDeniedHandler() returns JSON 403 when user is authenticated but lacks role/authority.

5. Custom JWT filter registration
   - addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
   - JWT processing happens before username/password filter.

6. Password encoder bean
   - BCryptPasswordEncoder is used in signup/login and data initialization.

Important implementation detail:

- SecurityConfig writes JSON manually through writeError(...).
- That means security-layer 401/403 format can be produced directly by security handlers, not by controller advice.

---

## 3) JWT creation flow (signup and login)

### Signup flow

Path:

1. POST /api/v1/auth/signup reaches [src/main/java/com/sprintify/sprintify/controller/AuthController.java](src/main/java/com/sprintify/sprintify/controller/AuthController.java)
2. @Valid validates SignUpRequestDTO from [src/main/java/com/sprintify/sprintify/dto/SignUpRequestDTO.java](src/main/java/com/sprintify/sprintify/dto/SignUpRequestDTO.java)
3. Controller calls AuthService.signUp(...) in [src/main/java/com/sprintify/sprintify/service/AuthService.java](src/main/java/com/sprintify/sprintify/service/AuthService.java)
4. AuthService:
   - Normalizes email to lowercase + trim.
   - Checks existsByEmail.
   - Hashes password using BCrypt.
   - Saves user (role defaults to USER in User entity).
   - Generates token via JwtService.generateToken(email, role).
5. Returns AuthResponseDTO from [src/main/java/com/sprintify/sprintify/dto/AuthResponseDTO.java](src/main/java/com/sprintify/sprintify/dto/AuthResponseDTO.java) with token, email, role.

### Login flow

Path:

1. POST /api/v1/auth/login reaches AuthController.login(...)
2. @Valid validates LoginRequestDTO from [src/main/java/com/sprintify/sprintify/dto/LoginRequestDTO.java](src/main/java/com/sprintify/sprintify/dto/LoginRequestDTO.java)
3. AuthService.login(...):
   - Normalizes email.
   - findByEmail(email).
   - passwordEncoder.matches(raw, hashed).
   - If valid, generates new JWT.
4. Returns AuthResponseDTO.

### What JwtService puts inside token

In [src/main/java/com/sprintify/sprintify/security/JwtService.java](src/main/java/com/sprintify/sprintify/security/JwtService.java):

- subject = email
- claim role = USER or ADMIN
- iat = issued at current time
- exp = issuedAt + security.jwt.expiration-ms
- signature = HS256 with security.jwt.secret

Validation behavior:

- Secret shorter than 32 chars fails app startup with IllegalStateException.
- Parsing invalid/expired/tampered token throws runtime exception.

---

## 4) Request authentication flow for protected endpoints

When client calls a protected route (for example GET /api/v1/users/profile):

1. Request enters security filter chain.
2. JwtAuthenticationFilter executes once.
3. Filter reads Authorization header.
   - If missing or not starting with Bearer, it does not authenticate and passes request onward.
4. If Bearer token exists:
   - Extract token string.
   - Extract email from token.
   - Validate token (email match + not expired).
5. Load user by email from database.
   - If user missing or role null -> 401 from filter (sendError).
6. Build UsernamePasswordAuthenticationToken:
   - principal = user email
   - credentials = null
   - authorities = ROLE_ + role name
7. Put Authentication in SecurityContextHolder.
8. Continue filter chain.

After this point, authorization checks use the Authentication from SecurityContext.

---

## 5) Authorization flow (who can access what)

Rules in SecurityConfig map directly to roles/identity set by JwtAuthenticationFilter:

- /api/v1/auth/**
  - Public.
  - No token required.

- /api/v1/users/**
  - Any authenticated user.
  - USER and ADMIN both allowed.

- /api/v1/admin/**
  - Must have role ADMIN.
  - Internally Spring checks authority ROLE_ADMIN.

Because the filter creates authorities as ROLE_<ROLE_NAME>, the hasRole("ADMIN") matcher works correctly.

---

## 6) Exception handling flow - complete map

Your application has two exception handling layers:

1. Security layer handlers in SecurityConfig
2. MVC/controller layer handler in GlobalExceptionHandler

### A) Security layer errors (before or during authorization)

Source file:

- [src/main/java/com/sprintify/sprintify/config/SecurityConfig.java](src/main/java/com/sprintify/sprintify/config/SecurityConfig.java)

Cases:

- 401 Unauthorized
  - Trigger: unauthenticated access to authenticated endpoint.
  - Handler: authenticationEntryPoint.
  - JSON shape: { status, error, message } written by writeError.

- 403 Forbidden
  - Trigger: authenticated user without required role.
  - Handler: accessDeniedHandler.
  - JSON shape: { status, error, message } written by writeError.

### B) JWT filter direct errors

Source file:

- [src/main/java/com/sprintify/sprintify/security/JwtAuthenticationFilter.java](src/main/java/com/sprintify/sprintify/security/JwtAuthenticationFilter.java)

Cases:

- Invalid token / expired token / malformed token
  - response.sendError(401, "Invalid or expired token")

- Token email not found in DB
  - response.sendError(401, "User not found")

Important behavior:

- These are sent directly from the filter, so they do not pass through GlobalExceptionHandler.

### C) Controller/service exceptions

Source file:

- [src/main/java/com/sprintify/sprintify/exception/GlobalExceptionHandler.java](src/main/java/com/sprintify/sprintify/exception/GlobalExceptionHandler.java)

Handled exception types:

1. MethodArgumentNotValidException -> 400
   - Validation failures from @Valid request DTOs.
   - Message format: first field error only, for example email: Email must be valid.

2. ResponseStatusException -> dynamic status
   - Used heavily in services.
   - Examples:
     - 400 Email is already taken (signup)
     - 401 Invalid email or password (login)
     - 404 User not found (profile)

3. HttpMessageNotReadableException -> 400
   - Invalid JSON body.
   - Message: Invalid request body.

4. AuthenticationException -> 401
   - Fallback for authentication exceptions raised in MVC layer.

5. AccessDeniedException -> 403
   - Fallback for access-denied exceptions in MVC layer.

All these responses use ApiErrorResponse from [src/main/java/com/sprintify/sprintify/dto/ApiErrorResponse.java](src/main/java/com/sprintify/sprintify/dto/ApiErrorResponse.java).

---

## 7) End-to-end scenarios

### Scenario 1: Signup success

1. Client sends valid signup body.
2. Validation passes.
3. New user created with hashed password and default USER role.
4. JWT generated and returned.

### Scenario 2: Login wrong password

1. User exists but password mismatch.
2. AuthService throws ResponseStatusException(401, "Invalid email or password").
3. GlobalExceptionHandler returns ApiErrorResponse with 401.

### Scenario 3: Missing token on /api/v1/users/profile

1. JWT filter finds no Bearer header and continues.
2. Endpoint requires authenticated user.
3. Security entry point returns 401 JSON from SecurityConfig.

### Scenario 4: USER token on admin endpoint

1. JWT filter authenticates user and sets ROLE_USER.
2. Endpoint requires hasRole("ADMIN").
3. Access denied handler returns 403 JSON from SecurityConfig.

### Scenario 5: Expired token

1. JWT parsing/validation fails in JwtAuthenticationFilter/JwtService.
2. Filter sends 401 Invalid or expired token.

---

## 8) Data and DTO safety in current design

- Entity password is always stored hashed (BCrypt).
- Profile responses use UserProfileResponseDTO from [src/main/java/com/sprintify/sprintify/dto/UserProfileResponseDTO.java](src/main/java/com/sprintify/sprintify/dto/UserProfileResponseDTO.java), so password is never returned.
- Auth responses return token + identity role data only.

---

## 9) Quick architecture summary

- Token issuance:
  - AuthController -> AuthService -> JwtService

- Token verification per request:
  - JwtAuthenticationFilter -> JwtService + UserRepository -> SecurityContext

- Access control:
  - SecurityConfig requestMatchers + role checks

- Error shaping:
  - SecurityConfig handles auth/authorization failures in filter-security layer
  - GlobalExceptionHandler handles MVC/controller/service exceptions

This separation is why you may see some 401/403 responses generated by security handlers and other errors generated by GlobalExceptionHandler.
