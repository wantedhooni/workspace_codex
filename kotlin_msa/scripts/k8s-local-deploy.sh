#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET="${1:-auto}"
WITH_ISTIO="false"
SKIP_BUILD="false"
NAMESPACE="commerce"
VERSION="0.1.0"

IMAGES=(
  "commerce/api-gateway:${VERSION}"
  "commerce/product-service:${VERSION}"
  "commerce/inventory-service:${VERSION}"
  "commerce/payment-service:${VERSION}"
  "commerce/order-service:${VERSION}"
)

usage() {
  cat <<'USAGE'
Usage:
  scripts/k8s-local-deploy.sh [auto|docker-desktop|minikube|kind] [--with-istio] [--skip-build]

Examples:
  scripts/k8s-local-deploy.sh
  scripts/k8s-local-deploy.sh docker-desktop
  scripts/k8s-local-deploy.sh minikube --with-istio
  scripts/k8s-local-deploy.sh kind --skip-build

Environment:
  KIND_CLUSTER  kind 클러스터 이름. 기본값은 현재 context 또는 kind.
  DOCKER_DESKTOP_KIND_CLUSTER  Docker Desktop이 kind 기반일 때 클러스터 이름. 기본값 desktop.
USAGE
}

for arg in "$@"; do
  case "$arg" in
    auto|docker-desktop|minikube|kind)
      TARGET="$arg"
      ;;
    --with-istio)
      WITH_ISTIO="true"
      ;;
    --skip-build)
      SKIP_BUILD="true"
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "알 수 없는 인자입니다: $arg" >&2
      usage
      exit 1
      ;;
  esac
done

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "필수 명령을 찾을 수 없습니다: $1" >&2
    exit 1
  fi
}

detect_target() {
  local context
  context="$(kubectl config current-context 2>/dev/null || true)"

  if [[ "$context" == "docker-desktop" ]]; then
    echo "docker-desktop"
  elif [[ "$context" == kind-* ]]; then
    echo "kind"
  elif [[ "$context" == "minikube" || "$context" == minikube-* ]]; then
    echo "minikube"
  else
    echo "docker-desktop"
  fi
}

build_images() {
  if [[ "$SKIP_BUILD" == "true" ]]; then
    echo "[deploy] 이미지 빌드를 건너뜁니다."
    return
  fi

  case "$TARGET" in
    docker-desktop|kind)
      echo "[deploy] 로컬 Docker 데몬에 Jib 이미지 빌드"
      (cd "$ROOT_DIR" && ./gradlew jibDockerBuild)
      ;;
    minikube)
      echo "[deploy] minikube Docker 데몬에 Jib 이미지 빌드"
      eval "$(minikube docker-env)"
      (cd "$ROOT_DIR" && ./gradlew jibDockerBuild)
      ;;
    *)
      echo "지원하지 않는 배포 대상입니다: $TARGET" >&2
      exit 1
      ;;
  esac
}

load_kind_images() {
  if [[ "$TARGET" != "kind" || "$SKIP_BUILD" == "true" ]]; then
    return
  fi

  local context cluster
  context="$(kubectl config current-context)"
  cluster="${KIND_CLUSTER:-${context#kind-}}"
  if [[ -z "$cluster" || "$cluster" == "$context" ]]; then
    cluster="kind"
  fi

  echo "[deploy] kind 클러스터(${cluster})에 이미지 로드"
  for image in "${IMAGES[@]}"; do
    kind load docker-image "$image" --name "$cluster"
  done
}

load_docker_desktop_kind_images() {
  if [[ "$TARGET" != "docker-desktop" || "$SKIP_BUILD" == "true" ]]; then
    return
  fi

  if ! command -v kind >/dev/null 2>&1; then
    return
  fi

  local cluster
  cluster="${DOCKER_DESKTOP_KIND_CLUSTER:-desktop}"
  if ! kind get clusters 2>/dev/null | grep -qx "$cluster"; then
    return
  fi

  echo "[deploy] Docker Desktop kind 클러스터(${cluster})에 이미지 로드"
  for image in "${IMAGES[@]}"; do
    kind load docker-image "$image" --name "$cluster"
  done
}

apply_manifests() {
  echo "[deploy] Kubernetes 리소스 적용"
  kubectl apply -f "$ROOT_DIR/k8s/base"

  if [[ "$WITH_ISTIO" == "true" ]]; then
    if kubectl get crd gateways.networking.istio.io >/dev/null 2>&1; then
      echo "[deploy] Istio 리소스 적용"
      kubectl apply -f "$ROOT_DIR/istio"
    else
      echo "[deploy] Istio CRD를 찾을 수 없어 Istio 리소스 적용을 건너뜁니다." >&2
    fi
  fi
}

restart_deployments() {
  echo "[deploy] Deployment 재시작"
  kubectl -n "$NAMESPACE" rollout restart deploy/api-gateway
  kubectl -n "$NAMESPACE" rollout restart deploy/product-service
  kubectl -n "$NAMESPACE" rollout restart deploy/inventory-service
  kubectl -n "$NAMESPACE" rollout restart deploy/payment-service
  kubectl -n "$NAMESPACE" rollout restart deploy/order-service
}

wait_rollout() {
  echo "[deploy] 배포 rollout 대기"
  local deployments=(
    api-gateway
    product-service
    inventory-service
    payment-service
    order-service
  )

  for deployment in "${deployments[@]}"; do
    kubectl -n "$NAMESPACE" rollout status "deploy/${deployment}" --timeout=180s
  done
}

main() {
  require_command kubectl

  if [[ "$TARGET" == "auto" ]]; then
    TARGET="$(detect_target)"
  fi

  if [[ "$TARGET" == "minikube" ]]; then
    require_command minikube
  fi

  if [[ "$TARGET" == "kind" ]]; then
    require_command kind
  fi

  echo "[deploy] 대상: $TARGET"
  echo "[deploy] namespace: $NAMESPACE"

  build_images
  load_docker_desktop_kind_images
  load_kind_images
  apply_manifests
  restart_deployments
  wait_rollout

  echo "[deploy] 완료"
  kubectl -n "$NAMESPACE" get deploy,svc,pods
}

main
