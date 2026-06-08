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
- Consider Redis-backed distributed rate limiting + account lockout (currently per-instance IP throttle only).
- Consider refresh-token reuse detection (revoke session on replay of a rotated token).
