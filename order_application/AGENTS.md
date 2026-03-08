# AGENTS.md

## 목적

이 문서는 Codex가 **주문/결제/재고 계열 백엔드 시스템**을 구현하거나 리팩터링할 때 따라야 할 운영 원칙과 구현 규칙을 정의한다.

이 시스템은 **Saga 패턴을 직접 구현하지 않는다.** 대신 아래 5가지를 기본 원칙으로 채택한다.

1. **멱등성 API**
2. **재시도 + Unknown 상태 관리**
3. **취소/무효화 API**
4. **Transactional Outbox**
5. **비동기 이벤트 처리**

---

## 아키텍처 원칙

| 항목      | 원칙                                                                  |
| ------- | ------------------------------------------------------------------- |
| 분산 트랜잭션 | 2PC, XA, 글로벌 트랜잭션 사용 금지                                             |
| 서비스 경계  | 각 서비스는 자신의 DB만 직접 수정                                                |
| 정합성 모델  | 강한 일관성보다 **최종적 일관성** 우선                                             |
| 실패 대응   | rollback 기대 금지, **멱등성 + 재시도 + 취소**로 해결                              |
| 이벤트 발행  | DB 저장과 이벤트 발행은 **Transactional Outbox**로 연결                         |
| 통신 방식   | 핵심 쓰기 작업은 동기 API 가능, 후속 반영은 비동기 이벤트 우선                              |
| 상태 설계   | `UNKNOWN`, `PENDING`, `CONFIRMED`, `CANCELED`, `FAILED` 등 명시적 상태 필수 |

---

## Codex 작업 원칙

Codex는 아래 규칙을 반드시 따른다.

### 1. 직접 Saga 오케스트레이터를 만들지 말 것

* 별도 Saga Coordinator, Workflow Engine, 중앙 보상 오케스트레이터를 기본 구현으로 넣지 않는다.
* 요구사항에 명시되지 않은 한 Saga, Choreography, Orchestration 기반 분산 트랜잭션을 도입하지 않는다.
* 먼저 **로컬 트랜잭션 + Outbox + 이벤트 소비자 + 취소 API** 조합으로 해결한다.

### 2. 모든 외부 연동 쓰기 API는 멱등하게 만들 것

* 주문 생성, 결제 승인 요청, 취소 요청, 예약 요청은 모두 멱등해야 한다.
* `Idempotency-Key` 또는 동등한 비즈니스 키를 받아야 한다.
* 동일 키 재요청 시 **중복 생성 금지** 및 **이전 결과 재반환**이 가능해야 한다.

### 3. 실패는 3가지로 구분할 것

* **명확한 성공**
* **명확한 실패**
* **Unknown 상태**

네트워크 타임아웃, 응답 유실, downstream 5xx, 커넥션 종료는 기본적으로 `Unknown` 가능성을 먼저 고려한다.

### 4. Unknown 상태는 즉시 버리지 말 것

* Unknown은 실패로 단정하지 않는다.
* 재조회 API, 후속 확인 배치, 이벤트 후속 정리, 운영자 재처리 포인트를 남긴다.

### 5. 취소/무효화 API를 항상 별도로 둘 것

* 승인 API와 별개로 취소 API를 둔다.
* 실패 시 자동 rollback을 기대하지 말고, **명시적 취소/무효화**를 수행한다.

### 6. DB 저장 후 이벤트 발행은 반드시 Outbox 사용할 것

* 비즈니스 상태 저장과 이벤트 발행 요청은 하나의 로컬 트랜잭션으로 묶는다.
* 메시지 브로커에 직접 publish 후 DB commit 하는 구조는 금지한다.

### 7. 소비자는 반드시 멱등하게 만들 것

* 이벤트 중복 전달을 정상 시나리오로 간주한다.
* 이벤트 소비 이력 테이블 또는 멱등 키 저장소를 둔다.

---

## 기본 유즈케이스

### 주문 생성

1. 클라이언트가 주문 생성 요청
2. 주문 서비스는 `Idempotency-Key` 검증
3. 주문 레코드 저장 (`PENDING` 또는 `CREATED`)
4. Outbox에 `OrderCreated` 이벤트 저장
5. 커밋 후 Outbox Relay가 이벤트 발행
6. 후속 서비스가 비동기 처리

### 결제 승인

1. 결제 요청 수신
2. 외부 PG 연동 호출
3. 결과가 성공이면 `APPROVED`
4. 명확한 실패면 `FAILED`
5. 응답 불명확/타임아웃이면 `UNKNOWN`
6. 후속 조회 API 또는 배치로 상태 확정

### 주문 취소

1. 취소 요청 수신
2. 현재 상태 검증
3. 결제 취소 필요 시 취소 API 호출
4. 재고 복구 필요 시 이벤트 발행 또는 내부 처리
5. 주문 상태 `CANCELED` 반영
6. Outbox에 `OrderCanceled` 저장

---

## 상태 모델 규칙

Codex는 상태를 숨기지 말고 명시적으로 설계한다.

| 도메인 | 필수 상태 예시                                                        |
| --- | --------------------------------------------------------------- |
| 주문  | `CREATED`, `PENDING_CONFIRM`, `CONFIRMED`, `CANCELED`, `FAILED` |
| 결제  | `REQUESTED`, `APPROVED`, `FAILED`, `UNKNOWN`, `CANCELED`        |
| 재고  | `RESERVED`, `RELEASED`, `INSUFFICIENT`, `UNKNOWN`               |
| 이벤트 | `INIT`, `PUBLISHED`, `FAILED`, `RETRYING`                       |

규칙:

* boolean 하나로 상태 표현 금지
* `UNKNOWN` 상태 생략 금지
* 상태 전이 메서드는 엔티티 또는 도메인 서비스에 명시할 것
* 상태 전이 로그를 남길 것

---

## 멱등성 API 규칙

### 필수 요구사항

* 모든 write API는 멱등 키를 받을 수 있어야 한다.
* 멱등 키가 중복되면 **같은 요청인지 검증**한다.
* 같은 키 + 다른 payload 조합은 오류 처리한다.
* 최초 성공 응답을 저장하고 재반환할 수 있어야 한다.

### 권장 저장 구조

* `idempotency_key`
* `request_hash`
* `resource_type`
* `resource_id`
* `response_snapshot`
* `status`
* `created_at`
* `expires_at`

### 금지사항

* 멱등 키만 보고 payload 비교 없이 재사용 처리 금지
* 비멱등 POST를 외부 재시도 대상으로 노출 금지

---

## 재시도 + Unknown 상태 규칙

### 재시도 대상

* 일시적 네트워크 오류
* timeout
* 502/503/504
* 메시지 브로커 publish 실패
* lock timeout

### 재시도 비대상

* 입력값 검증 실패
* 비즈니스 규칙 위반
* 명확한 잔액 부족, 재고 부족
* 중복 확정과 같은 논리 오류

### 구현 규칙

* retry는 무한 반복 금지
* exponential backoff 적용
* retry 횟수 초과 시 `UNKNOWN` 또는 `FAILED` 전이
* 외부 결제/정산 시스템은 **조회 API** 또는 **callback 이벤트**로 최종 확정 경로를 둘 것

### Unknown 처리 규칙

* `UNKNOWN`은 운영 가능한 상태여야 한다.
* 조회 API, 운영 대시보드, 배치 정리, 수동 재처리 포인트를 제공한다.
* Unknown 상태 객체는 TTL 또는 재평가 정책을 가진다.

---

## 취소/무효화 API 규칙

### 원칙

* 모든 승인/예약 계열 작업에는 대응되는 취소 API가 있어야 한다.
* 취소 API도 멱등해야 한다.
* 취소는 rollback이 아니라 **별도 비즈니스 작업**으로 취급한다.

### 예시

* 결제 승인 ↔ 결제 취소
* 재고 예약 ↔ 재고 해제
* 쿠폰 사용 ↔ 쿠폰 복원
* 포인트 차감 ↔ 포인트 복구

### 구현 규칙

* 취소 가능 상태를 명시한다.
* 이미 취소된 요청은 성공적으로 재응답 가능해야 한다.
* 외부 시스템 취소 결과가 Unknown이면 내부 상태도 Unknown/CancelPending 계열로 남겨야 한다.

---

## Transactional Outbox 규칙

### 반드시 지킬 것

* 비즈니스 엔티티 저장과 Outbox insert를 **동일 DB 트랜잭션**에서 수행한다.
* 이벤트 발행 성공 여부를 API 응답 성공 기준으로 삼지 않는다.
* Outbox Relay/Publisher는 별도 프로세스로 동작하게 한다.

### Outbox 테이블 최소 컬럼

* `id`
* `aggregate_type`
* `aggregate_id`
* `event_type`
* `payload`
* `status`
* `retry_count`
* `next_retry_at`
* `created_at`
* `published_at`

### Relay 규칙

* poll 또는 CDC 중 하나 선택
* publish 성공 시 `PUBLISHED`
* 실패 시 `FAILED` 또는 `RETRYING`
* 재시도 횟수/간격 명시
* poison message 격리 경로 마련

### 금지사항

* 서비스 로직 안에서 DB 저장 후 즉시 브로커 publish만 수행하는 방식
* 브로커 publish 실패 시 DB rollback 기대

---

## 비동기 이벤트 처리 규칙

### 소비자 규칙

* at-least-once delivery를 기본 가정으로 구현
* 이벤트 중복 수신 허용
* 순서 뒤집힘 가능성 고려
* 소비 이력 저장 또는 버전 비교로 멱등 처리

### 이벤트 설계 규칙

* 이벤트명은 과거형 사용: `OrderCreated`, `PaymentApproved`, `OrderCanceled`
* payload에는 식별자, 버전, 발생 시각 포함
* 타 서비스 내부 모델 전체를 이벤트에 노출하지 않는다.

### 장애 처리

* 실패 이벤트는 재시도 큐 또는 DLQ로 이동 가능해야 한다.
* DLQ 적재 기준과 운영 대응 방법을 문서화한다.

---

## 권장 디렉토리 구조

```text
src/main/java
 ├─ domain
 │   ├─ order
 │   ├─ payment
 │   ├─ inventory
 │   └─ outbox
 ├─ application
 │   ├─ command
 │   ├─ query
 │   └─ service
 ├─ infrastructure
 │   ├─ persistence
 │   ├─ messaging
 │   ├─ relay
 │   └─ external
 └─ interfaces
     ├─ api
     ├─ consumer
     └─ scheduler
```

---

## API 설계 규칙

| API 종류     | 요구사항                    |
| ---------- | ----------------------- |
| POST 생성    | 멱등 키 지원 필수              |
| POST 승인/확정 | 멱등 키 또는 비즈니스 키 기반 중복 보호 |
| POST 취소    | 멱등 필수                   |
| GET 상태 조회  | `UNKNOWN` 포함 전체 상태 반환   |
| 운영용 API    | 재처리, 상태조회, outbox 조회 가능 |

### 응답 규칙

* 성공/실패/Unknown을 구분해서 응답 모델 정의
* 단순 200/500 이분법 금지
* `requestId`, `idempotencyKey`, `resourceId`, `currentStatus` 포함 권장

---

## 데이터 모델 규칙

### 주문 테이블

* 주문 ID
* 상태
* 금액
* 버전
* 생성시각/수정시각
* 취소 사유

### 결제 테이블

* 결제 ID
* 주문 ID
* 외부 거래 ID
* 상태 (`APPROVED`, `FAILED`, `UNKNOWN`, `CANCELED`)
* 승인시각/취소시각

### 멱등성 테이블

* idempotency key
* request hash
* resource mapping
* response snapshot

### Outbox 테이블

* event metadata
* payload
* relay status

---

## 동시성 규칙

* JPA 사용 시 `@Version` 적용 우선 검토
* 중복 승인/중복 취소 방지
* 상태 전이 시 낙관적 락 또는 조건부 업데이트 사용
* 재고/포인트 계열은 필요 시 분산락보다 **DB 조건 업데이트 + 멱등성**을 우선 검토

---

## 로깅/관측성 규칙

### 필수 로그 필드

* `traceId`
* `requestId`
* `idempotencyKey`
* `orderId`
* `paymentId`
* `eventId`
* `eventType`
* `statusBefore`
* `statusAfter`

### 필수 모니터링 항목

* Unknown 상태 건수
* Outbox 적체 건수
* 이벤트 publish 실패율
* 소비자 재시도 횟수
* 취소 API 실패율
* 멱등 충돌 건수

---

## 테스트 규칙

Codex는 아래 테스트를 반드시 작성한다.

### 단위 테스트

* 동일 멱등 키 재요청 시 중복 생성 방지
* 같은 키 + 다른 요청 바디 차단
* Unknown 상태 전이 검증
* 취소 API 멱등성 검증
* 상태 전이 허용/차단 검증

### 통합 테스트

* DB 커밋 시 Outbox 동시 저장 검증
* Relay publish 성공/실패 재시도 검증
* 이벤트 소비자 중복 수신 검증
* 외부 API timeout 시 Unknown 처리 검증
* 재처리 후 최종 상태 정합성 검증

### 장애 테스트

* 브로커 다운
* 외부 결제 API timeout
* duplicate event delivery
* relay 중복 실행
* consumer 재시작 후 재처리

---

## Codex 산출물 요구사항

작업 시 Codex는 아래 산출물을 포함해야 한다.

1. 도메인 상태 다이어그램
2. API 명세
3. 엔티티 및 테이블 설계
4. 멱등성 처리 방식
5. Outbox 구조 및 Relay 방식
6. Unknown 상태 처리 정책
7. 취소/무효화 플로우
8. 재시도 정책
9. 테스트 코드
10. 운영 체크포인트

---

## 금지사항

Codex는 아래를 기본안으로 제시하지 말 것.

* XA / 2PC / 글로벌 트랜잭션
* 이벤트 발행 유실 가능 구조
* 멱등성 없는 write API
* Unknown 상태를 즉시 FAILED 처리하는 구현
* 취소 API 없는 승인 플로우
* 중복 이벤트 미고려 소비자
* 상태 enum 없이 문자열 하드코딩
* 운영자가 추적 불가능한 비동기 흐름

---

## 기본 구현 우선순위

Codex는 아래 순서로 구현 제안을 한다.

1. 로컬 트랜잭션 안정화
2. 멱등성 API 추가
3. 상태 모델 명확화 (`UNKNOWN` 포함)
4. 취소/무효화 API 추가
5. Transactional Outbox 추가
6. 비동기 이벤트 소비자 멱등 처리
7. 재시도 정책 및 운영 지표 추가
8. 필요 시에만 Saga/워크플로 엔진 재검토

---

## 최종 판단 기준

이 시스템의 목표는 **Saga를 예쁘게 구현하는 것**이 아니라,

* 중복 요청에 안전하고
* 네트워크 불확실성을 견디며
* 실패를 취소/재처리 가능하게 만들고
* 이벤트 유실 없이
* 운영자가 상태를 추적 가능한 시스템

을 만드는 것이다.

Codex는 항상 이 목표를 우선한다.
