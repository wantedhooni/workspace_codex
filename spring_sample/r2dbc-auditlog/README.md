# r2dbc-auditlog

R2DBC 기반 업무 요청 처리와 감사 로그 적재를 함께 보여주는 샘플이다. 상태 변경 시점마다 audit trail을 남겨 운영 추적성을 확보하는 구조를 단순화해 담았다.

## 목적

- 반응형 트랜잭션 안에서 도메인 데이터와 감사 로그를 함께 저장하는 예시 제공
- 승인 요청 생성, 상태 변경, 감사 이력 조회 흐름 설명
- 운영성 관점에서 추적 가능한 API 설계 예시 제공

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring WebFlux
- Spring Data R2DBC
- PostgreSQL
- Gradle

## 제공 기능

- 승인 요청 생성
- 승인 요청 승인 처리
- 요청 번호 기준 단건 조회
- 요청 번호 기준 감사 로그 조회

## 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/r2dbc-auditlog
docker compose up -d
./gradlew bootRun
```

- 애플리케이션 포트: `8082`
- PostgreSQL 포트: `5434`

## API 예제

```bash
curl -X POST http://localhost:8082/api/approval-requests \
  -H 'Content-Type: application/json' \
  -d '{
    "requestNumber": "APR-2026-001",
    "requester": "platform-team",
    "targetSystem": "billing-api",
    "reason": "월말 정산 배치 권한 오픈"
  }'
```

```bash
curl -X POST http://localhost:8082/api/approval-requests/APR-2026-001/approve \
  -H 'Content-Type: application/json' \
  -d '{
    "actor": "ops-manager",
    "comment": "배포 윈도우 확인 후 승인"
  }'
```

```bash
curl http://localhost:8082/api/audit-logs/APR-2026-001
```

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/r2dbc-auditlog
./gradlew test
./gradlew build
```
