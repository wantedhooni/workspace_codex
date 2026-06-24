# AGENTS.md

## 작업 원칙

- 이 작업공간(`/Users/revy/workspace_codex/kotlin_msa`) 내부 파일만 다룬다.
- 다른 프로젝트의 코드, 설정, 산출물을 참조하거나 침범하지 않는다.
- 실무에서 바로 확장 가능한 구조를 우선한다.
- Spring Boot 4, Kotlin, Gradle Kotlin DSL, Spring Cloud 2025.1 계열을 기준으로 작성한다.
- Kubernetes와 Istio 배포를 전제로 하되, 로컬 실행도 가능하게 기본 프로파일을 둔다.
- 클래스, 서비스, 서비스 메서드에는 한글 KDoc 주석을 작성한다.

## 코드 작성 기준

- 도메인 모델은 서비스별 경계를 명확히 분리한다.
- 서비스 간 통신은 HTTP API 계약을 기준으로 하고, Kubernetes 환경에서는 Service DNS를 사용한다.
- 데이터 저장소는 초기 스캐폴딩 단계에서는 인메모리 구현으로 제공한다.
- 운영 확장을 위해 Actuator, readiness/liveness probe, ConfigMap 기반 설정을 포함한다.
- Istio 트래픽 제어는 Gateway, VirtualService, DestinationRule로 분리한다.

