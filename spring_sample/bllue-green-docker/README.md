# bllue-green-docker

Docker + Nginx 기반 Blue-Green 배포 샘플이다.

## 구성

- `app`: Spring Boot 애플리케이션 (`/api/deployment`, `/actuator/health`)
- `docker-compose.yml`: `app-blue`, `app-green`, `nginx` 컨테이너 구성
- `nginx/upstreams/active-upstream.conf`: 현재 활성 슬롯 라우팅 파일
- `scripts/deploy.sh`: 빌드, 대상 슬롯 기동, 헬스체크, Nginx 전환
- `scripts/rollback.sh`: 반대 슬롯 롤백
- `scripts/status.sh`: 슬롯/ingress 상태 확인
- `scripts/down.sh`: 컨테이너 일괄 종료/정리
- `runtime/versions/*.version`: 슬롯별 마지막 배포 버전 기록 (롤백 시 재사용)

## 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/bllue-green-docker
./scripts/deploy.sh blue
./scripts/status.sh
```

다음 배포는 자동으로 반대 슬롯으로 전환된다.

```bash
DEPLOY_VERSION=v2026.03.05 ./scripts/deploy.sh
```

롤백:

```bash
./scripts/rollback.sh
```

정리:

```bash
./scripts/down.sh
```

## 엔드포인트

- Ingress: `http://127.0.0.1:8098/api/deployment`
- Blue: `http://127.0.0.1:19081/api/deployment`
- Green: `http://127.0.0.1:19082/api/deployment`

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/bllue-green-docker/app && ./gradlew test
bash -n /Users/revy/workspace_codex/spring_sample/bllue-green-docker/scripts/common.sh
bash -n /Users/revy/workspace_codex/spring_sample/bllue-green-docker/scripts/deploy.sh
bash -n /Users/revy/workspace_codex/spring_sample/bllue-green-docker/scripts/rollback.sh
bash -n /Users/revy/workspace_codex/spring_sample/bllue-green-docker/scripts/status.sh
docker compose -f /Users/revy/workspace_codex/spring_sample/bllue-green-docker/docker-compose.yml config
```
