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

## ⬜ Step 2 — Supply chain & container

Once Step 1 is green:
- ⬜ **SBOM** — add the CycloneDX Gradle plugin; publish `build/reports/bom.json` as a
  build artifact and attach it to the image, so CVE exposure is answerable instantly.
- ⬜ **Image scan** — Trivy on the built Docker image (`HIGH,CRITICAL`), upload SARIF.
- ⬜ **Dockerfile hardening** — non-root user, pinned distroless/jammy base image,
  no secrets baked into layers, healthcheck.

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
