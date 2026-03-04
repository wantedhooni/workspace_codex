# sample_spring_admin

Spring Boot Admin Server와 Client를 한 프로젝트에서 구성해 운영 상태를 한 화면에서 확인할 수 있는 샘플이다. 자기 자신을 모니터링 대상으로 등록해 바로 UI를 확인할 수 있게 했다.

## 목적

- Spring Boot Admin 연동 기본 구조 제공
- Actuator 기반 애플리케이션 상태 시각화 예시 제공
- 운영 담당자가 자주 보는 health, metrics, env, logfile 접근 경로 설명

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring Boot Admin 3.4
- Spring Boot Actuator
- Gradle

## 주요 기능

- Admin Server UI 제공
- 자기 자신을 Boot Admin Client로 자동 등록
- 운영 체크리스트 조회 API
- Actuator 엔드포인트 노출

## 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_spring_admin
./gradlew bootRun
```

- 애플리케이션 포트: `8080`
- Admin UI: `http://localhost:8080/admin`

## 확인 포인트

- `Applications` 에 현재 애플리케이션이 등록되는지 확인
- `Details` 에서 health, metrics, env, beans, mappings 를 확인
- `Journal` 에서 상태 이벤트를 확인

## API 예제

```bash
curl http://localhost:8080/api/admin/checklists
curl http://localhost:8080/actuator/health
```

## 주요 설정

- `spring.boot.admin.ui.public-url`
- `spring.boot.admin.client.url`
- `management.endpoints.web.exposure.include`

## 확장 방향

- Basic Auth 또는 OAuth2 로그인 적용
- Slack / Teams notifier 연동
- 다중 애플리케이션 등록 예제 추가

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_spring_admin
./gradlew test
./gradlew build
```
