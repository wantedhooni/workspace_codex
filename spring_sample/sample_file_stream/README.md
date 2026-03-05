# sample_file_stream

JPA로 파일 메타데이터를 관리하고, 실제 파일 본문은 `LOCAL` 또는 `S3`에 저장/다운로드하는 샘플이다. 운영에서 바로 참고할 수 있게 저장소 추상화, 해시 계산, 메타데이터 조회 API를 분리했다.

## 목적

- 파일 메타데이터(`파일명`, `용량`, `컨텐츠 타입`, `저장 위치`, `SHA-256`)를 JPA로 관리
- 저장소를 `LOCAL`/`S3`로 선택해 업로드/다운로드 처리
- S3 비활성 환경에서도 로컬 저장소만으로 동일 API 사용

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring Data JPA
- H2
- AWS SDK v2 (S3)
- Gradle

## 주요 기능

- `multipart/form-data` 파일 업로드
- 저장소 타입 선택 업로드 (`LOCAL`, `S3`)
- 파일 메타데이터 단건/목록 조회
- 파일 다운로드 스트리밍
- 업로드 시 SHA-256 체크섬 계산

## 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_file_stream
./gradlew bootRun
```

- 기본 포트: `8080`
- H2 콘솔: `http://localhost:8080/h2-console`
- 기본 저장소: `LOCAL`

## S3 설정

기본값은 S3 비활성(`app.storage.s3.enabled=false`)이다. 활성화하려면 환경 변수를 지정한다.

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_file_stream
APP_STORAGE_S3_ENABLED=true \
APP_STORAGE_S3_BUCKET=your-bucket \
APP_STORAGE_S3_REGION=ap-northeast-2 \
./gradlew bootRun
```

LocalStack 등을 사용할 때는 endpoint/path-style 옵션을 추가한다.

```bash
APP_STORAGE_S3_ENABLED=true \
APP_STORAGE_S3_BUCKET=sample-file-stream \
APP_STORAGE_S3_REGION=ap-northeast-2 \
APP_STORAGE_S3_ENDPOINT=http://localhost:4566 \
APP_STORAGE_S3_PATH_STYLE_ACCESS=true \
./gradlew bootRun
```

## API 예제

### 1) 로컬 업로드

```bash
curl -X POST http://localhost:8080/api/files \
  -F "file=@/tmp/sample.pdf" \
  -F "storageType=LOCAL"
```

### 2) S3 업로드

```bash
curl -X POST http://localhost:8080/api/files \
  -F "file=@/tmp/sample.pdf" \
  -F "storageType=S3"
```

### 3) 메타데이터 단건 조회

```bash
curl http://localhost:8080/api/files/{fileId}
```

### 4) 메타데이터 목록 조회

```bash
curl http://localhost:8080/api/files
```

### 5) 다운로드

```bash
curl -L -o downloaded.bin http://localhost:8080/api/files/{fileId}/download
```

## 주요 설정

- `app.storage.default-type`: 기본 저장소 (`LOCAL`, `S3`)
- `app.storage.local.base-path`: 로컬 파일 저장 루트
- `app.storage.s3.enabled`: S3 사용 여부
- `app.storage.s3.bucket`: S3 버킷 이름
- `app.storage.s3.region`: 리전
- `app.storage.s3.endpoint`: 커스텀 엔드포인트(선택)
- `app.storage.s3.path-style-access`: Path-style URL 사용 여부

## 핵심 구성

- `FileService`: 파일 해시 계산, 저장소 업로드, 메타데이터 영속화
- `StorageRegistry`: 저장소 구현체 선택
- `LocalFileContentStorage`: 로컬 파일 저장/조회
- `S3FileContentStorage`: S3 업로드/다운로드
- `FileController`: 업로드/조회/다운로드 API

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_file_stream
./gradlew test
./gradlew build
```
