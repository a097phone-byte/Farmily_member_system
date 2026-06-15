# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Language

所有回應一律使用繁體中文。

## Build & Run Commands

```bash
# Build
mvnw.cmd clean install

# Run
mvnw.cmd spring-boot:run

# Test (all)
mvnw.cmd test

# Test (single class)
mvnw.cmd test -Dtest=UserApplicationTests

# Package
mvnw.cmd package
```

Requires Java 17 and a running MySQL instance (see configuration below).

## Database Setup

MySQL on `localhost:3306`, database `myproject`, credentials `root`/`123456`. DDL is in `src/main/resources/create_table.txt`. JPA is set to `validate` mode — the schema must exist before running.

## Architecture

Spring Boot 3.x REST API for the Farmily platform's user management. Three distinct principal types (Member, Farmer, Admin) each have their own entity, UserDetails implementation, UserDetailsService, and security filter chain.

### Security: Four Independent Filter Chains

`UserSecurityConfig` defines four `SecurityFilterChain` beans ordered 1–4:
1. `/api/admin/**` → requires `ROLE_ADMIN`
2. `/api/farmer/**` → requires `ROLE_FARMER`
3. `/api/member/**` → requires `ROLE_USER`
4. Default catch-all → requires authentication

Authentication is session-based (`SessionCreationPolicy.ALWAYS`). CSRF is disabled. Passwords are BCrypt-encoded.

### Layered Structure

```
Controller → Service (interface + impl) → Repository (JpaRepository) → MySQL
```

DTOs handle all request/response shapes; entities are never exposed directly.

### Key Domain Concepts

**Farmer Review Workflow**: When a farmer registers, a `FarmerReview` snapshot record is created (round 1) with cert files (`LONGBLOB`). On resubmit (`PUT /api/farmer/me/application`), a new `FarmerReview` row is added (incremented round), preserving full revision history. The farmer stays `PENDING` until an admin approves.

**Cross-table Email Uniqueness**: `EmailUniquenessChecker` validates that an email is not already used across User, Farmer, or Admin tables before any registration.

**AuthProvider**: `User` entities track `LOCAL` vs `GOOGLE`. OAuth 2.0 infrastructure (dependency, `OAuthUserInfo` DTO) exists but Google login endpoints are not yet implemented.

### Incomplete Areas

The following controllers exist but contain no implementation:
- `AuthController`, `AdminController`, `AdminUserController`, `AdminFarmerController`, `AdminReviewController`

Admin review (approve/reject farmer applications) is the primary unimplemented feature. OAuth login is also unimplemented.

## API Surface

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/member/register` | Member registration |
| POST | `/api/member/login` | Member login (starts session) |
| GET | `/api/member/me` | Own profile |
| PUT | `/api/member/me` | Update profile |
| PUT | `/api/member/me/password` | Change password |
| DELETE | `/api/member/me` | Delete account |
| POST | `/api/farmer/register` | Farmer application (creates PENDING review) |
| POST | `/api/farmer/login` | Farmer login (ACTIVE only) |
| GET | `/api/farmer/me` | Farm profile + latest review status |
| PUT | `/api/farmer/me` | Update contact info |
| PUT | `/api/farmer/me/application` | Resubmit farmer application |
| PUT | `/api/farmer/me/password` | Change password |

## Exception Handling

`GlobalExceptionHandler` maps exceptions to HTTP status codes:
- `BadCredentialsException` → 401
- `IllegalStateException` → 409 (conflict: duplicate email, suspended account)
- `IllegalArgumentException` → 404
- `MethodArgumentNotValidException` → 500 (validation errors — note: unusual mapping)