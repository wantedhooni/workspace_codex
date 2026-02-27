# Admin / Application 공유 서비스 설계

## 공유 대상
- Auth: 로그인, 토큰 발급, 역할/권한
- Tenant: 멀티테넌시 정책, 조직/스토어 분리
- Metadata: 공통 스키마(확장 필드, 카테고리 속성)
- Search: 검색 인덱스/추천

## 요청 흐름
1. Admin 또는 Application이 각 게이트웨이로 진입
2. 게이트웨이가 공통 서비스로 라우팅
3. 도메인 서비스는 각자 DB를 소유

## 토큰 규격 예시
- `sub`: user id
- `tenant_id`: 테넌트
- `roles`: ADMIN, OPERATOR, CUSTOMER
- `scopes`: catalog:read, order:write 등

## 데이터 공유
- 공통 서비스는 읽기 중심
- 도메인 간 연결은 이벤트 기반을 권장

## 예시
- Admin에서 상품 수정 -> Catalog 이벤트 발행 -> Search 인덱스 업데이트
- Application 주문 생성 -> Order 이벤트 -> Notification 발송

## 이벤트 (Kafka)
- Topic: `catalog.events`
- Producer: `service-catalog`
- Consumer: `service-search` (인메모리 인덱스, OpenSearch로 교체 가능)
- Topic: `order.events`
- Producer: `service-order`
- Consumer: `service-notification`
