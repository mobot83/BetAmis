#!/usr/bin/env bash
# =============================================================
# BetAmis — Local Kubernetes setup with kind
# =============================================================
# Usage:
#   ./scripts/setup-local-k8s.sh [--skip-build] [--skip-cluster]
#
# Options:
#   --skip-build    Skip Maven build and Docker image creation
#   --skip-cluster  Skip kind cluster creation (use existing cluster)
#
# Prerequisites: kind, kubectl, docker, mvn (or ./mvnw)
# =============================================================

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CLUSTER_NAME="betamis"
SKIP_BUILD=false
SKIP_CLUSTER=false

# ── Parse arguments ────────────────────────────────────────────────────────────
for arg in "$@"; do
  case $arg in
    --skip-build)   SKIP_BUILD=true ;;
    --skip-cluster) SKIP_CLUSTER=true ;;
    *) echo "Unknown argument: $arg" && exit 1 ;;
  esac
done

# ── Colour helpers ─────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
info()    { echo -e "${BLUE}[INFO]${NC}  $*"; }
success() { echo -e "${GREEN}[OK]${NC}    $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error()   { echo -e "${RED}[ERROR]${NC} $*" >&2; exit 1; }

# ── Prerequisite check ─────────────────────────────────────────────────────────
check_prereqs() {
  info "Checking prerequisites..."
  local missing=()
  for cmd in kind kubectl docker; do
    command -v "$cmd" &>/dev/null || missing+=("$cmd")
  done
  if [[ ${#missing[@]} -gt 0 ]]; then
    error "Missing required tools: ${missing[*]}"
  fi

  # Resolve Maven wrapper or system mvn
  if [[ -f "$ROOT_DIR/mvnw" ]]; then
    MVN="$ROOT_DIR/mvnw"
  elif command -v mvn &>/dev/null; then
    MVN="mvn"
  elif [[ "$SKIP_BUILD" == "true" ]]; then
    MVN=""
  else
    error "Neither ./mvnw nor mvn found. Install Maven or use --skip-build."
  fi
  success "All prerequisites satisfied."
}

# ── kind cluster ───────────────────────────────────────────────────────────────
create_cluster() {
  if [[ "$SKIP_CLUSTER" == "true" ]]; then
    info "Skipping cluster creation (--skip-cluster)."
    return
  fi

  if kind get clusters 2>/dev/null | grep -q "^${CLUSTER_NAME}$"; then
    warn "kind cluster '${CLUSTER_NAME}' already exists — skipping creation."
  else
    info "Creating kind cluster '${CLUSTER_NAME}'..."
    kind create cluster --config "$ROOT_DIR/k8s/kind-config.yaml"
    success "Cluster created."
  fi

  kubectl cluster-info --context "kind-${CLUSTER_NAME}" &>/dev/null \
    || error "Cannot reach cluster kind-${CLUSTER_NAME}. Check your kubeconfig."
}

# ── Build & load images ────────────────────────────────────────────────────────
SERVICES=(league-service prediction-service scoring-service match-service)

build_and_load() {
  if [[ "$SKIP_BUILD" == "true" ]]; then
    info "Skipping build (--skip-build). Loading existing local images into kind..."
    for svc in "${SERVICES[@]}"; do
      info "Loading betamis/${svc}:dev into kind..."
      kind load docker-image "betamis/${svc}:dev" --name "$CLUSTER_NAME"
    done
    return
  fi

  for svc in "${SERVICES[@]}"; do
    info "Building ${svc}..."
    (
      cd "$ROOT_DIR/${svc}"
      $MVN package -DskipTests -q
      docker build -f src/main/docker/Dockerfile.jvm \
        -t "betamis/${svc}:dev" .
    )
    info "Loading betamis/${svc}:dev into kind..."
    kind load docker-image "betamis/${svc}:dev" --name "$CLUSTER_NAME"
    success "${svc} built and loaded."
  done
}

# ── Apply manifests ────────────────────────────────────────────────────────────
apply_manifests() {
  info "Creating namespaces..."
  kubectl apply -f "$ROOT_DIR/k8s/namespace.yaml"

  info "Deploying infrastructure (postgres, zookeeper, kafka, schema-registry, redis, keycloak)..."
  kubectl apply -f "$ROOT_DIR/k8s/infra/postgres.yaml"
  kubectl apply -f "$ROOT_DIR/k8s/infra/zookeeper.yaml"
  kubectl apply -f "$ROOT_DIR/k8s/infra/kafka.yaml"
  kubectl apply -f "$ROOT_DIR/k8s/infra/schema-registry.yaml"
  kubectl apply -f "$ROOT_DIR/k8s/infra/redis.yaml"
  kubectl apply -f "$ROOT_DIR/k8s/infra/keycloak.yaml"

  info "Waiting for infrastructure to be ready (up to 3 minutes)..."
  kubectl rollout status deployment/postgres        -n betamis-infra --timeout=180s
  kubectl rollout status deployment/zookeeper       -n betamis-infra --timeout=180s
  kubectl rollout status deployment/kafka           -n betamis-infra --timeout=180s
  kubectl rollout status deployment/schema-registry -n betamis-infra --timeout=180s
  kubectl rollout status deployment/redis           -n betamis-infra --timeout=180s
  kubectl rollout status deployment/keycloak        -n betamis-infra --timeout=180s
  success "Infrastructure ready."

  info "Deploying application services..."
  kubectl apply -f "$ROOT_DIR/k8s/services/secret.yaml"
  kubectl apply -f "$ROOT_DIR/k8s/services/league-service.yaml"
  kubectl apply -f "$ROOT_DIR/k8s/services/prediction-service.yaml"
  kubectl apply -f "$ROOT_DIR/k8s/services/scoring-service.yaml"
  kubectl apply -f "$ROOT_DIR/k8s/services/match-service.yaml"

  info "Waiting for application services to be ready (up to 3 minutes)..."
  kubectl rollout status deployment/league-service     -n betamis-dev --timeout=180s
  kubectl rollout status deployment/prediction-service -n betamis-dev --timeout=180s
  kubectl rollout status deployment/scoring-service    -n betamis-dev --timeout=180s
  kubectl rollout status deployment/match-service      -n betamis-dev --timeout=180s
  success "All services ready."
}

# ── Port-forward instructions ──────────────────────────────────────────────────
print_access_info() {
  echo ""
  echo -e "${GREEN}============================================================${NC}"
  echo -e "${GREEN} BetAmis is running on kind cluster '${CLUSTER_NAME}'${NC}"
  echo -e "${GREEN}============================================================${NC}"
  echo ""
  echo "Services use ClusterIP. Use kubectl port-forward to access them:"
  echo ""
  echo "  # Keycloak"
  echo "  kubectl port-forward -n betamis-infra svc/keycloak 8180:8080"
  echo ""
  echo "  # league-service"
  echo "  kubectl port-forward -n betamis-dev svc/league-service 8080:8080"
  echo ""
  echo "  # prediction-service"
  echo "  kubectl port-forward -n betamis-dev svc/prediction-service 8082:8082"
  echo ""
  echo "  # scoring-service"
  echo "  kubectl port-forward -n betamis-dev svc/scoring-service 8083:8080"
  echo ""
  echo "  # match-service"
  echo "  kubectl port-forward -n betamis-dev svc/match-service 8084:8080"
  echo ""
  echo "Useful commands:"
  echo "  kubectl get pods -n betamis-infra"
  echo "  kubectl get pods -n betamis-dev"
  echo "  kind delete cluster --name ${CLUSTER_NAME}   # tear down"
  echo ""
  warn "Remember to set FOOTBALL_DATA_API_TOKEN in k8s/services/secret.yaml before deploying match-service."
}

# ── Main ───────────────────────────────────────────────────────────────────────
main() {
  check_prereqs
  create_cluster
  build_and_load
  apply_manifests
  print_access_info
}

main "$@"
