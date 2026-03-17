# smaple_multi-tenancy

Spring Boot + JPA + JWT + Next.js 조합으로 만든 멀티테넌시 데모 프로젝트다. 하나의 백엔드 서버를 공유하되 JWT에 담긴 `tenantId`와 저장소 조건을 함께 사용해 테넌트별 데이터가 분리되도록 구성했다.

## 목적

- JWT 인증과 테넌트 격리를 한 번에 보여주는 실무형 샘플 제공
- `backend`와 `frontend`를 분리해 로컬 개발 환경에서 각각 독립 실행 가능하도록 구성
- 데모 계정 전환만으로 테넌트별 목록/등록/상태 변경이 분리되는 화면 제공

## 디렉터리 구조

```text
smaple_multi-tenancy
├── AGENTS.md
├── PLANS.md
├── TASK.md
├── README.md
├── backend
├── frontend
├── runtime
└── scripts
```

## 기술 구성

- Backend
  - Spring Boot 3.4
  - Spring Security
  - Spring Data JPA
  - JWT(`jjwt`)
  - H2 파일 DB
- Frontend
  - Next.js 15
  - React 19
  - TypeScript

## 멀티테넌시 방식

이 샘플은 공유 스키마(shared schema) 방식이다.

- 모든 멀티테넌시 엔티티에 `tenant_id` 컬럼을 둔다.
- 로그인 시 `tenantId`, `username`, `password`를 검증해 JWT를 발급한다.
- 요청마다 JWT에서 `tenantId`를 읽어 `TenantContext`를 구성한다.
- 서비스 계층에서 Hibernate `tenantFilter`를 활성화하고, 핵심 조회는 `tenantId` 조건이 포함된 저장소 메서드로 한 번 더 제한한다.
- 다른 테넌트의 프로젝트 ID를 직접 호출해도 상태 변경이 거부된다.

## 데모 계정

| Tenant | Username | Password | 설명 |
| --- | --- | --- | --- |
| `alpha` | `alpha.admin` | `demo1234` | Alpha 운영 관리자 |
| `alpha` | `alpha.viewer` | `demo1234` | Alpha 조회 사용자 |
| `beta` | `beta.admin` | `demo1234` | Beta 운영 관리자 |
| `beta` | `beta.viewer` | `demo1234` | Beta 조회 사용자 |

## 실행 방법

### 전체 시작

```bash
cd /Users/revy/workspace_codex/spring_sample/smaple_multi-tenancy
./scripts/all-start.sh
```

실행 후 출력 정보:

- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8086`
- H2 Console: `http://localhost:8086/h2-console`

### 전체 중지

```bash
cd /Users/revy/workspace_codex/spring_sample/smaple_multi-tenancy
./scripts/all-stop.sh
```

### 전체 재시작

```bash
cd /Users/revy/workspace_codex/spring_sample/smaple_multi-tenancy
./scripts/all-restart.sh
```

## 개별 실행

### Backend

```bash
cd /Users/revy/workspace_codex/spring_sample/smaple_multi-tenancy/backend
./gradlew bootRun
```

### Frontend

```bash
cd /Users/revy/workspace_codex/spring_sample/smaple_multi-tenancy/frontend
npm install
npm run dev
```

필요하면 `frontend/.env.local`에 아래 값을 둘 수 있다.

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8086
```

## 주요 API

### 로그인

```bash
curl -X POST http://localhost:8086/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "tenantId": "alpha",
    "username": "alpha.admin",
    "password": "demo1234"
  }'
```

### 대시보드 조회

```bash
curl http://localhost:8086/api/projects/dashboard \
  -H "Authorization: Bearer <JWT>"
```

### 프로젝트 등록

```bash
curl -X POST http://localhost:8086/api/projects \
  -H "Authorization: Bearer <JWT>" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "신규 멀티테넌시 파일럿",
    "description": "공유 인프라 기반 신규 고객사 온보딩",
    "ownerName": "문지후",
    "status": "DISCOVERY"
  }'
```

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/smaple_multi-tenancy/backend
./gradlew test

cd /Users/revy/workspace_codex/spring_sample/smaple_multi-tenancy/frontend
npm install
npm run build

cd /Users/revy/workspace_codex/spring_sample/smaple_multi-tenancy
sh -n scripts/all-start.sh
sh -n scripts/all-stop.sh
sh -n scripts/all-restart.sh
```

## 확인 포인트

- Alpha 로그인 시 Alpha 데이터만 노출되는지 확인
- Beta 로그인 후 동일 화면에서 다른 데이터 셋으로 바뀌는지 확인
- 다른 테넌트 프로젝트 ID로 상태 변경 요청 시 오류가 반환되는지 확인
- 브라우저 새로고침 후에도 저장된 세션으로 대시보드가 다시 로드되는지 확인
