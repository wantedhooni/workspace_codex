# AGENTS.md

## 작업 원칙

- 이 작업공간 안에서만 파일을 조회하고 변경한다.
- 다른 프로젝트의 코드나 설정을 참조하거나 침범하지 않는다.
- 예제 수준이 아닌 실무 운영을 고려한 구조, 오류 처리, 관측성, 배포 설정을 작성한다.
- Spring Boot 3.x와 Java 21을 기준으로 구현한다.
- Gradle Groovy DSL 기반 멀티모듈 프로젝트를 유지한다.
- 클래스, 서비스, 서비스 메서드에는 역할과 책임을 설명하는 한글 문서 주석을 작성한다.
- 비밀값은 저장소에 커밋하지 않고 환경 변수 또는 Kubernetes Secret으로 주입한다.
- Local/Dev를 제외한 데이터 계층은 관리형 서비스를 우선하고 외부 Secret으로 자격증명을 주입한다.
- 변경 후 가능한 범위에서 빌드, 테스트, Kubernetes 매니페스트 검증을 수행한다.

## 프로젝트 범위

- 커머스 기반 플랫폼에 API Gateway, User Service와 금융 Account/Contents Service를 제공한다.
- PostgreSQL을 회원·계좌 원장·콘텐츠 원본 저장소로, Redis를 회원 조회 캐시와 Gateway 요청 제한 저장소로 사용한다.
- Account Service는 금액 정밀도, 멱등성, 잔액 동시성, 불변 거래 원장을 우선한다.
- Contents Service는 상태 전이, 낙관적 잠금, 커서 페이지네이션을 우선한다.
- Spring Cloud Kubernetes를 사용해 Kubernetes 네이티브 서비스 디스커버리와 설정 연동을 지원한다.
- 컨테이너 이미지 빌드와 Kubernetes 배포 환경을 제공한다.
- 모든 실행 모듈은 루트 공통 `Dockerfile`을 사용하고 Docker Compose와 Docker Buildx로 빌드한다.
- OpenTelemetry와 LGTM(Loki, Grafana, Tempo, Mimir) 스택을 기준으로 로그, 메트릭, 트레이스를 수집한다.
- 공통 메트릭 의존성, 태그, 카디널리티 제한, 업무 지표는 `common-metrics` 모듈에서 관리한다.
- Keycloak 기반 OAuth2 Client와 Resource Server 구성을 사용하고, JWT 권한 변환 정책은 `common-security` 모듈에서 관리한다.

## 코드 규칙

- 패키지는 `com.example.commerce` 하위로 구성한다.
- API, 애플리케이션 서비스, 도메인, 인프라스트럭처 책임을 명확히 분리한다.
- 외부에 노출되는 API에는 입력 검증과 일관된 오류 응답을 적용한다.
- 테스트 가능한 비즈니스 로직을 우선하며 핵심 유스케이스에는 자동화 테스트를 작성한다.
- 운영용 Actuator 엔드포인트는 필요한 항목만 노출한다.
- 메트릭 태그에는 사용자·계좌·콘텐츠 식별자 같은 고카디널리티 값을 사용하지 않는다.
- 인증/인가 테스트는 Bearer 토큰과 Keycloak role 기준으로 수행한다.
