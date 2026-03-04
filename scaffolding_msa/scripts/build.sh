#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "[build] running gradle build"
cd "$ROOT_DIR"
./gradlew build

if [[ -f "$ROOT_DIR/package.json" ]]; then
  if [[ ! -d "$ROOT_DIR/node_modules" ]]; then
    echo "[build] installing frontend workspace dependencies"
    npm install
  fi

  echo "[build] running frontend workspace build"
  npm run build:frontends
fi

echo "[build] build completed"
