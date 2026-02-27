#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

cd "${ROOT_DIR}"

echo "[local-test] backend"
gradle test

echo "[local-test] frontend"
cd frontend-admin
npm test
