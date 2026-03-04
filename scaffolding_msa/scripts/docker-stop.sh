#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "[docker-stop] stopping docker compose stack"
cd "$ROOT_DIR"
docker compose down "$@"

echo "[docker-stop] docker compose stack stopped"
