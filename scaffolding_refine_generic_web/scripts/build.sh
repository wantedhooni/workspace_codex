#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "${ROOT_DIR}/backand"
./gradlew :server:api-admin-server:build

cd "${ROOT_DIR}/frontend/admin-ui"
npm install
npm run build
