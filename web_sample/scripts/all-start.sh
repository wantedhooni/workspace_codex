#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

docker compose up -d user-postgres admin-postgres redis

export GRADLE_USER_HOME="${GRADLE_USER_HOME:-/tmp/web-sample-gradle}"

if [[ -x ./gradlew ]]; then
  GRADLE_CMD="./gradlew"
else
  GRADLE_CMD="gradle"
fi

nohup "$GRADLE_CMD" :modules:user-server:bootRun > /tmp/web-sample-user-server.log 2>&1 &
echo "$!" > /tmp/web-sample-user-server.pid

nohup "$GRADLE_CMD" :modules:admin-server:bootRun > /tmp/web-sample-admin-server.log 2>&1 &
echo "$!" > /tmp/web-sample-admin-server.pid

echo "애플리케이션 시작 요청 완료"
echo "USER 서버 API URL: http://localhost:8080"
echo "USER 서버 Health URL: http://localhost:8080/actuator/health"
echo "ADMIN 서버 API URL: http://localhost:8081"
echo "ADMIN 서버 Health URL: http://localhost:8081/actuator/health"
echo "Redis URL: redis://localhost:6379"
echo "USER 데모 계정: user@example.com / User1234!"
echo "ADMIN 데모 계정: admin@example.com / Admin1234!"
echo "USER 로그 확인: tail -f /tmp/web-sample-user-server.log"
echo "ADMIN 로그 확인: tail -f /tmp/web-sample-admin-server.log"
