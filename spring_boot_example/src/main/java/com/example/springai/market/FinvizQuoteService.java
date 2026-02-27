package com.example.springai.market;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FinvizQuoteService {

	private static final String FINVIZ_BASE_URL = "https://finviz.com";
	private static final Pattern TICKER_PATTERN = Pattern.compile("[A-Z0-9.\\-]{1,10}");
	private static final Set<String> ALLOWED_PERIODS = Set.of("d", "w", "m");
	private static final List<String> PRIORITY_METRICS = List.of(
			"Price",
			"Change",
			"Prev Close",
			"Volume",
			"Avg Volume",
			"Rel Volume",
			"Market Cap",
			"P/E",
			"Forward P/E",
			"EPS (ttm)",
			"EPS next Y",
			"Sales Q/Q",
			"EPS Q/Q",
			"Gross Margin",
			"Profit Margin",
			"ROE",
			"ROA",
			"RSI (14)",
			"SMA20",
			"SMA50",
			"SMA200",
			"52W High",
			"52W Low",
			"Target Price",
			"Earnings");

	public FinvizQuoteSnapshot fetchQuote(String rawUrl, String rawTicker, String rawPeriod, int newsLimit) {
		String quoteUrl = resolveQuoteUrl(rawUrl, rawTicker, rawPeriod);
		Document document = fetchDocument(quoteUrl);

		Map<String, String> snapshotMetrics = parseSnapshotMetrics(document);
		String headerPrice = text(document.selectFirst("strong.quote-price_wrapper_price"));
		if (!headerPrice.isBlank() && !snapshotMetrics.containsKey("Price")) {
			snapshotMetrics.put("Price", headerPrice);
		}

		Map<String, String> selectedMetrics = selectPriorityMetrics(snapshotMetrics);
		if (selectedMetrics.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Finviz 데이터 파싱에 실패했습니다.");
		}

		return new FinvizQuoteSnapshot(
				quoteUrl,
				text(document.selectFirst("h1.js-recent-quote-ticker")),
				text(document.selectFirst("h2.quote-header_ticker-wrapper_company")),
				text(document.selectFirst("span.quote-price_date")),
				text(document.selectFirst("div.quote-links a.tab-link[href*=f=sec_]")),
				text(document.selectFirst("div.quote-links a.tab-link[href*=f=ind_]")),
				text(document.selectFirst("div.quote-links a.tab-link[href*=f=geo_]")),
				text(document.selectFirst("div.quote-links a.tab-link[href*=f=exch_]")),
				selectedMetrics,
				parseRecentNews(document, newsLimit));
	}

	private Document fetchDocument(String quoteUrl) {
		try {
			return Jsoup.connect(quoteUrl)
					.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36"
							+ " (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36")
					.referrer(FINVIZ_BASE_URL + "/")
					.timeout(15_000)
					.get();
		} catch (IOException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Finviz 페이지를 가져오지 못했습니다.");
		}
	}

	private String resolveQuoteUrl(String rawUrl, String rawTicker, String rawPeriod) {
		if (rawUrl != null && !rawUrl.isBlank()) {
			URI uri = parseUri(rawUrl.trim());
			validateFinvizQuoteUri(uri);
			return uri.toString();
		}

		String ticker = normalizeTicker(rawTicker);
		String period = normalizePeriod(rawPeriod);
		return FINVIZ_BASE_URL + "/quote.ashx?t=" + ticker + "&p=" + period;
	}

	private URI parseUri(String rawUrl) {
		try {
			return URI.create(rawUrl);
		} catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "url 형식이 올바르지 않습니다.");
		}
	}

	private void validateFinvizQuoteUri(URI uri) {
		String scheme = toLower(uri.getScheme());
		String host = toLower(uri.getHost());

		if (!"https".equals(scheme) && !"http".equals(scheme)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "url은 http 또는 https여야 합니다.");
		}

		if (!"finviz.com".equals(host) && !"www.finviz.com".equals(host)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "url 호스트는 finviz.com 이어야 합니다.");
		}

		if (!"/quote.ashx".equals(uri.getPath())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "url 경로는 /quote.ashx 이어야 합니다.");
		}

		Map<String, String> query = parseQueryString(uri.getRawQuery());
		if (!query.containsKey("t") || query.get("t").isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "url에는 티커 파라미터(t)가 필요합니다.");
		}
	}

	private Map<String, String> parseQueryString(String rawQuery) {
		Map<String, String> result = new LinkedHashMap<>();
		if (rawQuery == null || rawQuery.isBlank()) {
			return result;
		}

		for (String token : rawQuery.split("&")) {
			if (token.isBlank()) {
				continue;
			}

			int separator = token.indexOf('=');
			String key = separator >= 0 ? token.substring(0, separator) : token;
			String value = separator >= 0 ? token.substring(separator + 1) : "";
			result.putIfAbsent(decodeUrlPart(key), decodeUrlPart(value));
		}
		return result;
	}

	private String decodeUrlPart(String value) {
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}

	private String normalizeTicker(String rawTicker) {
		if (rawTicker == null || rawTicker.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ticker 값이 필요합니다.");
		}

		String ticker = rawTicker.trim().toUpperCase(Locale.ROOT);
		if (!TICKER_PATTERN.matcher(ticker).matches()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ticker 형식이 올바르지 않습니다.");
		}
		return ticker;
	}

	private String normalizePeriod(String rawPeriod) {
		if (rawPeriod == null || rawPeriod.isBlank()) {
			return "d";
		}

		String period = rawPeriod.trim().toLowerCase(Locale.ROOT);
		if (!ALLOWED_PERIODS.contains(period)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "period는 d, w, m 중 하나여야 합니다.");
		}
		return period;
	}

	private Map<String, String> parseSnapshotMetrics(Document document) {
		Map<String, String> metrics = new LinkedHashMap<>();
		Elements cells = document.select("table.js-snapshot-table td.snapshot-td2");

		for (int index = 0; index + 1 < cells.size(); index += 2) {
			String key = normalizeWhitespace(cells.get(index).text());
			String value = normalizeWhitespace(cells.get(index + 1).text());

			if (key.isBlank() || value.isBlank()) {
				continue;
			}
			metrics.putIfAbsent(key, value);
		}

		return metrics;
	}

	private Map<String, String> selectPriorityMetrics(Map<String, String> allMetrics) {
		Map<String, String> selected = new LinkedHashMap<>();

		for (String metricKey : PRIORITY_METRICS) {
			String value = allMetrics.get(metricKey);
			if (value != null && !value.isBlank()) {
				selected.put(metricKey, value);
			}
		}

		if (selected.size() < 8) {
			for (Map.Entry<String, String> entry : allMetrics.entrySet()) {
				selected.putIfAbsent(entry.getKey(), entry.getValue());
				if (selected.size() >= 12) {
					break;
				}
			}
		}

		return selected;
	}

	private List<FinvizNewsItem> parseRecentNews(Document document, int newsLimit) {
		List<FinvizNewsItem> newsItems = new ArrayList<>();
		Elements rows = document.select("#news-table tr");

		for (Element row : rows) {
			Element timeCell = row.selectFirst("td[align=right]");
			Element link = row.selectFirst("a.tab-link-news");
			if (timeCell == null || link == null) {
				continue;
			}

			String sourceRaw = text(row.selectFirst(".news-link-right span"));
			String source = sourceRaw.replace("(", "").replace(")", "").trim();
			String href = normalizeNewsUrl(link.attr("href"));

			newsItems.add(new FinvizNewsItem(
					normalizeWhitespace(timeCell.text()),
					source,
					normalizeWhitespace(link.text()),
					href));

			if (newsItems.size() >= newsLimit) {
				break;
			}
		}

		return newsItems;
	}

	private String normalizeNewsUrl(String href) {
		if (href == null || href.isBlank()) {
			return "";
		}

		if (href.startsWith("https://") || href.startsWith("http://")) {
			return href;
		}

		if (href.startsWith("//")) {
			return "https:" + href;
		}

		if (href.startsWith("/")) {
			return FINVIZ_BASE_URL + href;
		}

		return FINVIZ_BASE_URL + "/" + href;
	}

	private String normalizeWhitespace(String value) {
		return value.replaceAll("\\s+", " ").trim();
	}

	private String text(Element element) {
		if (element == null) {
			return "";
		}
		return normalizeWhitespace(element.text());
	}

	private String toLower(String value) {
		if (value == null) {
			return "";
		}
		return value.toLowerCase(Locale.ROOT);
	}

	public record FinvizQuoteSnapshot(
			String sourceUrl,
			String ticker,
			String company,
			String quoteTime,
			String sector,
			String industry,
			String country,
			String exchange,
			Map<String, String> metrics,
			List<FinvizNewsItem> recentNews) {

		public String metricsAsBulletText() {
			return metrics.entrySet().stream()
					.map(entry -> "- " + entry.getKey() + ": " + entry.getValue())
					.collect(Collectors.joining("\n"));
		}

		public String newsAsBulletText() {
			return recentNews.stream()
					.map(item -> "- [" + item.time + "] (" + item.source + ") " + item.headline)
					.collect(Collectors.joining("\n"));
		}
	}

	public record FinvizNewsItem(
			String time,
			String source,
			String headline,
			String url) {
	}
}
