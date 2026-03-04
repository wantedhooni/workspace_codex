# infra

로컬 개발과 통합 테스트를 위한 인프라 자산 디렉터리다.

## 포함 파일
- `docker-compose.yml`: Vault, `user-service`, `order-service`용 로컬 인프라 실행 정의

전체 서비스 컨테이너 기동은 루트 [docker-compose.yml](/Users/revy/workspace_codex/scaffolding_msa/docker-compose.yml)을 사용한다.

## 사용 예시
```bash
docker compose -f infra/docker-compose.yml up -d
docker compose -f infra/docker-compose.yml down
```

Vault만 먼저 띄우고 싶다면:
```bash
docker compose -f infra/docker-compose.yml up -d vault
```

## Vault 개발용 접속 정보
- 주소: `http://localhost:8200`
- Root Token: `dev-root-token`

## Vault 샘플 데이터 적재
```bash
export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN=dev-root-token

vault kv put secret/application shared.api-key=sample-shared-key
vault kv put secret/user-service datasource.password=userapp-secret
vault kv put secret/order-service datasource.password=orderapp-secret
```

## 메모
- 현재는 Vault와 PostgreSQL만 포함한다.
- 이후 Redis, Kafka, Zipkin, Prometheus, Grafana가 필요하면 이 디렉터리에 추가하는 방식이 자연스럽다.
