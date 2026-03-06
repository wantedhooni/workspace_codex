# dynamic-job-runner

Gradle 빌드(Gradle wrapper 포함)로 구성된 Quartz 기반 애플리케이션입니다. DB에 저장된 Spring Bean / method / argsJson 을 읽어 기존 서비스 메서드를 주기 실행하는 예제 프로젝트입니다.

## 목적

- 기존 서비스 코드는 그대로 사용
- Job 전용 클래스를 매번 새로 만들지 않음
- 새 잡 추가/수정은 DB row 와 API 호출로 처리
- argsJson 으로 DTO 파라미터 전달 가능

## 포함 기능

- Quartz 스케줄링
- H2 + JPA 기반 `job_definition`, `job_execution_log`
- Spring Bean method invoke
- DTO / primitive / wrapper 파라미터 지원
- 실행 로그 저장
- REST API로 job CRUD / enable / disable / run now
- 샘플 서비스 2개 포함

## 프로젝트 구조

```text
src/main/java/com/example/dynamicjob
├── config
├── controller
├── dto
├── entity
├── job
├── repository
└── service
```

## 실행

Gradle wrapper(`./gradlew`)를 사용하여 애플리케이션을 실행하거나 빌드합니다.

```bash
# 개발 모드로 실행
./gradlew bootRun

# 또는 빌드 후 jar로 실행
./gradlew build
java -jar build/libs/sample_dynamic-job-runner-0.0.1-SNAPSHOT.jar
```

애플리케이션 시작 후:

- API: `http://localhost:8080/api/jobs`
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:dynamicjob`
  - User: `sa`
  - Password: 빈 값

## 샘플 Job 정의

기본으로 2개가 로드됩니다.

### 1) DTO 파라미터

- beanName: `reportService`
- methodName: `generateDailyReport`
- argTypesJson:

```json
["com.example.dynamicjob.dto.ReportRequest"]
```

- argsJson:

```json
[
  {
    "bizDate": "2026-03-06",
    "type": "DAILY",
    "retryCount": 3
  }
]
```

### 2) Primitive/Wrapper 파라미터

- beanName: `userService`
- methodName: `syncUsers`
- argTypesJson:

```json
["java.lang.String", "java.lang.Boolean"]
```

- argsJson:

```json
["LEGACY", true]
```

## REST API

### 전체 조회

```bash
curl http://localhost:8080/api/jobs
```

### 단건 조회

```bash
curl http://localhost:8080/api/jobs/1
```

### 즉시 실행

```bash
curl -X POST http://localhost:8080/api/jobs/1/run
```

### 실행 로그 조회

```bash
curl http://localhost:8080/api/jobs/logs
```

### 신규 등록

```bash
curl -X POST http://localhost:8080/api/jobs \
  -H 'Content-Type: application/json' \
  -d '{
    "jobName": "monthly-summary",
    "cronExpr": "0 0 3 1 * ?",
    "beanName": "reportService",
    "methodName": "rebuildMonthlySummary",
    "argTypesJson": "[\"java.lang.String\"]",
    "argsJson": "[\"2026-03\"]",
    "enabled": true
  }'
```

### 수정

```bash
curl -X PUT http://localhost:8080/api/jobs/1 \
  -H 'Content-Type: application/json' \
  -d '{
    "jobName": "daily-report",
    "cronExpr": "0 0/5 * * * ?",
    "beanName": "reportService",
    "methodName": "generateDailyReport",
    "argTypesJson": "[\"com.example.dynamicjob.dto.ReportRequest\"]",
    "argsJson": "[{\"bizDate\":\"2026-03-06\",\"type\":\"DAILY\",\"retryCount\":5}]",
    "enabled": true
  }'
```

### 비활성화 / 활성화

```bash
curl -X POST http://localhost:8080/api/jobs/1/disable
curl -X POST http://localhost:8080/api/jobs/1/enable
```

### 삭제

```bash
curl -X DELETE http://localhost:8080/api/jobs/2
```

## 핵심 설계

### 1. 기존 서비스 재사용

새로운 잡 클래스를 작업마다 만들지 않고, DB에 저장된 `beanName + methodName + argTypesJson + argsJson` 을 사용해서 기존 Spring Bean 메서드를 실행합니다.

### 2. DTO 지원

`argsJson` 에 JSON object 를 넣고 `argTypesJson` 에 DTO 클래스명을 넣으면 Jackson 이 DTO로 역직렬화합니다.

### 3. 운영 포인트

실서비스에서는 아래 보완이 필요합니다.

- 허용 Bean / Method whitelist
- 관리자 권한 제어
- 민감 메서드 실행 차단
- cron expression 검증
- 긴 에러 메시지 truncate
- 분산 환경이면 Quartz JDBC JobStore 검토

## 예시 DB row

```sql
INSERT INTO job_definition (
    job_name, cron_expr, bean_name, method_name, arg_types_json, args_json, enabled, created_at, updated_at
) VALUES (
    'daily-report',
    '0 0 2 * * ?',
    'reportService',
    'generateDailyReport',
    '["com.example.dynamicjob.dto.ReportRequest"]',
    '[{"bizDate":"2026-03-06","type":"DAILY","retryCount":3}]',
    TRUE,
    CURRENT_TIMESTAMP(),
    CURRENT_TIMESTAMP()
);
```

## 제한 사항

현재 예제는 아래까지만 바로 지원합니다.

- 단일 DTO 파라미터
- 기본 타입 / wrapper / String
- 파라미터 배열

다음은 추가 확장이 필요합니다.

- `List<DTO>`
- 제네릭 컬렉션
- method overload 자동 해석
- 패키지/메서드 수준 세밀한 권한 통제
