# build-logic

Gradle 공통 컨벤션 플러그인을 담는 포함 빌드다. 멀티모듈 전반에 동일한 Java 버전, 의존성 관리, 테스트 규칙을 강제하기 위해 사용한다.

## 제공 플러그인
- `com.revy.scaffolding.spring-boot-service`: 실행형 Spring Boot 서비스용
- `com.revy.scaffolding.spring-library`: 공통 라이브러리용

## 목적
- 서비스별 `build.gradle` 중복 최소화
- Spring Boot / Spring Cloud 버전 정책 일원화
- 테스트 설정과 툴체인 설정 공통화
- Config Client, JPA, Security 같은 서비스별 선택 의존성은 각 모듈에서 명시적으로 추가한다.

