# sample_secure_bff

보안이 적용된 BFF와 API Composition 패턴을 보여주는 샘플이다. 인증된 사용자가 단일 BFF 엔드포인트를 호출하면 서버가 여러 내부 API를 조합해 대시보드 응답을 반환한다.

## 목적

- Spring Security 기반 웹 BFF 구조 예시 제공
- `RestClient + HTTP Interface` 조합 사용법 설명
- 인증/인가와 응답 조합을 한 프로젝트 안에서 보여주기

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring Security
- Spring Web MVC
- RestClient
- HTTP Interface
- Gradle

## 주요 기능

- 기본 사용자 인증
- 역할 기반 엔드포인트 접근 제어
- 고객/포지션/리스크 데이터 조합 응답
- 관리자 전용 세션 조회 API

## 기본 계정

- `trader / trader123`
- `ops / ops123`
- `admin / admin123`

## 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_secure_bff
./gradlew bootRun
```

- 기본 포트: `8080`

## API 예제

### 계좌 대시보드 조회

```bash
curl -u trader:trader123 http://localhost:8080/api/bff/accounts/ACC-100/dashboard
```

### 관리자 세션 조회

```bash
curl -u admin:admin123 http://localhost:8080/api/admin/session
```

## 핵심 구성

- `SecurityConfig`: HTTP Basic과 역할 기반 접근 제어
- `DownstreamClientConfig`: HTTP Interface 프록시 클라이언트 생성
- `DashboardAggregationService`: 여러 내부 응답 조합
- `MockDownstreamController`: 샘플 내부 API

## 확장 방향

- OAuth2/OIDC 로그인
- 토큰 relay
- 세션/CSRF/CORS 정책 분리
- 실제 외부 API 연동

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_secure_bff
./gradlew test
./gradlew build
```
