# Development Guide (생산성 중심)

이 문서는 다음 개발자가 **빠르게 기능을 추가하고**, **회귀를 줄이고**, **검증까지 한 번에 끝내기** 위한 실전 가이드입니다.

## 1. 기본 원칙

1. API 계약(OpenAPI)부터 고정하고 구현한다.
2. 기능은 수직 슬라이스로 개발한다.
3. 한 리소스 단위로 끝낸다.
4. 시드 데이터는 항상 재실행 가능한(idempotent) 형태로 작성한다.
5. 로컬 검증(백엔드 + 프론트 + UI 스모크) 없이 완료로 보지 않는다.

## 2. 권장 개발 순서

1. 실행 환경 준비
- `docker compose up -d`
- 백엔드: `cd backand && ./gradlew :server:api-admin-server:bootRun --args="--server.port=8090"`
- 프론트: `cd frontend/admin-ui && VITE_API_URL=http://localhost:8090 npm run dev -- --host localhost --port 5173`

2. API 계약 먼저 정리
- 요청/응답은 `record`로 정의
- DTO에 `@Schema`를 붙여 Swagger에 바로 반영
- Swagger URL 기준: `http://localhost:8090/swagger-ui/index.html`

3. 백엔드 구현
- Domain/Repo 생성 또는 확장
- `BaseCrudController` 패턴으로 CRUD 엔드포인트 구현
- 관계 ID(예: roleIds, permissionIds)는 controller에서 resolve
- 인증은 `/auth/login`, `/auth/me`, JWT cookie 흐름 유지

4. 기본 데이터/데모 계정 보강
- `SeedData`에서 권한/역할/메뉴/콘텐츠/계정을 보장
- `count()==0` 방식보다 `findBy...`/`existsBy...` 기반 보정 방식 권장

5. 프론트 연결
- `resources` 등록 (`src/App.tsx`)
- List/Create/Edit 페이지 3종 구현
- `authProvider`, `simpleRestProvider` 규약 준수

6. 검증
- API: 로그인/목록 조회 curl 검증
- UI: 데모 계정 로그인 + 주요 메뉴 접근 스모크 테스트
- 마지막으로 `./gradlew :server:api-admin-server:build -x test`

## 3. 백엔드 개발 패턴

### 3.1 DTO/컨트롤러
- DTO 네이밍: `CreateXRequest`, `UpdateXRequest`, `XResponse`
- 가능한 한 nullable 정책을 명확히 한다.
- update는 PATCH 의미를 지키고, `null` 처리 규칙을 일관되게 유지한다.

### 3.2 시드 데이터
- 신규 추가는 아래 순서 권장:
- Permission -> Role -> AdminUser -> Menu -> Content
- 메뉴/콘텐츠는 자연키(path/slug) 기준으로 upsert 성격으로 보정한다.

### 3.3 DB 관련 주의
- Postgres 준비 전 백엔드를 띄우면 부팅 실패한다.
- `@Lob` 컬럼은 시드 시 불필요하게 조회하지 않는다.
- `existsBySlug`처럼 최소 조회로 분기하는 패턴을 우선 사용한다.

## 4. 프론트 개발 패턴

1. 리소스 하나 추가 시 기본 순서
- `src/pages/<resource>.tsx`에 List/Create/Edit 추가
- `src/App.tsx` `resources`와 route 등록
- 필요한 경우 select 옵션용 관계 리소스 연결

2. 인증 관련 규칙
- API 호출은 `credentials: "include"` 유지
- 로컬 테스트는 `localhost` 호스트 통일 권장 (쿠키/SameSite 이슈 예방)

3. 화면 검증 최소 기준
- 로그인 성공
- 목록 조회
- 생성/수정/삭제 중 최소 1개 동작 확인

## 5. 작업 템플릿 (추천)

1. 이슈/기능 정의
- 어떤 리소스에 어떤 필드/관계가 필요한지 5줄 이내로 고정

2. 구현
- 백엔드 -> Swagger 확인 -> 프론트 연결 순서

3. 검증
- API 수동 체크 3개
- UI 스모크 1회
- 빌드 1회

4. 문서 업데이트
- `readme.md`에 실행/계정/새 리소스 반영

## 6. 자주 막히는 포인트

1. 디렉터리명 오타
- 실제 경로는 `backend`가 아니라 `backand`

2. Swagger 주소
- `/swagger-ui`는 리다이렉트
- 문서/공유 시 `/swagger-ui/index.html` 사용

3. 쿠키 인증 실패
- 프론트를 `127.0.0.1`로 열고 API를 `localhost`로 쓰면 불안정할 수 있음
- 둘 다 `localhost`로 통일 권장

4. DB 준비 타이밍
- `docker compose up -d` 직후 `pg_isready` 확인 후 백엔드 실행 권장

## 7. 완료 기준 (Definition of Done)

아래를 모두 만족하면 완료:

1. Swagger에서 새/변경 API 스펙 확인 가능
2. 데모 계정으로 로그인 가능
3. 해당 리소스 CRUD 동작 확인
4. 시드 재실행 시 중복/오류 없이 보정 동작
5. 백엔드 빌드 성공
6. 문서(`readme.md`, 필요 시 본 문서) 업데이트 완료
