#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

pids=()
cleanup() {
  for pid in "${pids[@]:-}"; do
    if kill -0 "$pid" 2>/dev/null; then
      kill "$pid" 2>/dev/null || true
    fi
  done
}
trap cleanup EXIT INT TERM

(
  cd "$root_dir/backand"
  ./gradlew :api-admin:bootRun
) &
pids+=("$!")

(
  cd "$root_dir/frontend/admin-ui"
  npm run dev
) &
pids+=("$!")

wait
