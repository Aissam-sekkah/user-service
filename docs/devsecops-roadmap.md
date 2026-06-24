# DevSecOps Roadmap — user-service

Security baked into every stage of the lifecycle, rolled out in priority order.
This file tracks **what is done** and **what to do next time**.

Legend: ✅ done · ⬜ todo · 🔧 manual (GitHub setting, not a committed file)

---

## ✅ Step 1 — Cheap, high-value (DONE)

The foundation: stop secrets leaking, scan our code and dependencies, fail PRs early.

- ✅ **Secret scanning (CI)** — `.github/workflows/security.yml` → `gitleaks` over full
  history (`fetch-depth: 0`). Directly addresses the previously-committed JWT/DB secrets.
- ✅ **Pre-commit hook** — `.pre-commit-config.yaml` runs gitleaks + hygiene checks
  locally so secrets never leave the machine. Enable with:
  ```bash
  pipx install pre-commit   # or: pip install pre-commit
  pre-commit install        # registers the git hook
  pre-commit run --all-files
  ```
- ✅ **SAST** — CodeQL (`java-kotlin`) in `security.yml`, on push/PR + weekly.
- ✅ **SCA** — `dependency-review-action` blocks PRs adding HIGH-severity vulnerable deps;
  Dependabot (`.github/dependabot.yml`) opens weekly update PRs for Gradle + Actions.
- 🔧 **Branch protection** — cannot be committed; set once in the GitHub UI (see below).
- 🔧 **Dependency Graph** — required by `dependency-review` and Dependabot alerts.
  Enable once: **Settings → Code security and analysis → Dependency graph → Enable**
  (free, incl. private repos). Until then the `dependency-review` job is marked
  `continue-on-error: true` so it can't fail the pipeline; remove that line once
  the graph is enabled so the job actually blocks vulnerable-dependency PRs.

### 🔧 Branch protection — manual setup (do this once)
GitHub → repo **Settings → Branches → Add rule** for `master`:
- ☑ Require a pull request before merging (≥1 approval)
- ☑ Require status checks to pass → select **CI / build**, **Security / codeql**,
  **Security / secret-scan**
- ☑ Require branches to be up to date before merging
- ☑ Do not allow bypassing the above
- (optional) ☑ Require signed commits

> Or, once the `gh` CLI is installed and authenticated:
> ```bash
> gh api -X PUT repos/Aissam-sekkah/user-service/branches/master/protection \
>   -F required_pull_request_reviews.required_approving_review_count=1 \
>   -F enforce_admins=true \
>   -F 'required_status_checks.strict=true' \
>   -F 'required_status_checks.contexts[]=build' \
>   -F restrictions=
> ```

> ⚠️ Note: CodeQL/Trivy results land in **Security → Code scanning**, which needs
> GitHub Advanced Security (free for public repos; paid add-on for private).
> Gitleaks and Dependabot work regardless.

---

## ✅ Step 2 — Supply chain & container (DONE)

- ✅ **SBOM** — CycloneDX Gradle plugin (`org.cyclonedx.bom` 2.3.1) →
  `build/reports/bom.json` (`./gradlew cyclonedxBom`); `security.yml` `sbom` job
  uploads it as a build artifact.
- ✅ **Image scan** — `security.yml` `image-scan` job builds the image and runs Trivy
  (`HIGH,CRITICAL`), uploads SARIF as an artifact + to code scanning (best-effort).
  Currently **report-only** (`exit-code: '0'`) — raise to `'1'` to enforce.
- ✅ **Dockerfile hardening** — `.dockerignore` keeps `.env`/`data/` out of the build
  context; jar copied as non-root (`--chown`); `HEALTHCHECK` on `/actuator/health`;
  container-aware heap (`MaxRAMPercentage`); layered copy for dependency caching;
  plain jar disabled so the `*.jar` glob is unambiguous.

### Tighten later (when ready)
- ⬜ Pin base images by **digest** (`@sha256:...`) instead of tags for reproducibility.
- ⬜ Flip Trivy `exit-code` to `'1'` (and/or `ignore-unfixed: true`) to block on CVEs.
- ⬜ Consider a **distroless** runtime base to shrink the attack surface further.

## ⬜ Step 3 — Secrets & provenance

- ⬜ **Secrets manager** — move `JWT_SECRET` / `DB_PASSWORD` out of `.env`/env vars into
  Vault or a cloud secrets manager (k8s: sealed-secrets / external-secrets).
- ⬜ **JWT key rotation** — mechanism to rotate the signing key (kid header + key set),
  and rotate the previously-leaked key in every environment.
- ⬜ **Image signing** — sign release images with `cosign`; verify signature at deploy.

## ⬜ Step 4 — Runtime & advanced

- ⬜ **DAST** — OWASP ZAP baseline scan against a spun-up instance, exercising
  login / refresh / RBAC endpoints (catches what SAST cannot).
- ⬜ **IaC / policy-as-code** — if k8s/Terraform is adopted: tfsec/Checkov + OPA/Kyverno.
- ⬜ **Runtime security & alerting** — wire the existing Prometheus/Grafana + audit logs
  to alerts (failed-login spikes, lockouts, 5xx), and ship audit events to a SIEM.

---

## Pipeline at a glance

| Stage    | Control                          | Workflow / file                     |
|----------|----------------------------------|-------------------------------------|
| Pre-commit | gitleaks + hygiene             | `.pre-commit-config.yaml`           |
| Build/Test | compile + 71 tests             | `.github/workflows/ci.yml`          |
| Secrets  | gitleaks (full history)          | `.github/workflows/security.yml`    |
| SAST     | CodeQL                           | `.github/workflows/security.yml`    |
| SCA      | dependency-review + Dependabot   | `security.yml` + `dependabot.yml`   |
| Package  | image scan / SBOM *(Step 2)*     | _todo_                              |
| Deploy   | secrets mgr / signing *(Step 3)* | _todo_                              |
| Runtime  | metrics + audit + alerts         | Prometheus/Grafana *(partial)*      |
