#!/usr/bin/env bash
# =============================================================
# BetAmis — Local Kubernetes setup with kind
# =============================================================
# Usage:
#   ./scripts/setup-local-k8s.sh [--skip-build] [--skip-cluster] [--skip-argocd]
#
# Options:
#   --skip-build    Skip Docker image build and kind load
#   --skip-cluster  Skip kind cluster creation (use existing cluster)
#   --skip-argocd   Skip ArgoCD installation and root-app bootstrap
#
# Prerequisites: kind, kubectl, docker
#
# ArgoCD is installed automatically and manages the betamis-prod namespace.
# Helmfile continues to manage betamis-dev for local iteration.
# =============================================================

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CLUSTER_NAME="betamis"
ARGOCD_VERSION="v2.13.3"
SKIP_BUILD=false
SKIP_CLUSTER=false
SKIP_ARGOCD=false

# ── Parse arguments ────────────────────────────────────────────────────────────
for arg in "$@"; do
  case $arg in
    --skip-build)    SKIP_BUILD=true ;;
    --skip-cluster)  SKIP_CLUSTER=true ;;
    --skip-argocd)   SKIP_ARGOCD=true ;;
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
    docker build -f "${svc}/Dockerfile" \
      -t "betamis/${svc}:dev" \
      "$ROOT_DIR"
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

# ── ArgoCD ─────────────────────────────────────────────────────────────────────
install_argocd() {
  if [[ "$SKIP_ARGOCD" == "true" ]]; then
    info "Skipping ArgoCD installation (--skip-argocd)."
    return
  fi

  if kubectl get deployment argocd-server -n argocd &>/dev/null; then
    info "ArgoCD already installed — skipping."
  else
    info "Installing ArgoCD ${ARGOCD_VERSION}..."
    kubectl create namespace argocd --dry-run=client -o yaml | kubectl apply -f -
    kubectl apply -n argocd \
      -f "https://raw.githubusercontent.com/argoproj/argo-cd/${ARGOCD_VERSION}/manifests/install.yaml"
    info "Waiting for ArgoCD server to be ready (up to 3 min)..."
    kubectl rollout status deployment argocd-server -n argocd --timeout=180s
    success "ArgoCD ${ARGOCD_VERSION} installed."
  fi

  info "Applying ArgoCD notifications config..."
  kubectl apply -f "$ROOT_DIR/argocd/notifications/argocd-notifications-cm.yaml"
  # Secret is a template — only apply if it hasn't been created yet so existing
  # credentials are not overwritten by the empty placeholder.
  if ! kubectl get secret argocd-notifications-secret -n argocd &>/dev/null; then
    kubectl apply -f "$ROOT_DIR/argocd/notifications/argocd-notifications-secret.yaml"
    warn "argocd-notifications-secret created with empty values. Populate Slack/email creds before notifications will fire."
  fi

  info "Applying ArgoCD root application (App of Apps)..."
  kubectl apply -f "$ROOT_DIR/argocd/root-app.yaml"
  success "Root application applied — ArgoCD will reconcile all child apps."
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
  echo "Services are exposed via Nginx Ingress on http://api.betamis.localdev"
  echo ""
  echo "Add to /etc/hosts (once, requires sudo):"
  echo "  echo '127.0.0.1  api.betamis.localdev' | sudo tee -a /etc/hosts"
  echo ""
  echo "Endpoints:"
  echo "  http://api.betamis.localdev/leagues"
  echo "  http://api.betamis.localdev/predictions"
  echo "  http://api.betamis.localdev/rankings"
  echo "  http://api.betamis.localdev/matches"
  echo ""
  echo "Keycloak (port-forward):"
  echo "  kubectl port-forward -n betamis-infra svc/keycloak 8180:8080"
  echo ""
  echo "Useful commands:"
  echo "  kubectl get pods -n betamis-infra"
  echo "  kubectl get pods -n betamis-dev"
  echo "  kubectl get pods -n betamis-prod"
  echo "  kubectl get applications -n argocd"
  echo "  kind delete cluster --name ${CLUSTER_NAME}   # tear down"
  echo ""
  echo "  # ArgoCD UI"
  echo "  kubectl port-forward -n argocd svc/argocd-server 8443:443"
  echo "  # Initial admin password:"
  echo "  kubectl get secret argocd-initial-admin-secret -n argocd -o jsonpath='{.data.password}' | base64 -d"
  echo ""
  warn "Remember to populate argocd-notifications-secret with Slack/email creds."
  warn "Remember to set FOOTBALL_DATA_API_TOKEN in helm/match-service/values-prod.yaml before deploying match-service."
}

# ── Main ───────────────────────────────────────────────────────────────────────
main() {
  check_prereqs
  create_cluster
  install_metrics_server
  build_and_load
  install_argocd
  apply_manifests
  print_access_info
}

main "$@"
