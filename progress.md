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

**All 71 tests pass after this round.**

## 🔴 Critical
- ✅ **N1. No admin bootstrap.** Seed users now get roles (admin=`ROLE_ADMIN`, staff=`ROLE_MANAGER`); demo seeding restricted to the `dev` profile; added env-driven prod admin bootstrap (`APP_BOOTSTRAP_ADMIN_EMAIL`/`APP_BOOTSTRAP_ADMIN_PASSWORD`, policy-enforced). Demo passwords no longer seeded in prod.

## 🟠 High
- ✅ **N3. Logout / token revocation.** `POST /api/v1/auth/logout` revokes the caller's refresh token (audited `LOGOUT`).
- ✅ **N4. Account lockout.** 5 consecutive failures → 15-min lock; locked accounts rejected before password check; reset on success. New `failed_login_attempts`/`lock_until` columns (V7) wired through entity/mapper/domain; `isAccountNonLocked()` reflects lock. Tests added.
- ✅ **N5. `updateUser` email-conflict check** → returns 409 when changing to an email owned by another user.

## 🟡 Medium
- ✅ **N6. `@Async` enabled.** `AsyncConfig` (`@EnableAsync`) + bounded `auditExecutor` (CallerRuns back-pressure); audit methods use `@Async("auditExecutor")`.
- ✅ **N7. Audit IP/User-Agent** resolved from the request context on the calling thread before the async hop.
- ✅ **N8. Refresh tokens SHA-256** (constant-time compare) via `TokenHasher`, replacing bcrypt (72-byte truncation).
- ⏭️ **N9. Single refresh-token column = one session/user.** Left as-is (intentional single-session); logout now makes revocation possible. Revisit with a `refresh_tokens` table if multi-device is needed.
- ✅ **N10. Pagination on `GET /users`** — `page`/`size` params (defaults 0/20, size clamped to [1,100]) through use-case + repository ports.

## 🟢 Low / Quality
- ✅ **N11.** Removed redundant manual `Flyway::migrate` from `BeanConfig` (committed with N1).
- ✅ **N12.** Dedicated `InvalidTokenException` (→ 401) replaces `message.contains("JWT")` routing.
- ✅ **N13.** Optimistic locking (`@Version`) on `UserEntity`, threaded through the domain model/mapper; `ObjectOptimisticLockingFailureException` → 409 (V8 migration).
- ✅ **N14.** GitHub Actions CI (`.github/workflows/ci.yml`) runs the test suite on push/PR (JDK 21).
- ✅ **N15.** `AuthController.register` fetches the seeded `ROLE_USER` via `RoleUseCase` instead of a detached literal.

## Work Log (round 2)
- 2026-06-21 — Implemented N1, N3–N8, N10–N15 on branch `security-review-round2`; N9 deferred with rationale. Threaded lockout + optimistic-version state through the domain; added lockout & SHA-256 tests; updated JWT-exception tests. Full suite 71/71 green.
