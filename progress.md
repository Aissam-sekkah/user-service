# Security & Code Quality — Remediation Progress

Tracking the findings from the security/quality review of `user-service`.
Status legend: ⬜ TODO · 🔄 In progress · ✅ Done · ⏭️ Skipped

**All 66 tests pass after remediation.**

---

## 🔴 Critical

- ✅ **C1. Hardcoded JWT secret default** — default changed to empty (`${JWT_SECRET:}`) in `application.yaml`; startup now fails fast (JwtService validator) when unset. ⚠️ The previously-committed key must be rotated wherever it was deployed.
- ✅ **C2. Permissive CORS with credentials** — deleted `config/WebConfig.java`; only the env-driven `CorsConfig` remains.

## 🟠 High

- ✅ **H3. Committed DB password** — removed `secret` defaults; `application.yaml` uses `${DB_PASSWORD:}`, `docker-compose.yml` requires `DB_PASSWORD`/`JWT_SECRET` via `${VAR:?}`; `.env.example` documents them.
- ✅ **H4. Actuator health leaks details to anonymous** — base `application.yaml` now uses `show-details: when_authorized`.

## 🟡 Medium

- ✅ **M5. Missing authz on change-password (IDOR)** — `PUT /users/{id}/password` now `@PreAuthorize("hasRole('ADMIN') or @authz.isSelf(authentication, #id)")`.
- ✅ **M6. Rate limiter bypassable / not robust** — no longer reads client `X-Forwarded-For`/`X-Real-IP` (relies on container `getRemoteAddr()` + `forward-headers-strategy`); counter now `AtomicInteger`. (Still in-memory — documented; use Redis for multi-instance.)
- ✅ **M7. Broad user enumeration** — `getAll` now `hasRole('MANAGER')`; `getById` is `hasRole('MANAGER') or @authz.isSelf(...)`.
- ✅ **M8. bcrypt 72-byte truncation** — added max length 72 to `PasswordPolicy`, `CreateUserRequest`, `ChangePasswordRequest`.

## 🟢 Low / Quality

- ✅ **L9.** Removed unused `generateToken(username, roles)` overload (port + impl); migrated `generateRefreshToken` to the non-deprecated jjwt 0.12 API.
- ✅ **L10.** `AuthController` now depends on `TokenServicePort` instead of the `JwtService` adapter.
- ✅ **L11.** `GlobalExceptionHandler` null-safe via `String.valueOf(getMessage())`.
- ✅ **L12.** Tightened `EmailValidator` regex (requires TLD; rejects leading/trailing/consecutive dots).
- ✅ **L13.** `assignDirectRoles` now defensively copies and null-guards.
- ⏭️ **L14.** CSP `'unsafe-inline' 'unsafe-eval'` left as-is — required by the bundled Swagger UI. Documented; revisit if Swagger is removed/served separately.
- ✅ **L15.** `show-sql: false` in base `application.yaml` (dev profile still enables it).

---

## 🟡 Medium (found during verification)

- ✅ **M16. Authorization denials returned HTTP 500** — `@PreAuthorize` failures (`AuthorizationDeniedException`) had no handler and fell through to the generic 500 handler, leaking the wrong status. Added an `AccessDeniedException` handler in `GlobalExceptionHandler` → now returns **403**.

## New code introduced
- `adapter/in/web/security/AuthorizationService.java` — `@authz` bean providing null/type-safe `isSelf(authentication, id)` ownership checks for `@PreAuthorize`.

## Test changes
- `UserControllerTest`: adjusted `@WithMockUser` roles to match new authorization (MANAGER for read/list, ADMIN for password changes); updated two password-length assertion messages.
- `SecurityIntegrationTest`: listing-endpoint token user given `ROLE_MANAGER` (USER is now intentionally denied); added two ownership-path tests — a plain USER **can** change their own password (204) but **cannot** change another user's (403).
- Full suite now 68/68 green.

## Work Log

- 2026-06-08 — Review completed; tracker created.
- 2026-06-08 — Implemented C1–C2, H3–H4, M5–M8, L9–L15 (L14 skipped w/ rationale). Added `AuthorizationService`; updated tests. Full suite green (66/66).

## Follow-ups / out of scope
- Rotate the previously-committed JWT secret in any environment where it was used.
- Consider Redis-backed distributed rate limiting (currently per-instance IP throttle only).
- Consider refresh-token reuse detection (revoke session on replay of a rotated token).

---

# Second Review — Functional gaps & code quality (2026-06-21)

Findings from a follow-up architecture/code review. Resolved critical → low.
Status legend: ⬜ TODO · 🔄 In progress · ✅ Done · ⏭️ Skipped

## 🔴 Critical
- ⬜ **N1. No admin bootstrap.** Admin-only endpoints (`POST /users`, role mgmt, delete) require `ROLE_ADMIN`, but the seeded `admin@example.com`/`staff@example.com` get **no roles** and no migration assigns any → a fresh deploy has nobody who can administer the system. Also demo passwords (`admin12345`/`staff12345`) were seeded in **prod** (`@Profile("!test")`).
  - Fix: assign `ROLE_ADMIN`/`ROLE_MANAGER` to seed users; restrict demo seeding to `dev`; add env-driven prod admin bootstrap (`INITIAL_ADMIN_EMAIL`/`INITIAL_ADMIN_PASSWORD`).

## 🟠 High
- ⬜ **N3. No logout / token revocation.** `LOGOUT`/`ACCOUNT_LOCKED` audit types defined but unused; no way to invalidate a refresh token server-side. Add `POST /auth/logout`.
- ⬜ **N4. No account lockout.** Failed logins audited but never counted/locked; `isAccountNonLocked()` hardcoded `true`. Add attempt counter + temporary lock.
- ⬜ **N5. `updateUser` has no email-conflict check.** `createUser` guards `existByEmail`; `updateUser` does not → relies on raw DB constraint (500) when changing to an in-use email.

## 🟡 Medium
- ⬜ **N6. `@Async` no-op.** `AuditLogger` is `@Async` but no `@EnableAsync` → audit runs on the request thread. Enable async with a bounded executor.
- ⬜ **N7. Audit IP always "unknown".** Rich overload never called; resolve client IP from the request context.
- ⬜ **N8. Refresh tokens bcrypt-hashed → 72-byte truncation** on JWTs. Switch to SHA-256 for opaque/long tokens.
- ⏭️ **N9. Single refresh-token column = one session/user.** Left as-is (intentional single-session); logout now makes revocation possible. Revisit with a `refresh_tokens` table if multi-device is needed.
- ⬜ **N10. No pagination on `GET /users`.** Loads & serializes every user. Add page/size.

## 🟢 Low / Quality
- ⬜ **N11.** Redundant manual `Flyway::migrate` in `BeanConfig` while `spring.flyway.enabled=true` — remove.
- ⬜ **N12.** `GlobalExceptionHandler` routes JWT errors via `message.contains("JWT")` — use a dedicated exception type.
- ⬜ **N13.** No optimistic locking (`@Version`) — add to entities.
- ⬜ **N14.** No CI pipeline — add GitHub Actions to run the test suite.
- ⬜ **N15.** `AuthController.register` builds a detached `Role` literal — fetch the seeded role instead.
