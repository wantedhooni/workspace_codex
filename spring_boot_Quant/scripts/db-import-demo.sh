#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

if ! docker compose ps | grep -q 'quant-mariadb'; then
  echo "[db-import] mariadb container not running. run ./scripts/infra-up.sh first"
  exit 1
fi

echo "[db-import] resetting database"
docker compose exec -T mariadb mariadb -uroot -proot1234 -e \
  "DROP DATABASE IF EXISTS quant; CREATE DATABASE quant CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

echo "[db-import] applying Flyway-compatible common migrations"
for file in backend-mvp/src/main/resources/db/migration/common/*.sql; do
  echo "  - $file"
  docker compose exec -T mariadb mariadb -uquant -pquant1234 quant < "$file"
done

echo "[db-import] applying local demo seed"
for file in backend-mvp/src/main/resources/db/migration/local/*.sql; do
  echo "  - $file"
  docker compose exec -T mariadb mariadb -uquant -pquant1234 quant < "$file"
done

echo "[db-import] done"
