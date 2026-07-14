# PostgreSQL 및 Redis 인프라 가이드

## 역할

- PostgreSQL: 회원, 금융 계좌·거래 원장, 공지사항·게시물 원본 저장소
- Flyway: PostgreSQL 스키마 버전 관리
- Redis: 회원 단건 조회 캐시와 API Gateway 요청 제한
- Keycloak: 로컬·개발 인증/인가와 JWT 발급

Redis는 원본 데이터 저장소가 아니다. Redis 장애 시 User Service는 PostgreSQL 조회를 계속하며, Gateway는 readiness에서 제외되어 요청 제한을 우회하지 않는다.

## 로컬 실행

환경 파일을 준비하고 전체 서비스를 빌드·실행한다.

```bash
cp .env.example .env
docker compose up -d --build --wait
docker compose ps
```

애플리케이션을 IDE 또는 Gradle로 직접 실행하고 인증을 끈 기본값을 사용할 때는 데이터 인프라만 시작한다.

```bash
docker compose up -d --wait postgresql redis
./gradlew :user-service:bootRun
./gradlew :account-service:bootRun
./gradlew :contents-service:bootRun
```

기본 연결값:

| 설정 | 기본값 |
| --- | --- |
| PostgreSQL | `localhost:5432/commerce` |
| DB 사용자 | `commerce` |
| Redis | `localhost:6379` |
| Keycloak | `localhost:8090` |
| Keycloak management | `localhost:9000` |

중지:

```bash
docker compose down
```

로컬 데이터를 포함해 초기화할 때만 다음 명령을 사용한다.

```bash
docker compose down -v
```

## 스키마

서비스별 Flyway migration과 소유 스키마:

```text
user-service/src/main/resources/db/migration       public
account-service/src/main/resources/db/migration    account
contents-service/src/main/resources/db/migration   contents
```

애플리케이션 시작 시 migration을 적용하고 Hibernate `validate`로 엔티티와 스키마 정합성을 확인한다. 운영 중 적용된 migration 파일을 수정하지 않고 새 버전 파일을 추가한다.

Local/Dev는 비용과 재현성을 위해 하나의 PostgreSQL 인스턴스와 사용자 안에서 스키마를 분리한다. Staging/Production overlay는 Account와 Contents에 각각 별도 관리형 DB endpoint를 사용한다. 운영 Secret도 서비스별 DB 사용자와 최소 권한으로 분리하는 것이 원칙이다.

## Redis 캐시

- cache name: `users`
- key: 회원 UUID
- TTL: 10분
- write: 회원 생성 완료 후 cache put
- read: cache miss 시 PostgreSQL 조회
- 장애 정책: 캐시 오류를 기록하고 PostgreSQL로 fallback

## Kubernetes 개발 환경

`local`과 `dev` overlay는 단일 PostgreSQL/Redis StatefulSet, PVC, 개발용 Keycloak Deployment를 포함한다.

```bash
kubectl apply -k deploy/k8s/overlays/local
kubectl -n commerce rollout status statefulset/postgresql --timeout=5m
kubectl -n commerce rollout status statefulset/redis --timeout=5m
kubectl -n commerce rollout status deployment/keycloak --timeout=5m
kubectl -n commerce rollout status deployment/user-service --timeout=5m
kubectl -n commerce rollout status deployment/account-service --timeout=5m
kubectl -n commerce rollout status deployment/contents-service --timeout=5m
```

PVC 확인:

```bash
kubectl get pvc -n commerce
```

클러스터에 기본 StorageClass가 없으면 PVC가 `Pending` 상태가 된다.

## Staging 및 Production

Staging과 Production overlay는 데이터베이스 StatefulSet을 배포하지 않는다. 다음 외부 관리형 서비스를 전제로 한다.

- 서비스별 PostgreSQL 호환 managed database
- Redis 호환 managed cache
- 외부 Keycloak 또는 OIDC 호환 IdP
- TLS와 자동 백업
- 다중 AZ 또는 공급자 HA
- 모니터링과 장애조치

overlay의 예시 hostname, Registry, OIDC issuer를 실제 환경 값으로 변경해야 한다.

애플리케이션이 요구하는 Secret 이름:

```text
commerce-data-credentials
commerce-auth-credentials
```

Local/Dev 필수 key:

```text
POSTGRES_USER
POSTGRES_PASSWORD
USER_DB_USERNAME
USER_DB_PASSWORD
ACCOUNT_DB_USERNAME
ACCOUNT_DB_PASSWORD
CONTENTS_DB_USERNAME
CONTENTS_DB_PASSWORD
REDIS_PASSWORD
```

Staging/Production 필수 key:

```text
USER_DB_USERNAME
USER_DB_PASSWORD
ACCOUNT_DB_USERNAME
ACCOUNT_DB_PASSWORD
CONTENTS_DB_USERNAME
CONTENTS_DB_PASSWORD
REDIS_PASSWORD
OAUTH2_GATEWAY_CLIENT_SECRET
```

`POSTGRES_*`는 Local/Dev StatefulSet 초기화용이며 Staging/Production에서는 애플리케이션이 사용하지 않는다. 운영의 `*_DB_*` 값은 서비스별 최소 권한 계정으로 각각 발급한다. `OAUTH2_GATEWAY_CLIENT_SECRET`은 Gateway의 Keycloak confidential client secret이다. Secret은 Git에 저장하지 않고 External Secrets Operator, Secrets Store CSI 또는 클라우드 Secret Manager로 생성한다.

## 운영 점검

PostgreSQL:

```bash
kubectl exec -n commerce statefulset/postgresql -- \
  pg_isready -U commerce -d commerce
```

Redis:

```bash
kubectl exec -n commerce statefulset/redis -- redis-cli ping
```

운영 managed 서비스에는 위 StatefulSet 명령 대신 공급자 상태와 애플리케이션 Actuator health를 사용한다.

## 백업

개발용 StatefulSet은 HA 또는 자동 백업을 제공하지 않는다. 운영 PostgreSQL은 다음을 구성한다.

- 일 단위 full backup
- WAL 기반 point-in-time recovery
- 별도 계정 또는 리전에 백업 보관
- 정기 복구 훈련
- 정의된 RPO/RTO

Redis 캐시는 백업보다 재생성을 우선한다. Gateway rate limit 상태 보존이 필요하면 managed Redis의 HA와 persistence 정책을 별도로 적용한다.

계좌 원장은 일반 회원 데이터보다 엄격한 RPO/RTO, 변경 감사, 복구 후 잔액-원장 대사 절차가 필요하다. 운영 복구 훈련에는 임의 계좌의 원장 합계와 현재 잔액 검증을 포함한다.
