# scaffolding-msa

Spring Cloud 기반 MSA 스캐폴딩 프로젝트다. `discovery-service`, `config-server`, `api-gateway`, `auth-server`, `user-service`, `order-service`를 멀티모듈 Gradle 구조로 구성했고, 비즈니스 서비스에는 Spring Data JPA와 Querydsl을 기본 탑재했다.

실무에서 신규 MSA 백엔드를 빠르게 시작할 때 필요한 최소 플랫폼 구성을 먼저 제공하고, 이후 인증, 메시징, 설정 서버, 관측성 같은 운영 요소를 붙일 수 있도록 뼈대를 분리했다.

## 기술 스택
- Java 21
- Gradle 9.2.1 Wrapper
- Spring Boot 3.5.9
- Spring Cloud 2025.0.0
- Spring Data JPA
- Querydsl
- Spring Cloud Gateway
- Eureka Discovery
- OpenFeign
- H2 / PostgreSQL

## 제공 범위
- 멀티모듈 Gradle 빌드와 루트 공통 빌드 규약
- Eureka 기반 서비스 디스커버리
- Spring Cloud Config Server + Vault + 조회용 Web UI
- Spring Cloud Gateway 기반 진입점
- Spring Authorization Server 기반 인증 서버
- JPA + Querydsl 기반 도메인 서비스 예제 2종
- 공통 API 응답 포맷과 전역 예외 처리
- 로컬 H2 실행 환경과 Docker PostgreSQL 전환 환경

## 빠른 시작
### 요구 사항
- JDK 21
- Docker Desktop 선택 사항

### 전체 빌드
```bash
./gradlew build
```

### Docker 이미지 빌드
대표 예시:
```bash
docker build --build-arg APP_MODULE=discovery-service --build-arg APP_PORT=8761 -t scaffolding/discovery-service:local .
```

### Docker Compose 기동/중지
```bash
docker compose up -d --build
docker compose down
```

### 전체 기동/중지
```bash
./scripts/all-start.sh
./scripts/all-stop.sh
```

### 서비스 실행 순서
1. `discovery-service`
2. `config-server`
3. `api-gateway`
4. `auth-server`
5. `user-service`
6. `order-service`

각 서비스는 별도 터미널에서 아래 명령으로 실행한다.

## 프로젝트 구조
```text
.
├── AGENTS.md                # 작업 원칙
├── PLANS.md                 # 작업 계획
├── TASK.md                  # 작업 기록
├── common
│   └── core                 # 공통 API 응답, 예외 처리
├── config-repo              # Config Server native backend
├── services
│   ├── discovery-service    # Eureka Server
│   ├── config-server        # Config Server + Vault + UI
│   ├── api-gateway          # API Gateway
│   ├── auth-server          # OAuth2 Authorization Server
│   ├── user-service         # 사용자 서비스 (JPA + Querydsl)
│   └── order-service        # 주문 서비스 (JPA + Querydsl + Feign)
├── infra
│   └── docker-compose.yml   # 로컬 인프라
├── docker-compose.yml       # 전체 서비스 컨테이너 구성
├── Dockerfile               # 공통 멀티스테이지 이미지 빌드
├── scripts                  # 전체 기동/중지 스크립트
├── build.gradle             # 루트 공통 빌드 규약
└── settings.gradle          # 멀티모듈 구성
```

디렉터리별 설명은 [common/README.md](/Users/revy/workspace_codex/scaffolding_msa/common/README.md), [services/README.md](/Users/revy/workspace_codex/scaffolding_msa/services/README.md), [services/config-server/README.md](/Users/revy/workspace_codex/scaffolding_msa/services/config-server/README.md), [config-repo/README.md](/Users/revy/workspace_codex/scaffolding_msa/config-repo/README.md), [infra/README.md](/Users/revy/workspace_codex/scaffolding_msa/infra/README.md)에서 확인할 수 있다.

## 모듈 요약
| 모듈 | 포트 | 역할 | 비고 |
|---|---:|---|---|
| `services:discovery-service` | `8761` | Eureka 서버 | 서비스 등록/조회 |
| `services:config-server` | `8888` | 중앙 설정 서버 | native repo, Vault, `/config-ui` |
| `services:api-gateway` | `8000` | 외부 진입 게이트웨이 | 라우팅, 요청 ID 주입 |
| `services:auth-server` | `9000` | 인증 서버 | OAuth2/OIDC, JWK, 토큰 발급 |
| `services:user-service` | `8081` | 사용자 도메인 서비스 | JPA, Querydsl, 내부 조회 API |
| `services:order-service` | `8082` | 주문 도메인 서비스 | JPA, Querydsl, OpenFeign |

## 실행 순서
기본 프로필은 `local`이며, `user-service`와 `order-service`는 H2 메모리 DB로 즉시 실행된다.

권장 부팅 순서:
1. `discovery-service`
2. `config-server`
3. `api-gateway`
4. `auth-server`
5. `user-service`
6. `order-service`

### 1. discovery-service 실행
```bash
./gradlew :services:discovery-service:bootRun
```

### 2. config-server 실행
```bash
./gradlew :services:config-server:bootRun
```

### 3. api-gateway 실행
```bash
./gradlew :services:api-gateway:bootRun
```

### 4. auth-server 실행
```bash
./gradlew :services:auth-server:bootRun
```

### 5. user-service 실행
```bash
./gradlew :services:user-service:bootRun
```

### 6. order-service 실행
```bash
./gradlew :services:order-service:bootRun
```

## 프로필 전략
### `local`
- `user-service`, `order-service`는 인메모리 H2 사용
- 샘플 데이터 자동 적재
- 스캐폴딩 검증과 API 확인에 적합

### `docker`
- PostgreSQL 사용
- 운영형 DB 연결 흐름 확인용
- `infra/docker-compose.yml`와 함께 사용
- 컨테이너 환경에서는 루트 `docker-compose.yml` 기준으로 전체 스택 기동 가능

### Config Server 선택 사용
- 각 서비스는 기본적으로 로컬 `application.yml`만 사용한다.
- 특정 서비스만 Config Server를 쓰고 싶으면 실행 시 `SPRING_CONFIG_IMPORT=optional:configserver:`를 추가한다.
- Config Server 주소는 `CONFIG_SERVER_URL`로 바꿀 수 있으며 기본값은 `http://localhost:8888/config`다.
- `spring.cloud.config.import-check.enabled=false`를 적용해 Config Server를 쓰지 않는 서버도 동일 바이너리로 기동할 수 있게 했다.
- `./scripts/all-start.sh`는 `api-gateway`, `auth-server`, `user-service`, `order-service`를 Config Server 연동 모드로 기동한다.
- `./scripts/all-start.sh`는 마지막에 Gateway 라우트까지 확인한 뒤 종료한다.

예시:
```bash
SPRING_CONFIG_IMPORT=optional:configserver: \
./gradlew :services:user-service:bootRun
```

## Docker 기반 PostgreSQL 사용
```bash
docker compose -f infra/docker-compose.yml up -d
./gradlew :services:user-service:bootRun --args='--spring.profiles.active=docker'
./gradlew :services:order-service:bootRun --args='--spring.profiles.active=docker'
```

## Docker Compose 전체 스택
```bash
docker compose up -d --build
docker compose ps
docker compose down
```

기본 구성:
- `vault`
- `user-db`, `order-db`
- `discovery-service`
- `config-server`
- `api-gateway`
- `auth-server`
- `user-service`
- `order-service`

컨테이너 내부 연결 기준:
- Eureka: `http://discovery-service:8761/eureka`
- Config Server: `http://config-server:8888/config`
- user DB host: `user-db`
- order DB host: `order-db`

## Config Server + Vault 사용
```bash
docker compose -f infra/docker-compose.yml up -d vault

export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN=dev-root-token

vault kv put secret/application shared.api-key=sample-shared-key
vault kv put secret/user-service datasource.password=userapp-secret

./gradlew :services:config-server:bootRun
SPRING_CONFIG_IMPORT=optional:configserver: ./gradlew :services:user-service:bootRun
SPRING_CONFIG_IMPORT=optional:configserver: ./gradlew :services:order-service:bootRun
```

### Config Server 조회
```bash
curl http://localhost:8888/config/user-service/local
curl http://localhost:8888/config/application/default
open http://localhost:8888/config-ui
```

### Config Server Web UI
- 주소: `http://localhost:8888/config-ui`
- Gateway 경유 주소: `http://localhost:8000/config-ui`
- 기능: `application`, `profile`, `label` 기준으로 Config Server 응답을 웹에서 조회
- 성격: Spring Cloud Config Server의 공식 내장 화면이 아니라, 현재 프로젝트에 추가한 조회용 커스텀 UI

## 기본 데이터
- `user-service`는 `alice@example.com`, `bob@example.com` 사용자 2건을 적재한다.
- `order-service`는 `userId=1` 기준 샘플 주문 1건을 적재한다.

## 주요 API 예시
### 사용자 조회
```bash
curl http://localhost:8081/api/v1/users
```

### 사용자 등록
```bash
curl -X POST http://localhost:8081/api/v1/users \
  -H 'Content-Type: application/json' \
  -d '{"email":"charlie@example.com","name":"Charlie"}'
```

### 주문 등록
```bash
curl -X POST http://localhost:8082/api/v1/orders \
  -H 'Content-Type: application/json' \
  -d '{"userId":1,"amount":19900}'
```

### Gateway 경유 호출
```bash
curl http://localhost:8000/config/application/default
curl http://localhost:8000/config-ui
curl http://localhost:8000/.well-known/openid-configuration
curl http://localhost:8000/api/v1/users
curl http://localhost:8000/api/v1/orders?userId=1
```

### 토큰 발급
```bash
curl -X POST http://localhost:9000/oauth2/token \
  -u scaffolding-client:scaffolding-secret \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=client_credentials&scope=read write'
```

## auth-server 기본 계정
- 사용자 로그인: `admin` / `admin1234`
- OAuth2 Client ID: `scaffolding-client`
- OAuth2 Client Secret: `scaffolding-secret`
- Redirect URI: `http://127.0.0.1:8000/login/oauth2/code/scaffolding-client`

위 값은 개발용 초기값이며 운영 환경에서는 외부 비밀 관리와 영속 저장소로 교체해야 한다.

## 응답 형식
성공 응답 예시
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "email": "alice@example.com",
      "name": "Alice",
      "status": "ACTIVE"
    }
  ],
  "error": null,
  "timestamp": "2026-03-04T15:00:00"
}
```

실패 응답 예시
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "사용자를 찾을 수 없습니다. id=999",
    "timestamp": "2026-03-04T15:00:00"
  },
  "timestamp": "2026-03-04T15:00:00"
}
```

## 설계 포인트
- 공통 응답 포맷과 예외 처리는 `common:core`에서 통합 관리한다.
- `config-server`는 로컬 파일 기반 `config-repo`와 Vault를 합쳐 중앙 설정을 제공한다.
- `config-server` 기본 HTTP API 외에 조회 편의용 `config-ui` 웹 화면을 제공한다.
- 각 서비스는 `SPRING_CONFIG_IMPORT`를 지정했을 때만 Config Server를 사용한다.
- 각 도메인 서비스는 JPA 저장소와 Querydsl 커스텀 조회 저장소를 함께 사용한다.
- `order-service`는 `OpenFeign`으로 `user-service` 내부 API를 조회한다.
- `auth-server`는 Spring Authorization Server로 OAuth2/OIDC 엔드포인트를 제공한다.
- 로컬 개발은 H2로 빠르게 시작하고, Docker 프로필로 PostgreSQL 전환이 가능하다.
- 공통 Java 툴체인, BOM, 테스트 규칙은 루트 [build.gradle](/Users/revy/workspace_codex/scaffolding_msa/build.gradle)에서 관리한다.

## 검증
```bash
./gradlew test
./scripts/all-start.sh
./scripts/all-stop.sh
docker compose config
```

## 확장 가이드
- 서비스가 늘어나면 `services/<domain>-service` 형태로 모듈을 추가하고 현재 컨벤션 플러그인을 그대로 적용한다.
- 공통 응답, 예외, 필터, 유틸은 `common/core`에 먼저 배치하고 서비스가 직접 중복 구현하지 않도록 유지한다.
- 민감정보는 `config-repo` 대신 Vault 경로로 옮기고, Config Server 쪽 인증을 토큰 방식에서 AppRole 또는 Kubernetes 방식으로 전환하는 편이 안전하다.
- Config Server UI는 조회용으로만 유지하고, 수정 기능이 필요하면 별도 운영도구나 승인 흐름을 둔 관리 화면으로 분리하는 편이 안전하다.
- 운영 전환 시 `ddl-auto`는 제거하고 Flyway 또는 Liquibase를 붙이는 것이 적절하다.
- 외부 호출이 늘어나면 OpenFeign 공통 에러 디코더와 타임아웃 정책을 추가하는 편이 안전하다.

추가 구조 설명은 [docs/architecture.md](/Users/revy/workspace_codex/scaffolding_msa/docs/architecture.md)에서 볼 수 있다.
