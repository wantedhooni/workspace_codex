# config-server

중앙 설정 서버 모듈이다. Spring Cloud Config Server를 기반으로 `native` 파일 저장소와 Vault 비밀 저장소를 함께 조회하고, 조회 편의용 웹 화면 `/config-ui`를 제공한다.

## 핵심 기능
- Config HTTP API: `/config/{application}/{profile}`
- Web UI: `/config-ui`
- Vault 우선, native 저장소 후순위의 composite backend
- Eureka 등록을 통한 Gateway 라우팅 지원

## 기본 포트
- `8888`

## 로컬 실행
```bash
./gradlew :services:config-server:bootRun
```

Vault를 같이 쓰려면:
```bash
docker compose -f infra/docker-compose.yml up -d vault
export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN=dev-root-token
./gradlew :services:config-server:bootRun
```

컨테이너로 전체 스택을 올릴 때는 루트 compose를 사용한다:
```bash
docker compose up -d --build config-server discovery-service vault
```

## 설정 원본
- native: [config-repo/README.md](/Users/revy/workspace_codex/scaffolding_msa/config-repo/README.md)
- vault: `secret/application`, `secret/<service-name>`

## 조회 예시
```bash
curl http://localhost:8888/config/application/default
curl http://localhost:8888/config/user-service/local
curl http://localhost:8888/config/order-service/local
```

## Web UI 메모
- `application`, `profile`, `label`을 입력해 조회한다.
- 수정 기능은 없다.
- 운영 환경에서는 읽기 권한과 노출 범위를 별도로 통제하는 편이 안전하다.
