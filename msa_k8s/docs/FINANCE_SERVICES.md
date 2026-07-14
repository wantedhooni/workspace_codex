# 금융 서비스 설계 및 API 가이드

## 서비스 경계

| 서비스 | 포트 | 책임 | 원본 저장소 |
| --- | ---: | --- | --- |
| Account Service | `8082` | 계좌 개설, 잔액 조회, 멱등 입출금, 거래 원장 | PostgreSQL `account` 스키마 |
| Contents Service | `8083` | 공지사항·게시물 작성, 수정, 발행, 보관, 목록 조회 | PostgreSQL `contents` 스키마 |

두 서비스는 User Service 테이블을 직접 조회하거나 외래 키로 결합하지 않는다. 요청의 `ownerId`, `authorId`는 회원 식별자의 스냅샷이며, 운영에서는 JWT subject 또는 별도 회원 검증 API/이벤트로 권한과 존재 여부를 검증해야 한다.

## Account Service

### 정합성 정책

- 금액은 부동소수점이 아닌 PostgreSQL `numeric(19,2)`와 Java `BigDecimal`을 사용한다.
- 계좌번호는 PostgreSQL sequence로 발급해 여러 Account Service Pod 사이의 중복을 방지한다.
- 입출금은 계좌 행에 `PESSIMISTIC_WRITE` 잠금을 획득한 짧은 DB 트랜잭션 안에서 처리한다.
- 출금 후 잔액은 0 미만이 될 수 없으며 DB check constraint로도 방어한다.
- 모든 입출금은 `account_ledger_entry`에 불변 원장으로 남긴다.
- `Idempotency-Key`는 전체 거래 원장에서 고유하다. 동일 키와 동일 요청은 기존 결과를 반환하고, 다른 요청은 `409`로 거절한다.
- 외부 결제나 타행 API 호출은 현재 트랜잭션 안에 추가하지 않는다. 외부 연동이 필요하면 사전 호출 또는 Outbox/Saga를 사용한다.

### API

로컬 Compose 기본 구성은 Keycloak 인증이 활성화되어 있다. API 예시는 다음 토큰을 발급한 뒤 실행한다.

```bash
ACCESS_TOKEN=$(
  curl -fsS -X POST 'http://localhost:8090/realms/commerce/protocol/openid-connect/token' \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    --data-urlencode 'grant_type=password' \
    --data-urlencode 'client_id=commerce-cli' \
    --data-urlencode 'username=alice' \
    --data-urlencode 'password=alice-password' |
  sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p'
)
```

계좌 개설:

```bash
curl -i -X POST http://localhost:8080/api/v1/accounts \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{"ownerId":"00000000-0000-0000-0000-000000000001","currency":"KRW"}'
```

회원 계좌 목록:

```bash
curl 'http://localhost:8080/api/v1/accounts?ownerId=00000000-0000-0000-0000-000000000001' \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

입금과 출금:

```bash
curl -X POST http://localhost:8080/api/v1/accounts/{accountId}/deposits \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: deposit-20260702-0001' \
  -d '{"amount":10000.00,"memo":"최초 입금"}'

curl -X POST http://localhost:8080/api/v1/accounts/{accountId}/withdrawals \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: withdrawal-20260702-0001' \
  -d '{"amount":1000.00,"memo":"ATM 출금"}'
```

최근 원장:

```bash
curl 'http://localhost:8080/api/v1/accounts/{accountId}/transactions?limit=20' \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

## Contents Service

### 상태와 동시성 정책

```text
DRAFT ──publish──> PUBLISHED
  │                    │
  └────archive─────────┴──> ARCHIVED
```

- 콘텐츠는 항상 `DRAFT`로 생성한다.
- `PUBLISHED`는 수정할 수 있지만 다시 발행할 수 없다.
- `ARCHIVED`는 수정, 발행, 재보관할 수 없다.
- 수정·발행·보관 요청은 조회 응답의 `version`을 포함해야 한다.
- 다른 요청이 먼저 반영되어 버전이 달라지면 `409 CONTENT_CONFLICT`를 반환한다.
- 목록은 OFFSET 대신 `(created_at, id)` 복합 커서를 사용한다. `nextCursor`는 클라이언트가 해석하지 않고 다음 요청에 그대로 전달한다.

### API

초안 작성:

```bash
curl -i -X POST http://localhost:8080/api/v1/contents \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{
    "type":"NOTICE",
    "title":"서비스 점검 안내",
    "body":"점검 일정을 안내합니다.",
    "authorId":"00000000-0000-0000-0000-000000000001"
  }'
```

수정과 발행:

```bash
curl -X PUT http://localhost:8080/api/v1/contents/{contentId} \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{"title":"점검 안내","body":"변경된 일정입니다.","version":0}'

curl -X POST http://localhost:8080/api/v1/contents/{contentId}/publish \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{"version":1}'
```

목록과 다음 페이지:

```bash
curl 'http://localhost:8080/api/v1/contents?type=NOTICE&status=PUBLISHED&size=20' \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
curl 'http://localhost:8080/api/v1/contents?type=NOTICE&status=PUBLISHED&size=20&cursor={nextCursor}' \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

## 로컬 실행

공통 Dockerfile로 전체 애플리케이션 이미지를 빌드하고 데이터 인프라와 함께 실행한다.

```bash
cp .env.example .env
docker compose up -d --build --wait
docker compose ps
```

직접 `bootRun`할 때 인증을 끈 기본값을 사용하면 `docker compose up -d --wait postgresql redis`로 데이터 인프라만 시작하고 설치된 JDK 21을 `JAVA_HOME`에 지정한다. 인증을 켜고 직접 실행하려면 Keycloak도 시작하고 `OAUTH2_ENABLED`, `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`, `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI`를 함께 지정한다.

## 운영 확장 시 필수 항목

- 계좌 소유권과 콘텐츠 작성/관리 권한을 JWT `sub` 및 claim 기반으로 리소스 소유자와 비교 검증
- 송금 도메인은 단순 출금+입금 호출이 아닌 이중 원장, Saga 또는 단일 원장 트랜잭션으로 별도 설계
- 계좌 거래 감사 로그, 이상거래 탐지, 개인정보 마스킹, 데이터 보존 정책
- 서비스별 DB 계정과 최소 권한, 암호화 키 관리, PITR 및 정기 복구 훈련
- 원장 변경 이벤트용 Transactional Outbox와 메시지 브로커
- 금액·통화별 한도, 일일 누적 한도, 휴일/정산 정책
