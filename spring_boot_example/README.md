# Spring AI Example (Gradle Kotlin DSL)

Spring Initializr로 생성한 Spring Boot 3.5 + Spring AI(OpenAI) 예제입니다.
기본 예제 API + 미국 증시 테마 API를 함께 제공합니다.

## 1) 실행 준비

필수 환경 변수:

```bash
export OPENAI_API_KEY=sk-...
```

선택 환경 변수:

```bash
export OPENAI_CHAT_MODEL=gpt-4.1-mini
export OPENAI_TEMPERATURE=0.2
export SERVER_PORT=8080
```

## 2) 실행

```bash
./gradlew bootRun
```

스크립트 실행/정지:

```bash
chmod +x scripts/start.sh scripts/stop.sh
./scripts/start.sh
./scripts/stop.sh
```

참고:
- 로그 파일: `.run/spring-ai-example.log`
- PID 파일: `.run/spring-ai-example.pid`
- 빌드 생략 실행: `NO_BUILD=1 ./scripts/start.sh`
- JVM 옵션 지정: `JAVA_OPTS="-Xms512m -Xmx1024m" ./scripts/start.sh`
- 시작 확인 시간(초) 조정: `STARTUP_CHECK_SECONDS=12 ./scripts/start.sh`

## 3) 기본 API 예제

### 3-1. 단일 질문 (`GET /api/ai/ask`)

```bash
curl --get 'http://localhost:8080/api/ai/ask' \
  --data-urlencode 'q=Spring Boot에서 @Transactional이 롤백되는 기본 조건은?'
```

### 3-2. 텍스트 요약 (`POST /api/ai/summarize`)

```bash
curl -X POST 'http://localhost:8080/api/ai/summarize' \
  -H 'Content-Type: application/json' \
  -d '{
    "text": "Spring AI는 다양한 LLM 공급자(OpenAI, Anthropic 등)를 공통 인터페이스로 연결하고, 프롬프트 템플릿, 벡터 스토어, RAG 구성 요소 등을 제공한다.",
    "maxSentences": 2
  }'
```

### 3-3. 관리자 RBAC 초안 생성 (`POST /api/ai/rbac-draft`)

```bash
curl -X POST 'http://localhost:8080/api/ai/rbac-draft' \
  -H 'Content-Type: application/json' \
  -d '{
    "serviceName": "Portal Admin",
    "roles": ["SUPER_ADMIN", "CONTENT_EDITOR", "AUDITOR"],
    "features": ["메뉴 관리", "권한 관리", "콘텐츠 관리", "감사 로그 조회"]
  }'
```

## 4) 미국 증시 테마 API 예제

모든 미국 증시 테마 API는 아래 경로를 사용합니다.

- `/api/ai/us-market/daily-brief`
- `/api/ai/us-market/watchlist-plan`
- `/api/ai/us-market/earnings-scenario`
- `/api/ai/us-market/finviz-brief`
- `/api/ai/us-market/quant/build`
- `/api/ai/us-market/quant/rebalance`

### 4-1. 장세 브리핑 (`POST /api/ai/us-market/daily-brief`)

```bash
curl -X POST 'http://localhost:8080/api/ai/us-market/daily-brief' \
  -H 'Content-Type: application/json' \
  -d '{
    "asOfDate": "2026-02-06",
    "marketSession": "PREMARKET",
    "indices": [
      {"index": "SPX", "changePercent": 0.48},
      {"index": "NDX", "changePercent": 0.75},
      {"index": "DJI", "changePercent": -0.12}
    ],
    "headlines": [
      "미국 비농업 고용 지표 발표 예정",
      "반도체 업종의 CAPEX 가이던스 상향"
    ],
    "watchlist": ["NVDA", "MSFT", "TSLA"],
    "investorStyle": "BALANCED"
  }'
```

응답:
- `analysis`: 시장 요약/드라이버/리스크/워치리스트 포인트
- `disclaimer`: 투자 자문 아님 안내

### 4-2. 워치리스트 실행 계획 (`POST /api/ai/us-market/watchlist-plan`)

```bash
curl -X POST 'http://localhost:8080/api/ai/us-market/watchlist-plan' \
  -H 'Content-Type: application/json' \
  -d '{
    "strategyName": "US Swing Tech",
    "tickerIdeas": [
      {"ticker": "NVDA", "thesis": "AI 인프라 수요 지속", "catalyst": "다음 실적 가이던스"},
      {"ticker": "AMZN", "thesis": "클라우드 수익성 개선", "catalyst": "AWS 성장률 회복"}
    ],
    "riskBudgetRule": "종목당 계좌 위험 0.6% 이내",
    "maxOpenPositions": 4,
    "macroAssumptions": [
      "정책금리 동결 기조",
      "달러 강세는 완만"
    ]
  }'
```

응답:
- `planJson`: 티커별 진입/무효화/익절/포지션 사이징이 포함된 JSON
- `disclaimer`: 투자 자문 아님 안내

### 4-3. 실적 이벤트 시나리오 (`POST /api/ai/us-market/earnings-scenario`)

```bash
curl -X POST 'http://localhost:8080/api/ai/us-market/earnings-scenario' \
  -H 'Content-Type: application/json' \
  -d '{
    "ticker": "AAPL",
    "positionSide": "LONG",
    "holdingDays": 7,
    "bullishSignals": [
      "서비스 매출 가속",
      "가이던스 상향"
    ],
    "bearishSignals": [
      "중국 판매 둔화",
      "총마진 가이던스 하향"
    ],
    "eventNote": "분기 실적 발표 전후 변동성 확대 예상"
  }'
```

응답:
- `scenarioMarkdown`: 상향/중립/하향 시나리오 매트릭스 + 사후 복기 질문
- `disclaimer`: 투자 자문 아님 안내

### 4-4. Finviz 링크 기반 종목 브리핑 (`POST /api/ai/us-market/finviz-brief`)

사용자가 준 예시 링크(PLTR)를 그대로 입력할 수 있습니다.

```bash
curl -X POST 'http://localhost:8080/api/ai/us-market/finviz-brief' \
  -H 'Content-Type: application/json' \
  -d '{
    "url": "https://finviz.com/quote.ashx?t=PLTR&p=d",
    "newsLimit": 6,
    "focus": "실적 이후 단기 변동성과 밸류에이션 해석"
  }'
```

`url` 대신 `ticker` + `period` 조합도 가능합니다.

```bash
curl -X POST 'http://localhost:8080/api/ai/us-market/finviz-brief' \
  -H 'Content-Type: application/json' \
  -d '{
    "ticker": "PLTR",
    "period": "d",
    "newsLimit": 5
  }'
```

응답:
- `snapshot`: Finviz에서 파싱한 지표/뉴스 원본
- `analysis`: AI 해석 브리핑
- `disclaimer`: 투자 자문 아님 안내

### 4-5. 퀀트 포트폴리오 구성 (`POST /api/ai/us-market/quant/build`)

```bash
curl -X POST 'http://localhost:8080/api/ai/us-market/quant/build' \
  -H 'Content-Type: application/json' \
  -d '{
    "asOfDate": "2026-02-08",
    "capital": 100000,
    "topN": 4,
    "minWeightPct": 5,
    "maxWeightPct": 35,
    "factorWeights": {
      "momentum": 0.4,
      "value": 0.2,
      "quality": 0.3,
      "lowVol": 0.1
    },
    "universe": [
      {"ticker":"AAPL","momentum3mPct":8.0,"momentum6mPct":18.0,"peRatio":28.0,"roePct":45.0,"grossMarginPct":44.0,"volatility20dPct":2.1},
      {"ticker":"MSFT","momentum3mPct":6.0,"momentum6mPct":14.0,"peRatio":32.0,"roePct":38.0,"grossMarginPct":68.0,"volatility20dPct":1.8},
      {"ticker":"NVDA","momentum3mPct":12.0,"momentum6mPct":35.0,"peRatio":40.0,"roePct":55.0,"grossMarginPct":75.0,"volatility20dPct":3.9},
      {"ticker":"JNJ","momentum3mPct":1.0,"momentum6mPct":4.0,"peRatio":16.0,"roePct":28.0,"grossMarginPct":70.0,"volatility20dPct":1.2},
      {"ticker":"XOM","momentum3mPct":-2.0,"momentum6mPct":3.0,"peRatio":11.0,"roePct":22.0,"grossMarginPct":31.0,"volatility20dPct":2.4},
      {"ticker":"TSLA","momentum3mPct":-8.0,"momentum6mPct":-5.0,"peRatio":70.0,"roePct":15.0,"grossMarginPct":19.0,"volatility20dPct":5.1}
    ]
  }'
```

응답:
- `rankedStocks`: 팩터별 z-score 및 종합점수 순위
- `targetPortfolio`: 비중/금액 기반 목표 포트폴리오
- `summary`: 집중도(HHI), 추정 1일 변동성 등

### 4-6. 퀀트 리밸런싱 주문안 (`POST /api/ai/us-market/quant/rebalance`)

```bash
curl -X POST 'http://localhost:8080/api/ai/us-market/quant/rebalance' \
  -H 'Content-Type: application/json' \
  -d '{
    "capital": 100000,
    "tradeThresholdPct": 1.0,
    "currentPositions": [
      {"ticker":"AAPL","weightPct":20},
      {"ticker":"MSFT","weightPct":15},
      {"ticker":"TSLA","weightPct":5}
    ],
    "targetPortfolio": [
      {"ticker":"AAPL","weightPct":25},
      {"ticker":"MSFT","weightPct":10},
      {"ticker":"NVDA","weightPct":20}
    ]
  }'
```

응답:
- `trades`: BUY/SELL, 비중 변화, 주문 금액
- `summary`: 총 회전율(gross turnover), 순 익스포저 변화
- `disclaimer`: 투자 자문 아님 안내

## 5) 프로젝트 구조

- `src/main/java/com/example/springai/api/AiController.java`: 기본 Spring AI 예제 API
- `src/main/java/com/example/springai/api/UsMarketAiController.java`: 미국 증시 테마 API
- `src/main/java/com/example/springai/api/QuantInvestmentController.java`: 퀀트 투자 API
- `src/main/java/com/example/springai/market/FinvizQuoteService.java`: Finviz HTML 파싱 서비스
- `src/main/java/com/example/springai/quant/QuantPortfolioService.java`: 퀀트 점수/포트폴리오/리밸런싱 엔진
- `src/main/resources/application.yml`: OpenAI/서버 설정
- `build.gradle.kts`: Gradle Kotlin DSL + Spring AI 의존성
