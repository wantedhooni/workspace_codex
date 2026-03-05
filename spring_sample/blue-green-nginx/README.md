# blue-green-nginx

Docker 없이 로컬 프로세스와 Nginx reload만으로 Blue-Green 배포를 수행하는 샘플 프로젝트다.

## 구성

- `app`: Spring Boot 애플리케이션 (`/api/deployment`, `/actuator/health`)
- `nginx/conf.d/blue-green-nginx.conf`: Nginx 서버 블록 샘플 (포트 `8088`)
- `nginx/upstreams/active-upstream.conf`: 현재 활성 슬롯(`blue`/`green`) 라우팅 파일
- `scripts/deploy.sh`: 빌드, 대상 슬롯 기동, 헬스체크, Nginx 전환
- `scripts/rollback.sh`: 반대 슬롯 롤백
- `scripts/status.sh`: 슬롯별 상태/버전/응답 확인
- `scripts/stop-all.sh`: blue/green 로컬 프로세스 정지

## 사전 준비

1. Java 21
2. Nginx 설치 및 실행
3. 아래 설정 파일을 Nginx include 경로에 반영

```bash
# 예시(macOS Homebrew)
cp /Users/revy/workspace_codex/spring_sample/blue-green-nginx/nginx/conf.d/blue-green-nginx.conf /opt/homebrew/etc/nginx/servers/blue-green-nginx.conf
nginx -t && nginx -s reload
```

Linux는 환경에 맞게 `conf.d` 또는 `sites-enabled` 경로에 동일 개념으로 반영하면 된다.

## 배포

```bash
cd /Users/revy/workspace_codex/spring_sample/blue-green-nginx

# 최초 배포 (blue)
./scripts/deploy.sh blue

# 다음 배포 (현재 active 반대 슬롯 자동 선택)
DEPLOY_VERSION=v2026.03.05 ./scripts/deploy.sh
```

## 롤백 및 상태 확인

```bash
./scripts/status.sh
./scripts/rollback.sh
./scripts/stop-all.sh
```

## 엔드포인트

- Ingress (Nginx): `http://127.0.0.1:8088/api/deployment`
- Blue: `http://127.0.0.1:18081/api/deployment`
- Green: `http://127.0.0.1:18082/api/deployment`

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/blue-green-nginx/app && ./gradlew test
sh -n /Users/revy/workspace_codex/spring_sample/blue-green-nginx/scripts/common.sh
sh -n /Users/revy/workspace_codex/spring_sample/blue-green-nginx/scripts/deploy.sh
sh -n /Users/revy/workspace_codex/spring_sample/blue-green-nginx/scripts/rollback.sh
sh -n /Users/revy/workspace_codex/spring_sample/blue-green-nginx/scripts/status.sh
```
