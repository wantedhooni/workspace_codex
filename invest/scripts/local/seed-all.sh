#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SEED_DIR="${ROOT_DIR}/scripts/local/seeds"
STRICT_SEED="${STRICT_SEED:-false}"

log() {
  printf '[seed-all] %s\n' "$1"
}

if [ ! -d "${SEED_DIR}" ]; then
  log "seed directory not found: ${SEED_DIR}"
  exit 0
fi

shopt -s nullglob
SEED_SCRIPTS=("${SEED_DIR}"/*.sh)
shopt -u nullglob

if [ ${#SEED_SCRIPTS[@]} -eq 0 ]; then
  log "no seed scripts found"
  exit 0
fi

failed=0

for script in "${SEED_SCRIPTS[@]}"; do
  if [ ! -x "${script}" ]; then
    log "skip (not executable): ${script}"
    continue
  fi

  log "run ${script##*/}"
  if ! "${script}"; then
    log "failed ${script##*/}"
    failed=$((failed + 1))
  fi

done

if [ ${failed} -gt 0 ]; then
  if [ "${STRICT_SEED}" = "true" ]; then
    log "completed with failures=${failed} (STRICT_SEED=true)"
    exit 1
  fi
  log "completed with failures=${failed} (continue)"
  exit 0
fi

log "completed successfully"
