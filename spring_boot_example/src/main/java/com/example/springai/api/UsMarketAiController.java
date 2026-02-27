package com.example.springai.api;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.springai.market.FinvizQuoteService;
import com.example.springai.market.FinvizQuoteService.FinvizQuoteSnapshot;

@RestController
@RequestMapping("/api/ai/us-market")
public class UsMarketAiController {

	private static final String DISCLAIMER = "면책: 본 응답은 교육/정보 제공 목적이며 투자 자문이 아닙니다.";
	private static final Set<String> MARKET_SESSIONS = Set.of("PREMARKET", "REGULAR", "AFTER_HOURS");
	private static final Set<String> POSITION_SIDES = Set.of("LONG", "SHORT", "NEUTRAL");

	private final ChatClient chatClient;
	private final FinvizQuoteService finvizQuoteService;

	public UsMarketAiController(ChatClient.Builder chatClientBuilder, FinvizQuoteService finvizQuoteService) {
		this.chatClient = chatClientBuilder.build();
		this.finvizQuoteService = finvizQuoteService;
	}

	@PostMapping("/daily-brief")
	public DailyBriefResponse dailyBrief(@RequestBody DailyBriefRequest request) {
		if (request == null) {
			throw badRequest("요청 본문이 필요합니다.");
		}

		String asOfDate = normalizeDate(request.asOfDate(), "asOfDate");
		String marketSession = normalizeMarketSession(request.marketSession());
		String investorStyle = normalizeDefaultedValue(request.investorStyle(), "BALANCED");
		List<String> headlines = sanitizeStringList(request.headlines(), "headlines");
		List<String> watchlist = sanitizeTickers(request.watchlist(), "watchlist");
		List<String> indices = sanitizeIndices(request.indices());

		String analysis = chatClient.prompt()
				.system("""
					당신은 미국 증시 데일리 브리핑을 작성하는 시니어 시장 애널리스트다.
					답변은 한국어 마크다운으로 제공한다.
					주어진 정보만으로 분석하고 모르는 데이터는 추정하지 않는다.
					확정 수익, 과도한 매수/매도 권유 표현은 사용하지 않는다.
					""")
				.user("""
					기준일: %s
					세션: %s
					투자 성향: %s

					지수 스냅샷:
					%s

					헤드라인:
					%s

					관심 종목:
					%s

					출력 형식:
					## 시장 한줄 요약
					(1~2문장)

					## 핵심 드라이버 3가지
					- ...
					- ...
					- ...

					## 워치리스트 관찰 포인트
					- 각 티커별로 `강세 조건 / 약세 조건 / 확인 지표`를 한 줄씩

					## 오늘의 리스크 체크
					- 리스크 2개
					- 대응 아이디어 2개

					추가 규칙:
					- 반드시 "실시간 시세 미조회" 문장을 포함
					- 투자 자문처럼 보이는 단정 문장 금지
					""".formatted(
						asOfDate,
						marketSession,
						investorStyle,
						toBulletList(indices),
						toBulletList(headlines),
						toBulletList(watchlist)))
				.call()
				.content();

		return new DailyBriefResponse(analysis, DISCLAIMER);
	}

	@PostMapping("/watchlist-plan")
	public WatchlistPlanResponse watchlistPlan(@RequestBody WatchlistPlanRequest request) {
		if (request == null) {
			throw badRequest("요청 본문이 필요합니다.");
		}

		String strategyName = normalizeDefaultedValue(request.strategyName(), "US Market Swing Playbook");
		String riskBudgetRule = requireNotBlank(request.riskBudgetRule(), "riskBudgetRule");
		int maxOpenPositions = clamp(request.maxOpenPositions(), 3, 1, 10);
		List<String> macroAssumptions = sanitizeStringList(request.macroAssumptions(), "macroAssumptions");
		List<String> tickerIdeas = sanitizeTickerIdeas(request.tickerIdeas());

		String planJson = chatClient.prompt()
				.system("""
					당신은 미국 주식 워치리스트 실행 계획을 설계하는 애널리스트다.
					코드블록 없이 JSON 본문만 반환한다.
					""")
				.user("""
					전략명: %s
					동시 보유 최대 포지션: %d
					리스크 예산 규칙: %s

					거시 가정:
					%s

					티커 아이디어:
					%s

					아래 JSON 스키마로만 응답하세요.
					{
					  "strategyName": "string",
					  "capitalPolicy": {
					    "riskBudgetRule": "string",
					    "maxOpenPositions": 0
					  },
					  "playbook": [
					    {
					      "ticker": "string",
					      "thesis": "string",
					      "bullCase": "string",
					      "bearCase": "string",
					      "entryChecklist": ["string"],
					      "invalidationRule": "string",
					      "takeProfitPlan": "string",
					      "positionSizingHint": "string"
					    }
					  ],
					  "monitoring": {
					    "macroChecks": ["string"],
					    "weeklyReviewQuestions": ["string"]
					  }
					}

					규칙:
					- playbook 개수는 입력 티커 개수와 동일
					- ticker는 대문자 유지
					- 가격은 단일값 대신 범위로 표현
					- 모든 설명은 한국어
					""".formatted(
						strategyName,
						maxOpenPositions,
						riskBudgetRule,
						toBulletList(macroAssumptions),
						toBulletList(tickerIdeas)))
				.call()
				.content();

		return new WatchlistPlanResponse(planJson, DISCLAIMER);
	}

	@PostMapping("/earnings-scenario")
	public EarningsScenarioResponse earningsScenario(@RequestBody EarningsScenarioRequest request) {
		if (request == null) {
			throw badRequest("요청 본문이 필요합니다.");
		}

		String ticker = normalizeTicker(requireNotBlank(request.ticker(), "ticker"));
		String positionSide = normalizePositionSide(request.positionSide());
		int holdingDays = clamp(request.holdingDays(), 5, 1, 30);
		String eventNote = normalizeDefaultedValue(request.eventNote(), "실적 발표 이벤트");
		List<String> bullishSignals = sanitizeStringList(request.bullishSignals(), "bullishSignals");
		List<String> bearishSignals = sanitizeStringList(request.bearishSignals(), "bearishSignals");

		String scenarioMarkdown = chatClient.prompt()
				.system("""
					당신은 미국 증시 이벤트 드리븐 전략을 분석하는 리서치 애널리스트다.
					답변은 한국어 마크다운으로 작성한다.
					투자 자문처럼 단정하지 말고 시나리오 기반으로 정리한다.
					""")
				.user("""
					티커: %s
					포지션 방향: %s
					보유 예정 기간(거래일): %d
					이벤트 메모: %s

					강세 시그널:
					%s

					약세 시그널:
					%s

					출력 형식:
					## 이벤트 전 체크리스트
					- 4개 항목

					## 시나리오 매트릭스
					| 시나리오 | 촉발 조건 | 예상 반응(정성) | 행동 계획 |
					|---|---|---|---|
					| 상향 | ... | ... | ... |
					| 중립 | ... | ... | ... |
					| 하향 | ... | ... | ... |

					## 손실 제한 규칙
					- 3개 항목

					## 사후 복기 질문
					- 3개 질문

					추가 규칙:
					- 실시간 시세를 조회하지 않았다는 점을 명시
					- 확정적 수익 표현 금지
					""".formatted(
						ticker,
						positionSide,
						holdingDays,
						eventNote,
						toBulletList(bullishSignals),
						toBulletList(bearishSignals)))
				.call()
				.content();

		return new EarningsScenarioResponse(scenarioMarkdown, DISCLAIMER);
	}

	@PostMapping("/finviz-brief")
	public FinvizBriefResponse finvizBrief(@RequestBody FinvizBriefRequest request) {
		if (request == null) {
			throw badRequest("요청 본문이 필요합니다.");
		}

		int newsLimit = clamp(request.newsLimit(), 5, 1, 12);
		String focus = normalizeDefaultedValue(request.focus(), "가격 흐름, 밸류에이션, 뉴스 촉매 균형");

		FinvizQuoteSnapshot snapshot = finvizQuoteService.fetchQuote(
				request.url(),
				request.ticker(),
				request.period(),
				newsLimit);

		String metricText = snapshot.metricsAsBulletText().isBlank()
				? "- 지표 데이터 없음"
				: snapshot.metricsAsBulletText();
		String newsText = snapshot.newsAsBulletText().isBlank()
				? "- 뉴스 데이터 없음"
				: snapshot.newsAsBulletText();

		String analysis = chatClient.prompt()
				.system("""
					당신은 미국 증시 종목 브리핑을 작성하는 시장 애널리스트다.
					반드시 한국어 마크다운으로 작성하고, 제공된 데이터만 사용한다.
					단정적인 매수/매도 지시 문장은 금지한다.
					""")
				.user("""
					분석 초점: %s
					데이터 소스: Finviz quote page
					URL: %s

					종목 정보:
					- ticker: %s
					- company: %s
					- quoteTime: %s
					- sector: %s
					- industry: %s
					- country/exchange: %s / %s

					핵심 지표:
					%s

					최근 뉴스:
					%s

					출력 형식:
					## 한줄 요약
					(1~2문장)

					## 지표 해석
					- 가격/변동률/거래량
					- 밸류에이션/펀더멘털
					- 모멘텀(SMA/RSI)

					## 단기 시나리오
					- 상방 트리거 2개
					- 하방 리스크 2개

					## 체크리스트
					- 다음 세션에서 확인할 포인트 3개

					추가 규칙:
					- "실시간 시세 미조회" 문장을 반드시 포함
					- Finviz 스냅샷 시점 기반이라는 점을 명시
					""".formatted(
						focus,
						snapshot.sourceUrl(),
						snapshot.ticker(),
						snapshot.company(),
						snapshot.quoteTime(),
						snapshot.sector(),
						snapshot.industry(),
						snapshot.country(),
						snapshot.exchange(),
						metricText,
						newsText))
				.call()
				.content();

		return new FinvizBriefResponse(snapshot, analysis, DISCLAIMER);
	}

	private List<String> sanitizeIndices(List<IndexSnapshot> indices) {
		if (indices == null || indices.isEmpty()) {
			throw badRequest("indices 값은 최소 1개 이상 필요합니다.");
		}

		return indices.stream()
				.map(this::normalizeIndexSnapshot)
				.collect(Collectors.toList());
	}

	private String normalizeIndexSnapshot(IndexSnapshot snapshot) {
		if (snapshot == null) {
			throw badRequest("indices 항목은 null일 수 없습니다.");
		}

		String index = requireNotBlank(snapshot.index(), "indices.index");
		Double changePercent = snapshot.changePercent();

		if (changePercent == null) {
			throw badRequest("indices.changePercent 값은 비어 있을 수 없습니다.");
		}

		return "%s: %+.2f%%".formatted(index, changePercent);
	}

	private List<String> sanitizeTickerIdeas(List<TickerIdea> tickerIdeas) {
		if (tickerIdeas == null || tickerIdeas.isEmpty()) {
			throw badRequest("tickerIdeas 값은 최소 1개 이상 필요합니다.");
		}

		return tickerIdeas.stream()
				.map(this::normalizeTickerIdea)
				.collect(Collectors.toList());
	}

	private String normalizeTickerIdea(TickerIdea tickerIdea) {
		if (tickerIdea == null) {
			throw badRequest("tickerIdeas 항목은 null일 수 없습니다.");
		}

		String ticker = normalizeTicker(requireNotBlank(tickerIdea.ticker(), "tickerIdeas.ticker"));
		String thesis = requireNotBlank(tickerIdea.thesis(), "tickerIdeas.thesis");
		String catalyst = normalizeDefaultedValue(tickerIdea.catalyst(), "촉매 미정");

		return "%s | thesis: %s | catalyst: %s".formatted(ticker, thesis, catalyst);
	}

	private String normalizeDate(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			return LocalDate.now().toString();
		}

		try {
			return LocalDate.parse(value.trim()).toString();
		} catch (DateTimeParseException exception) {
			throw badRequest(fieldName + " 값은 yyyy-MM-dd 형식이어야 합니다.");
		}
	}

	private String normalizeMarketSession(String value) {
		String normalized = normalizeDefaultedValue(value, "PREMARKET").toUpperCase();
		if (!MARKET_SESSIONS.contains(normalized)) {
			throw badRequest("marketSession 값은 PREMARKET, REGULAR, AFTER_HOURS 중 하나여야 합니다.");
		}
		return normalized;
	}

	private String normalizePositionSide(String value) {
		String normalized = normalizeDefaultedValue(value, "LONG").toUpperCase();
		if (!POSITION_SIDES.contains(normalized)) {
			throw badRequest("positionSide 값은 LONG, SHORT, NEUTRAL 중 하나여야 합니다.");
		}
		return normalized;
	}

	private String normalizeTicker(String ticker) {
		return ticker.trim().toUpperCase();
	}

	private String normalizeDefaultedValue(String value, String defaultValue) {
		if (value == null || value.isBlank()) {
			return defaultValue;
		}
		return value.trim();
	}

	private String requireNotBlank(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw badRequest(fieldName + " 값은 비어 있을 수 없습니다.");
		}
		return value.trim();
	}

	private List<String> sanitizeStringList(List<String> values, String fieldName) {
		if (values == null) {
			throw badRequest(fieldName + " 값은 비어 있을 수 없습니다.");
		}

		List<String> normalized = values.stream()
				.filter(value -> value != null && !value.isBlank())
				.map(String::trim)
				.collect(Collectors.toList());

		if (normalized.isEmpty()) {
			throw badRequest(fieldName + " 값은 최소 1개 이상 필요합니다.");
		}
		return normalized;
	}

	private List<String> sanitizeTickers(List<String> values, String fieldName) {
		return sanitizeStringList(values, fieldName).stream()
				.map(this::normalizeTicker)
				.collect(Collectors.toList());
	}

	private int clamp(Integer value, int defaultValue, int min, int max) {
		int normalized = (value == null) ? defaultValue : value;
		if (normalized < min) {
			return min;
		}
		return Math.min(normalized, max);
	}

	private String toBulletList(List<String> values) {
		return values.stream()
				.map(value -> "- " + value)
				.collect(Collectors.joining("\n"));
	}

	private ResponseStatusException badRequest(String message) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
	}

	public record DailyBriefRequest(
			String asOfDate,
			String marketSession,
			List<IndexSnapshot> indices,
			List<String> headlines,
			List<String> watchlist,
			String investorStyle) {
	}

	public record IndexSnapshot(String index, Double changePercent) {
	}

	public record DailyBriefResponse(String analysis, String disclaimer) {
	}

	public record WatchlistPlanRequest(
			String strategyName,
			List<TickerIdea> tickerIdeas,
			String riskBudgetRule,
			Integer maxOpenPositions,
			List<String> macroAssumptions) {
	}

	public record TickerIdea(String ticker, String thesis, String catalyst) {
	}

	public record WatchlistPlanResponse(String planJson, String disclaimer) {
	}

	public record EarningsScenarioRequest(
			String ticker,
			String positionSide,
			Integer holdingDays,
			List<String> bullishSignals,
			List<String> bearishSignals,
			String eventNote) {
	}

	public record EarningsScenarioResponse(String scenarioMarkdown, String disclaimer) {
	}

	public record FinvizBriefRequest(
			String url,
			String ticker,
			String period,
			Integer newsLimit,
			String focus) {
	}

	public record FinvizBriefResponse(
			FinvizQuoteSnapshot snapshot,
			String analysis,
			String disclaimer) {
	}
}
