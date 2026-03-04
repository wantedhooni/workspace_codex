# services

실행 가능한 Spring Boot 서비스 모듈 디렉터리다. 각 서비스는 독립 배포 단위로 유지하고, 공통 코드는 `common`에만 둔다.

## 모듈 목록
- `discovery-service`: Eureka Server
- `config-server`: Spring Cloud Config Server + Vault + Web UI
- `api-gateway`: 외부 요청 진입점
- `auth-server`: OAuth2 Authorization Server
- `user-service`: 사용자 도메인 서비스
- `order-service`: 주문 도메인 서비스

## 실행 메모
- `config-server`는 `discovery-service` 다음에 먼저 올리는 편이 안정적이다.
- `user-service`, `order-service`, `auth-server`, `api-gateway`는 필요할 때만 Config Server를 바라보게 할 수 있다.
- 선택형 사용은 실행 시 `SPRING_CONFIG_IMPORT=optional:configserver:` 추가 여부로 제어한다.

## 추가 기준
- 새 서비스는 `services:<name>-service` 형식으로 추가한다.
- REST 엔드포인트는 `api`, 비즈니스 조합은 `service`, 저장은 `domain/repository`, 설정은 `config` 패키지에 둔다.
- 서비스 간 직접 DB 접근은 금지하고, 내부 API 또는 이벤트로만 연동한다.
- Config Server 사용은 서비스별로 `SPRING_CONFIG_IMPORT=optional:configserver:` 지정 여부로 선택한다.
- 중앙 설정이 필수가 아닌 서비스는 로컬 `application.yml`만으로도 기동 가능해야 한다.
