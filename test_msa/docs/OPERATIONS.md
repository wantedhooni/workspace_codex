# 운영/관리 가이드 (요약)

## 공통 운영 원칙
- **게이트웨이 분리**: Admin/Commerce 트래픽 격리
- **공통 서비스 공유**: Auth/Tenant/Metadata/Search는 단일 소스
- **도메인 분리**: 각 서비스는 독립 DB
- **비동기 이벤트**: 데이터 동기화는 이벤트 기반 권장

## 모니터링
- Actuator endpoint: `/actuator/health`, `/actuator/metrics`
- 게이트웨이 레벨에서 요청량/에러율 트래킹

## 확장 전략
- Application 트래픽 급증 시 `gateway-commerce` 및 `service-search` 우선 스케일
- Admin은 write-heavy이므로 `service-catalog`, `service-pricing`에 throttle 적용

## 권한/보안
- 공통 JWT Claims: `tenant_id`, `roles`, `scopes`
- Admin 정책과 Application 정책을 분리
- 게이트웨이에서 JWT 검증 + 역할 확인 후 `X-User-Id`, `X-Tenant-Id`, `X-Roles` 헤더 전달

## 배포
- 서비스별 독립 배포
- 스키마 변경 시 **Backward compatibility** 유지

## 로컬 실행
```
# 인프라 (Postgres/Redis/Kafka/OpenSearch)
cd infra
docker compose up -d

# 백엔드
cd ../backend
./gradlew :service-auth:bootRun
./gradlew :service-catalog:bootRun
./gradlew :service-order:bootRun
./gradlew :service-customer:bootRun
./gradlew :gateway-admin:bootRun
./gradlew :gateway-commerce:bootRun
```

## 검색 확인
- 인덱스 쿼리: `GET /search/query?q=hoodie`
- 인덱스 스냅샷: `GET /search/index`

## 기본 계정
- admin / admin1234
- operator / operator1234
- customer / customer1234
