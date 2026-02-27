#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

echo "[infra] stopping containers"
docker compose down
