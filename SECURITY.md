# Security Policy

CivicShield Labs projects are safety-sensitive systems.

## Core Rules

- Never commit credentials, tokens, private keys, production certificates, or personal data.
- Never build or deploy unauthorized access mechanisms.
- Never treat a license plate, VIN, device identifier, or other public identifier as an authorization credential.
- Consequential enforcement actions must require authenticated, authorized human approval.
- Maintain complete audit records for privileged actions.
- Use least privilege and short-lived credentials.
- Test safety-critical behavior in simulation before controlled hardware testing.
- Real-world integrations require appropriate authorization, legal review, and safety validation.

## Reporting

Security findings should be handled privately and responsibly.

## Current Deployment Gates

- The API uses one configured administrator account. `CIVICSHIELD_ADMIN_PASSWORD` is required and must be at least 32 characters; the application refuses the known placeholder. Keep the credential in a secret manager and require HTTPS at the trusted ingress before sending Basic authentication credentials.
- The current API uses one configured administrator account mapped to one `CIVICSHIELD_ADMIN_AGENCY_ID`. Create requests must name that agency, list queries are agency-filtered, and object/event reads use the authenticated agency in their repository lookup. Missing scope fails closed. This single-account mapping is not a verified multi-agency identity system: keep production multi-agency use blocked until distinct authenticated identities, agency membership lifecycle, and tenant-isolation behavior are integrated and independently verified.
- Audit-event actors are now derived from the authenticated principal. Because the current deployment has one shared administrator, this records the shared account rather than an individual human and is not sufficient for consequential actions.
- The application rejects request bodies larger than 64 KiB, and request DTOs also limit individual database-bound fields.
- Rate limiting, distributed identity management, security alerting, and a verified production restore process are not part of this service yet. Enforce abuse controls at the ingress and validate recovery before any production use.
- The Docker Compose database port is bound to loopback for local development. Keep its local-only credentials out of any network-facing deployment.
- Gradle CI uses the checked-in wrapper with a pinned distribution checksum, dependency locks, immutable action commits, weekly dependency/action update proposals, and a weekly OSV-Scanner check. The scanner fails on known dependency advisories; SARIF upload is disabled.
