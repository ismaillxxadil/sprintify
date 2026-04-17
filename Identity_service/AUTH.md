# Authentication Module Documentation

## Overview

This service implements stateless authentication using Spring Security, BCrypt, and JWT.

Public auth endpoints:

- POST /api/v1/auth/signup
- POST /api/v1/auth/login

All other endpoints require authentication by security policy.

## Architecture and Responsibilities

- Controller layer: accepts HTTP requests and returns HTTP responses.
- Service layer: contains auth business logic (validation checks, hashing, user lookup, token creation).
- Repository layer: persists and queries User entities.
- Security configuration: defines route access rules and password encoder bean.
- JWT service: creates and validates JWT tokens.

Current key classes:

- src/main/java/com/sprintify/sprintify/controller/AuthController.java
- src/main/java/com/sprintify/sprintify/service/AuthService.java
- src/main/java/com/sprintify/sprintify/repository/UserRepository.java
- src/main/java/com/sprintify/sprintify/security/JwtService.java
- src/main/java/com/sprintify/sprintify/config/SecurityConfig.java
- src/main/java/com/sprintify/sprintify/dto/SignUpRequestDTO.java
- src/main/java/com/sprintify/sprintify/dto/LoginRequestDTO.java
- src/main/java/com/sprintify/sprintify/dto/AuthResponseDTO.java

## Data Model

User entity fields used by auth flow:

- id: UUID primary key
- email: unique, required
- password: stored as BCrypt hash
- role: USER or ADMIN

## Request and Response DTOs

SignUpRequestDTO:

- email: @NotBlank + @Email
- password: @NotBlank + @Size(min = 8, max = 72)

LoginRequestDTO:

- email: @NotBlank + @Email
- password: @NotBlank

AuthResponseDTO:

- token
- email
- role

ApiErrorResponse:

- status
- error
- message
- details

## Signup Flow (POST /api/v1/auth/signup)

1. Request body is validated with @Valid.
2. Service normalizes email: trim + lowercase.
3. Service checks userRepository.existsByEmail(email).
4. If already present, returns HTTP 400 (Email is already taken).
5. Password is hashed with BCryptPasswordEncoder.
6. New user is saved.
7. JWT token is generated with subject=email, role claim, and expiration timestamp.
8. Response returns HTTP 201 with AuthResponseDTO.

## Login Flow (POST /api/v1/auth/login)

1. Request body is validated with @Valid.
2. Service normalizes email: trim + lowercase.
3. Service loads user with userRepository.findByEmail(email).
4. If user not found, returns HTTP 401 (Invalid email or password).
5. BCryptPasswordEncoder.matches(raw, hashed) is used for password verification.
6. If mismatch, returns HTTP 401.
7. JWT token is generated with the user's role claim.
8. Response returns HTTP 200 with AuthResponseDTO.

## JWT Details

Token generation uses JJWT and HS256.
Claims used:

- sub (subject): user email
- role: user role string such as USER or ADMIN
- iat (issued at)
- exp (expiration)

Configuration keys in application.properties:

- security.jwt.secret: signing secret, minimum 32 chars required
- security.jwt.expiration-ms: token lifetime in milliseconds

Default configured values:

- expiration: 86400000 (24 hours)

Important:

- Replace security.jwt.secret in non-dev environments.
- Do not commit real production secrets.
- Rotate secrets if exposed.

## Security Rules

SecurityFilterChain behavior:

- /api/v1/auth/\*\* is permitAll
- anyRequest().authenticated() for all other routes
- stateless session management
- CSRF disabled for API usage
- form login and HTTP basic disabled

This design is consistent with token-based APIs.

## Validation and Error Behavior

Validation failures:

- Triggered automatically by @Valid on controller method parameters.
- Returned as HTTP 400 with a structured ApiErrorResponse.

Business/auth failures:

- Email already taken -> HTTP 400
- Invalid login credentials -> HTTP 401

Exceptions are handled centrally by GlobalExceptionHandler so the response body includes the actual error message.

## Dependencies Used

Required Maven dependencies:

- spring-boot-starter-security
- spring-boot-starter-validation
- io.jsonwebtoken:jjwt-api:0.11.5
- io.jsonwebtoken:jjwt-impl:0.11.5 (runtime)
- io.jsonwebtoken:jjwt-jackson:0.11.5 (runtime)

## Example Requests

Signup:
{
"email": "user@example.com",
"password": "StrongPass123"
}

Login:
{
"email": "user@example.com",
"password": "StrongPass123"
}

Success response:
{
"token": "<jwt>",
"email": "user@example.com",
"role": "USER"
}

## Notes for Next Step

Current project issues a valid JWT but does not yet install a JWT authentication filter to read Authorization: Bearer tokens on protected endpoints.

To fully enforce JWT authentication for non-auth routes, add:

- OncePerRequestFilter to parse and validate token
- SecurityContext population from token subject
- UserDetailsService integration or equivalent user lookup strategy
