#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

kubectl delete -f "$ROOT_DIR/istio" --ignore-not-found=true
kubectl delete -f "$ROOT_DIR/k8s/base" --ignore-not-found=true

