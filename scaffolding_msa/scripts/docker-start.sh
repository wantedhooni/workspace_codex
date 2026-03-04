#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "[docker-start] building and starting docker compose stack"
cd "$ROOT_DIR"
docker compose up -d --build "$@"

echo "[docker-start] docker compose stack is starting"
docker compose ps

