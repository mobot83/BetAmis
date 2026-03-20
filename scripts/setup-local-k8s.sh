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

# ── metrics-server (required for HPA) ─────────────────────────────────────────
install_metrics_server() {
  if kubectl get deployment metrics-server -n kube-system &>/dev/null; then
    info "metrics-server already installed — skipping."
    return
  fi

  info "Installing metrics-server (required for HPA)..."
  kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
  # kind uses self-signed kubelet certs — patch to allow insecure TLS
  kubectl patch deployment metrics-server -n kube-system \
    --type=json \
    -p='[{"op":"add","path":"/spec/template/spec/containers/0/args/-","value":"--kubelet-insecure-tls"}]'
  success "metrics-server installed."
}

# ── Apply manifests ────────────────────────────────────────────────────────────
apply_manifests() {
  if ! command -v helmfile &>/dev/null; then
    error "helmfile not found. Install it: https://github.com/helmfile/helmfile/releases"
  fi

  info "Deploying full environment via helmfile..."
  helmfile sync -e dev
  success "All releases deployed."
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
  install_metrics_server
  build_and_load
  apply_manifests
  print_access_info
}

main "$@"
