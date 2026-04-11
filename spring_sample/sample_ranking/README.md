# sample_ranking

점수 이벤트를 적재하고 시즌별 리더보드, 플레이어 상세, 내 주변 순위를 제공하는 Spring Boot 기반 랭킹 시스템 서버 샘플이다. 단순 CRUD가 아니라 랭킹 스냅샷과 점수 이력을 분리해 운영형 구조를 가볍게 재현하는 데 초점을 맞췄다.

## 목적

- 점수 적재와 랭킹 조회를 분리한 실무형 API 구조 예시 제공
- 시즌 단위 리더보드, 플레이어 상세, 주변 순위 조회 패턴 제공
- 시드 데이터, 테스트, 실행 스크립트까지 포함한 독립 실행형 샘플 제공

## 기술 스택

- Java 21
- Spring Boot 3.4.4
- Spring Web
- Spring Data JPA
- H2 Database
- Gradle

## 주요 기능

- `POST /api/seasons/{seasonId}/scores` 점수 등록
- `GET /api/seasons/{seasonId}/leaderboard` 상위 리더보드 조회
- `GET /api/seasons/{seasonId}/players/{playerId}` 플레이어 상세 랭킹 조회
- `GET /api/seasons/{seasonId}/players/{playerId}/neighbors` 내 주변 순위 조회
- `GET /api/seasons/{seasonId}/overview` 시즌 운영 요약 조회

## 아키텍처

### 구성 요소

- `ranking.domain`
  - `PlayerRanking`: 시즌별 누적 랭킹 스냅샷
  - `ScoreRecord`: 점수 변동 이력
- `ranking.application`
  - `RankingCommandService`: 점수 등록과 스냅샷 갱신
  - `RankingQueryService`: 리더보드, 상세, 주변 순위 계산
- `ranking.api`
  - 컨트롤러와 요청/응답 DTO
- `config`
  - 시드 데이터 적재
- `common`
  - 공통 예외/오류 응답

### 정렬 규칙

동일 시즌 내 랭킹은 아래 우선순위로 정렬한다.

1. 누적 점수(`totalScore`) 내림차순
2. 승리 수(`winCount`) 내림차순
3. 최고 단일 점수(`bestSingleScore`) 내림차순
4. 마지막 경기 시각(`lastPlayedAt`) 오름차순
5. 플레이어 ID 오름차순

같은 정렬 값이면 공동 순위를 부여하고, 다음 순위는 competition ranking 방식으로 계산한다.

## 실행

### 스크립트로 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_ranking
./scripts/all-start.sh
```

실행 후 출력 정보

- API URL: `http://localhost:8092`
- H2 Console: `http://localhost:8092/h2-console`
- JDBC URL: `jdbc:h2:file:./runtime/data/ranking-db`
- 사용자: `sa`
- 비밀번호: 없음

### 직접 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_ranking
./gradlew bootRun
```

환경 변수로 아래 값을 조정할 수 있다.

- `SERVER_PORT`
- `APP_PORT`

## 데모 데이터

기본 시드 시즌은 `season-2026-spring` 이다.

대표 플레이어

- `player-100` / `Astra`
- `player-104` / `Echo`
- `player-106` / `Glint`

## API 예제

### 점수 등록

```bash
curl -X POST http://localhost:8092/api/seasons/season-2026-spring/scores \
  -H 'Content-Type: application/json' \
  -d '{
    "playerId": "player-200",
    "playerName": "Ion",
    "scoreDelta": 540,
    "result": "WIN",
    "memo": "랭킹전 승리"
  }'
```

### 리더보드 조회

```bash
curl "http://localhost:8092/api/seasons/season-2026-spring/leaderboard?limit=5"
```

### 플레이어 상세 조회

```bash
curl http://localhost:8092/api/seasons/season-2026-spring/players/player-100
```

### 주변 순위 조회

```bash
curl "http://localhost:8092/api/seasons/season-2026-spring/players/player-106/neighbors?radius=2"
```

## 테스트

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_ranking
./gradlew test
```

## 스크립트

- `scripts/all-start.sh`: 빌드 후 백그라운드 실행
- `scripts/all-stop.sh`: PID 기반 중지
- `scripts/all-restart.sh`: 재시작

## 검증 방법

1. `./scripts/all-start.sh` 실행
2. `GET /api/seasons/season-2026-spring/leaderboard?limit=5` 호출
3. `POST /api/seasons/season-2026-spring/scores`로 새 점수 등록
4. 동일 플레이어 상세/주변 순위 API로 반영 결과 확인
