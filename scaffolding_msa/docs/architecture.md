# Architecture

## 서비스 구성
### discovery-service
- Eureka Server 역할
- 각 서비스 등록과 조회 담당

### config-server
- Spring Cloud Config Server 역할
- `native` 파일 저장소와 Vault 비밀 저장소를 함께 조회
- `/config-ui` 경로에 조회용 웹 UI 제공

### api-gateway
- 외부 진입점
- `X-Request-Id` 헤더가 없으면 자동 생성
- 서비스 ID 기반 라우팅 수행

### user-service
- 사용자 마스터 서비스
- JPA 엔티티 기반 기본 저장
- Querydsl 기반 검색 조회 제공
- 외부 API와 내부 API 분리

### order-service
- 주문 서비스
- JPA 엔티티 기반 주문 저장
- Querydsl 기반 사용자별 주문 조회 제공
- OpenFeign으로 `user-service` 사용자 유효성 확인

## 레이어 기준
- `api`: 외부/내부 REST 엔드포인트
- `service`: 트랜잭션과 비즈니스 조합
- `domain`: 엔티티와 저장소
- `dto`: 요청/응답 모델
- `exception`: 서비스별 도메인 예외
- `config`: Querydsl, 인프라 설정

## 확장 권장 방향
1. Spring Cloud Bus 기반 설정 변경 전파 추가
2. 인증/인가 서버 또는 API Gateway JWT 필터 추가
3. Kafka, RabbitMQ 기반 이벤트 흐름 추가
4. Flyway 또는 Liquibase 기반 마이그레이션 적용
5. 관측성용 Micrometer, Prometheus, Grafana 연동
