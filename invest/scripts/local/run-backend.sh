#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

cd "${ROOT_DIR}"

export APP_ADMIN_USERNAME="${APP_ADMIN_USERNAME:-admin}"
export APP_ADMIN_PASSWORD="${APP_ADMIN_PASSWORD:-admin1234}"
export APP_USER_USERNAME="${APP_USER_USERNAME:-demo}"
export APP_USER_PASSWORD="${APP_USER_PASSWORD:-demo1234}"

docker compose up -d mariadb > /dev/null

echo "[local-backend] API: http://localhost:8080"
echo "[local-backend] demo login: ${APP_USER_USERNAME}/${APP_USER_PASSWORD}"

exec gradle :apps:api:bootRun --args='--spring.jpa.hibernate.ddl-auto=update'
