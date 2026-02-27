# Backend (Spring Boot MSA)

## 모듈
- Gateways: `gateway-admin`, `gateway-commerce`
- Shared Services: `service-auth`, `service-tenant`, `service-metadata`, `service-search`
- Domain Services: `service-catalog`, `service-order`, `service-pricing`, `service-inventory`, `service-customer`
- Supporting: `service-notification`, `service-scheduler`

## 포트
- gateway-admin: 8080
- gateway-commerce: 8081
- auth: 9001
- tenant: 9002
- metadata: 9003
- search: 9004
- catalog: 9010
- order: 9011
- pricing: 9012
- inventory: 9013
- customer: 9014
- notification: 9020
- scheduler: 9021

## 환경 변수
- `JWT_SECRET`: 게이트웨이/인증 서비스 공통 시크릿
- `DB_URL`, `DB_USER`, `DB_PASS`, `DB_DRIVER`: 각 서비스 DB 연결

## 실행 예시
```
./gradlew :gateway-admin:bootRun
```
