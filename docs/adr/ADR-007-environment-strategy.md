# ADR-007 — Environment Management Strategy

**Date**: 2026-03-21
**Status**: Accepted

## Context

BetAmis needs multiple deployment environments to safely validate changes before they reach end users. The project uses a mono-repo with four services (league, prediction, match, scoring) and shared infrastructure (PostgreSQL, Kafka, Redis, Keycloak). Two concerns drive the environment design:

1. **Isolation**: changes in one environment must not affect another.
2. **Parity**: staging must be close enough to production that it catches real integration bugs.

## Decision

Three environments are defined, each with a distinct purpose and isolation boundary:

| Environment | Namespace(s)     | Branch    | Deployed by       | TLS                   |
|-------------|------------------|-----------|-------------------|-----------------------|
| dev         | `betamis-dev`    | any       | `helmfile -e dev` | mkcert (`betamis.local`) |
| staging     | `betamis-staging`| `staging` | ArgoCD (auto)     | mkcert (`staging.betamis.local`) |
| prod        | `betamis-prod`   | `main`    | ArgoCD (auto)     | cert-manager / ACME   |

### Isolation mechanism

Isolation is enforced at the **Kubernetes namespace** level within the same cluster for dev/staging (kind), and at the **cluster** level for production. Each environment has its own:

- Kubernetes namespace (`betamis-dev`, `betamis-staging`, `betamis-prod`)
- Helm values file (`values-dev.yaml`, `values-staging.yaml`, `values-prod.yaml`)
- Ingress hostname (`betamis.local`, `staging.betamis.local`, `api.betamis.example.com`)
- Image tag (local `dev` tag, GHCR SHA from `staging` branch, GHCR SHA from `main`)

Infrastructure (PostgreSQL, Kafka, Redis, Keycloak) is shared within a local kind cluster but is separate in production.

### GitOps flow

```
Developer → git push staging → CI tests + builds image → pushes to GHCR
         → updates values-staging.yaml [skip ci] → ArgoCD syncs betamis-staging

Developer → git push main    → CI tests + builds image → pushes to GHCR
         → updates values-prod.yaml [skip ci]    → ArgoCD syncs betamis-prod
```

### Local TLS

Local environments (`dev`, `staging`) use [mkcert](https://github.com/FiloSottile/mkcert) to generate a locally-trusted certificate covering both `betamis.local` and `staging.betamis.local`. The setup script creates the `betamis-local-tls` Kubernetes TLS Secret in both namespaces. Production uses cert-manager with Let's Encrypt ACME.

Nginx Ingress routes `/api/*` paths to the appropriate backend, stripping the `/api` prefix via `nginx.ingress.kubernetes.io/rewrite-target`.

### Helmfile

`helmfile.yaml` is used for local iteration (dev and staging). It reads the target namespace from environment values:

```yaml
environments:
  dev:
    values:
      - appNamespace: betamis-dev
  staging:
    values:
      - appNamespace: betamis-staging
```

Production is managed exclusively by ArgoCD.

## Consequences

- A `staging` branch must exist in the repository. Changes intended for staging are pushed/merged there; changes for production go to `main`.
- The `setup-local-k8s.sh` script provisions both `betamis-dev` and `betamis-staging` namespaces and their TLS secrets in a single run.
- Adding a fourth environment would require a new values file per service, a new helmfile environment entry, and a new set of ArgoCD Application manifests.
- Shared infrastructure in local kind means a catastrophic infra failure affects both dev and staging simultaneously — acceptable for local development but not for a production staging cluster.
