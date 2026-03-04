#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "[build] running gradle build"
cd "$ROOT_DIR"
./gradlew build

echo "[build] build completed"

