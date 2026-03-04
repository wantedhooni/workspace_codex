# sample_native_image

Spring Boot 애플리케이션을 GraalVM Native Image 대상으로 준비하는 샘플이다. RuntimeHints와 리소스 포함 설정을 함께 보여준다.

## 목적

- Native Image 빌드 준비 구조 예시 제공
- RuntimeHints 등록 예시 제공
- 리소스 포함과 간단한 REST API 조합 설명

## 기술 스택

- Java 21
- Spring Boot 3.4
- GraalVM Native Build Tools
- Spring AOT
- Gradle

## 제공 기능

- RuntimeHints 기반 reflection/resource 등록
- 텍스트 리소스 로드 API
- JVM 빌드와 Native 빌드 명령 예시

## 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_native_image
./gradlew bootRun
```

## Native 빌드 예시

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_native_image
./gradlew nativeCompile
```

## API 예제

```bash
curl http://localhost:8080/api/native/info
```

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_native_image
./gradlew test
./gradlew build
```
