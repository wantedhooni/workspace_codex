#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is required to run this script."
  exit 1
fi

cd "$ROOT_DIR"

docker compose up --build
