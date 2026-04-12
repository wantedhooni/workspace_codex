# sample_file_manage

Spring Boot + JPA 기반 파일 관리 서버 샘플이다. 로컬 파일 저장소에 실파일을 저장하고, DB에는 파일 메타데이터와 다운로드 이력을 분리 저장한다.

## 목적

- 파일 업로드/다운로드와 메타데이터 관리를 실무형 계층 구조로 예시화
- 다운로드 집계와 상세 다운로드 이력 테이블을 함께 관리하는 패턴 제공
- 실행 스크립트, 테스트, H2 콘솔까지 포함한 독립 실행형 샘플 제공

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring Web MVC
- Spring Data JPA
- H2 Database
- Gradle

## 주요 기능

- Multipart 파일 업로드 및 로컬 저장소 저장
- 파일 메타데이터 테이블(`stored_files`) 관리
- 다운로드 이력 테이블(`file_download_histories`) 관리
- 파일 목록/상세 조회
- 다운로드 시 사용자, 사유, IP, User-Agent 이력 적재
- 파일 삭제 시 메타데이터 상태 변경 및 실제 파일 제거

## 프로젝트 구조

```text
sample_file_manage
├── src/main/java/com/example/samplefilemanage
│   ├── config
│   ├── dto
│   ├── entity
│   ├── repository
│   ├── service
│   └── web
├── data/files
├── scripts
├── AGENTS.md
├── PLANS.md
├── TASK.md
└── README.md
```

## 실행 방법

### 1. 애플리케이션 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_file_manage
./gradlew bootRun
```

### 2. 전체 실행 스크립트 사용

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_file_manage
./scripts/all-start.sh
```

### 3. 중지 / 재시작

```bash
./scripts/all-stop.sh
./scripts/all-restart.sh
```

## 접속 정보

- 애플리케이션: `http://localhost:8080`
- H2 콘솔: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:filemanage`
- 사용자명: `sa`
- 비밀번호: 빈 값
- 데모 계정: 인증 없음

## 테이블 설계

### `stored_files`

- 원본 파일명, 저장 파일명, 저장 경로, MIME 타입, 파일 크기
- 업로더, 설명, 상태(ACTIVE/DELETED)
- 다운로드 횟수, 업로드 시각, 마지막 다운로드 시각

### `file_download_histories`

- 대상 파일 ID
- 다운로드 사용자 식별자
- 다운로드 사유
- 클라이언트 IP
- User-Agent
- 다운로드 시각

## API 예시

### 파일 업로드

```bash
curl -X POST "http://localhost:8080/api/files" \
  -F "file=@/tmp/sample.txt" \
  -F "uploadedBy=ops-admin" \
  -F "description=운영 점검용 파일"
```

### 파일 목록 조회

```bash
curl "http://localhost:8080/api/files"
```

### 파일 상세 조회

```bash
curl "http://localhost:8080/api/files/1"
```

### 파일 다운로드

```bash
curl -L "http://localhost:8080/api/files/1/download?downloadedBy=audit-user&reason=감사검토" -o downloaded-file
```

### 다운로드 이력 조회

```bash
curl "http://localhost:8080/api/files/1/download-histories"
```

### 파일 삭제

```bash
curl -X DELETE "http://localhost:8080/api/files/1"
```

## 테스트

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_file_manage
./gradlew test
```

## 확장 방향

- 파일 버전 관리와 논리 삭제 정책 분리
- S3/오브젝트 스토리지 어댑터 추가
- 다운로드 승인 워크플로와 권한 정책 결합
- 바이러스 검사 또는 DLP 후처리 파이프라인 연동
