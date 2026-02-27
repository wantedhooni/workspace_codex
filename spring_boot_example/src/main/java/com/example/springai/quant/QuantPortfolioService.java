package com.example.springai.quant;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class QuantPortfolioService {

	private static final String DISCLAIMER = "면책: 본 결과는 교육/연구 목적이며 투자 자문이 아닙니다.";
	private static final double EPS = 1e-9;

	public QuantBuildResponse buildPortfolio(QuantBuildRequest request) {
		if (request == null) {
			throw badRequest("요청 본문이 필요합니다.");
		}

		List<StockFactorInput> universe = normalizeUniverse(request.universe());
		int topN = clamp(request.topN(), 8, 1, universe.size());
		double capital = positiveOrDefault(request.capital(), 100_000.0, "capital");
		String asOfDate = normalizeDate(request.asOfDate());

		FactorWeights normalizedWeights = normalizeWeights(request.factorWeights());
		double minWeightPct = request.minWeightPct() == null ? 3.0 : request.minWeightPct();
		double maxWeightPct = request.maxWeightPct() == null ? 25.0 : request.maxWeightPct();
		validateWeightRange(minWeightPct, maxWeightPct, topN);

		List<StockFactorInput> preparedUniverse = preprocessUniverse(universe);

		double[] momentumRaw = preparedUniverse.stream()
				.mapToDouble(stock -> average(stock.momentum3mPct(), stock.momentum6mPct()))
				.toArray();
		double[] valueRaw = preparedUniverse.stream()
				.mapToDouble(stock -> -stock.peRatio())
				.toArray();
		double[] qualityRaw = preparedUniverse.stream()
				.mapToDouble(stock -> average(stock.roePct(), stock.grossMarginPct()))
				.toArray();
		double[] lowVolRaw = preparedUniverse.stream()
				.mapToDouble(stock -> -stock.volatility20dPct())
				.toArray();

		double[] momentumZ = zScore(momentumRaw);
		double[] valueZ = zScore(valueRaw);
		double[] qualityZ = zScore(qualityRaw);
		double[] lowVolZ = zScore(lowVolRaw);

		List<RankedStock> ranked = new ArrayList<>();
		for (int index = 0; index < preparedUniverse.size(); index++) {
			double score = normalizedWeights.momentum() * momentumZ[index]
					+ normalizedWeights.value() * valueZ[index]
					+ normalizedWeights.quality() * qualityZ[index]
					+ normalizedWeights.lowVol() * lowVolZ[index];

			ranked.add(new RankedStock(
					preparedUniverse.get(index).ticker(),
					0,
					score,
					momentumZ[index],
					valueZ[index],
					qualityZ[index],
					lowVolZ[index],
					preparedUniverse.get(index)));
		}

		ranked.sort(Comparator
				.comparingDouble(RankedStock::compositeScore).reversed()
				.thenComparing(RankedStock::ticker));

		List<RankedStock> rankedWithRank = new ArrayList<>();
		for (int i = 0; i < ranked.size(); i++) {
			RankedStock stock = ranked.get(i);
			rankedWithRank.add(new RankedStock(
					stock.ticker(),
					i + 1,
					stock.compositeScore(),
					stock.momentumFactor(),
					stock.valueFactor(),
					stock.qualityFactor(),
					stock.lowVolFactor(),
					stock.raw()));
		}

		List<RankedStock> selected = rankedWithRank.subList(0, topN);
		double[] allocationWeights = allocateWeights(selected, minWeightPct / 100.0, maxWeightPct / 100.0);

		List<PortfolioAllocation> targetPortfolio = new ArrayList<>();
		for (int i = 0; i < selected.size(); i++) {
			RankedStock stock = selected.get(i);
			double weight = allocationWeights[i];
			targetPortfolio.add(new PortfolioAllocation(
					stock.ticker(),
					round(weight * 100.0, 4),
					round(capital * weight, 2),
					round(stock.compositeScore(), 6)));
		}

		PortfolioSummary summary = buildSummary(preparedUniverse.size(), targetPortfolio, selected);
		return new QuantBuildResponse(
				asOfDate,
				round(capital, 2),
				normalizedWeights,
				rankedWithRank,
				targetPortfolio,
				summary,
				DISCLAIMER);
	}

	public QuantRebalanceResponse rebalance(QuantRebalanceRequest request) {
		if (request == null) {
			throw badRequest("요청 본문이 필요합니다.");
		}

		double capital = positiveOrDefault(request.capital(), 100_000.0, "capital");
		double thresholdPct = request.tradeThresholdPct() == null ? 0.5 : request.tradeThresholdPct();
		if (thresholdPct < 0.0) {
			throw badRequest("tradeThresholdPct는 0 이상이어야 합니다.");
		}

		Map<String, Double> current = toWeightMap(request.currentPositions(), "currentPositions");
		Map<String, Double> target = toWeightMap(request.targetPortfolio(), "targetPortfolio");
		if (target.isEmpty()) {
			throw badRequest("targetPortfolio는 최소 1개 이상 필요합니다.");
		}

		double currentTotal = current.values().stream().mapToDouble(Double::doubleValue).sum();
		double targetTotal = target.values().stream().mapToDouble(Double::doubleValue).sum();
		if (currentTotal > 100.0 + EPS || targetTotal > 100.0 + EPS) {
			throw badRequest("포트폴리오 가중치 합은 100%를 초과할 수 없습니다.");
		}

		Set<String> tickers = new java.util.TreeSet<>();
		tickers.addAll(current.keySet());
		tickers.addAll(target.keySet());

		List<TradeInstruction> trades = new ArrayList<>();
		for (String ticker : tickers) {
			double currentWeight = current.getOrDefault(ticker, 0.0);
			double targetWeight = target.getOrDefault(ticker, 0.0);
			double delta = targetWeight - currentWeight;

			if (Math.abs(delta) < thresholdPct) {
				continue;
			}

			trades.add(new TradeInstruction(
					ticker,
					delta > 0 ? "BUY" : "SELL",
					round(currentWeight, 4),
					round(targetWeight, 4),
					round(delta, 4),
					round(capital * delta / 100.0, 2)));
		}

		double grossTurnover = trades.stream().mapToDouble(trade -> Math.abs(trade.deltaWeightPct())).sum();
		RebalanceSummary summary = new RebalanceSummary(
				trades.size(),
				round(grossTurnover, 4),
				round(targetTotal - currentTotal, 4),
				round(currentTotal, 4),
				round(targetTotal, 4));

		return new QuantRebalanceResponse(
				round(capital, 2),
				trades,
				summary,
				DISCLAIMER);
	}

	private PortfolioSummary buildSummary(int universeSize, List<PortfolioAllocation> portfolio, List<RankedStock> selected) {
		double topWeight = portfolio.stream().mapToDouble(PortfolioAllocation::weightPct).max().orElse(0.0);
		double hhi = portfolio.stream()
				.mapToDouble(item -> {
					double weight = item.weightPct() / 100.0;
					return weight * weight;
				})
				.sum();

		Map<String, Double> volatilityByTicker = new HashMap<>();
		for (RankedStock stock : selected) {
			volatilityByTicker.put(stock.ticker(), stock.raw().volatility20dPct());
		}

		double varianceApprox = portfolio.stream()
				.mapToDouble(item -> {
					double weight = item.weightPct() / 100.0;
					double volPct = volatilityByTicker.getOrDefault(item.ticker(), 0.0);
					double volDecimal = volPct / 100.0;
					return Math.pow(weight * volDecimal, 2);
				})
				.sum();
		double estimatedVolPct = Math.sqrt(varianceApprox) * 100.0;

		return new PortfolioSummary(
				universeSize,
				portfolio.size(),
				round(topWeight, 4),
				round(hhi, 6),
				round(estimatedVolPct, 4));
	}

	private List<StockFactorInput> preprocessUniverse(List<StockFactorInput> universe) {
		List<StockFactorInput> processed = new ArrayList<>();
		for (StockFactorInput stock : universe) {
			double pe = stock.peRatio();
			if (pe <= 0.0) {
				pe = 120.0;
			}
			processed.add(new StockFactorInput(
					normalizeTicker(stock.ticker()),
					stock.momentum3mPct(),
					stock.momentum6mPct(),
					pe,
					stock.roePct(),
					stock.grossMarginPct(),
					stock.volatility20dPct()));
		}
		return processed;
	}

	private double[] allocateWeights(List<RankedStock> selected, double minWeight, double maxWeight) {
		int count = selected.size();
		double minScore = selected.stream().mapToDouble(RankedStock::compositeScore).min().orElse(0.0);

		double[] base = new double[count];
		double sum = 0.0;
		for (int i = 0; i < count; i++) {
			double shifted = selected.get(i).compositeScore() - minScore + 0.05;
			base[i] = Math.max(shifted, 0.0001);
			sum += base[i];
		}
		for (int i = 0; i < count; i++) {
			base[i] = base[i] / sum;
		}

		double[] weights = new double[count];
		boolean[] fixed = new boolean[count];
		double remaining = 1.0;
		int freeCount = count;

		while (true) {
			double baseFreeSum = 0.0;
			for (int i = 0; i < count; i++) {
				if (!fixed[i]) {
					baseFreeSum += base[i];
				}
			}
			if (baseFreeSum <= EPS) {
				double equal = remaining / freeCount;
				for (int i = 0; i < count; i++) {
					if (!fixed[i]) {
						weights[i] = equal;
					}
				}
			} else {
				for (int i = 0; i < count; i++) {
					if (!fixed[i]) {
						weights[i] = remaining * (base[i] / baseFreeSum);
					}
				}
			}

			boolean changed = false;
			for (int i = 0; i < count; i++) {
				if (fixed[i]) {
					continue;
				}
				if (weights[i] < minWeight - EPS) {
					weights[i] = minWeight;
					fixed[i] = true;
					remaining -= minWeight;
					freeCount--;
					changed = true;
				} else if (weights[i] > maxWeight + EPS) {
					weights[i] = maxWeight;
					fixed[i] = true;
					remaining -= maxWeight;
					freeCount--;
					changed = true;
				}
			}

			if (!changed) {
				break;
			}
			if (freeCount <= 0) {
				break;
			}
		}

		double total = 0.0;
		for (double weight : weights) {
			total += weight;
		}
		if (Math.abs(total - 1.0) > 1e-6) {
			for (int i = 0; i < weights.length; i++) {
				weights[i] = weights[i] / total;
			}
		}
		return weights;
	}

	private void validateWeightRange(double minWeightPct, double maxWeightPct, int topN) {
		if (minWeightPct < 0.0 || maxWeightPct <= 0.0 || minWeightPct > maxWeightPct) {
			throw badRequest("minWeightPct와 maxWeightPct 범위가 올바르지 않습니다.");
		}
		if (minWeightPct * topN > 100.0 + EPS) {
			throw badRequest("minWeightPct * topN 값이 100%를 초과합니다.");
		}
		if (maxWeightPct * topN < 100.0 - EPS) {
			throw badRequest("maxWeightPct * topN 값이 100%보다 작아 비중을 채울 수 없습니다.");
		}
	}

	private List<StockFactorInput> normalizeUniverse(List<StockFactorInput> universe) {
		if (universe == null || universe.size() < 3) {
			throw badRequest("universe는 최소 3개 이상 필요합니다.");
		}

		Map<String, StockFactorInput> dedup = new LinkedHashMap<>();
		for (StockFactorInput stock : universe) {
			if (stock == null) {
				throw badRequest("universe 항목에 null이 포함될 수 없습니다.");
			}
			String ticker = normalizeTicker(stock.ticker());
			requireFinite(stock.momentum3mPct(), "momentum3mPct");
			requireFinite(stock.momentum6mPct(), "momentum6mPct");
			requireFinite(stock.peRatio(), "peRatio");
			requireFinite(stock.roePct(), "roePct");
			requireFinite(stock.grossMarginPct(), "grossMarginPct");
			requireFinite(stock.volatility20dPct(), "volatility20dPct");
			if (stock.volatility20dPct() <= 0.0) {
				throw badRequest("volatility20dPct는 0보다 커야 합니다. ticker=" + ticker);
			}

			dedup.put(ticker, new StockFactorInput(
					ticker,
					stock.momentum3mPct(),
					stock.momentum6mPct(),
					stock.peRatio(),
					stock.roePct(),
					stock.grossMarginPct(),
					stock.volatility20dPct()));
		}
		return new ArrayList<>(dedup.values());
	}

	private void requireFinite(Double value, String fieldName) {
		if (value == null || !Double.isFinite(value)) {
			throw badRequest(fieldName + " 값이 올바르지 않습니다.");
		}
	}

	private Map<String, Double> toWeightMap(List<PositionWeight> positions, String fieldName) {
		Map<String, Double> result = new LinkedHashMap<>();
		if (positions == null) {
			return result;
		}

		for (PositionWeight position : positions) {
			if (position == null) {
				throw badRequest(fieldName + " 항목에 null이 포함될 수 없습니다.");
			}
			String ticker = normalizeTicker(position.ticker());
			Double weight = position.weightPct();
			if (weight == null || !Double.isFinite(weight) || weight < 0.0) {
				throw badRequest(fieldName + ".weightPct 값이 올바르지 않습니다.");
			}
			result.merge(ticker, weight, Double::sum);
		}
		return result;
	}

	private FactorWeights normalizeWeights(FactorWeights input) {
		FactorWeights defaults = new FactorWeights(0.35, 0.25, 0.25, 0.15);
		FactorWeights source = input == null ? defaults : input;

		double momentum = nonNegativeOrDefault(source.momentum(), defaults.momentum(), "factorWeights.momentum");
		double value = nonNegativeOrDefault(source.value(), defaults.value(), "factorWeights.value");
		double quality = nonNegativeOrDefault(source.quality(), defaults.quality(), "factorWeights.quality");
		double lowVol = nonNegativeOrDefault(source.lowVol(), defaults.lowVol(), "factorWeights.lowVol");

		double sum = momentum + value + quality + lowVol;
		if (sum <= EPS) {
			throw badRequest("factorWeights 합계는 0보다 커야 합니다.");
		}

		return new FactorWeights(
				round(momentum / sum, 6),
				round(value / sum, 6),
				round(quality / sum, 6),
				round(lowVol / sum, 6));
	}

	private double positiveOrDefault(Double value, double defaultValue, String fieldName) {
		if (value == null) {
			return defaultValue;
		}
		if (!Double.isFinite(value) || value <= 0.0) {
			throw badRequest(fieldName + " 값은 0보다 커야 합니다.");
		}
		return value;
	}

	private double nonNegativeOrDefault(Double value, double defaultValue, String fieldName) {
		if (value == null) {
			return defaultValue;
		}
		if (!Double.isFinite(value) || value < 0.0) {
			throw badRequest(fieldName + " 값은 0 이상이어야 합니다.");
		}
		return value;
	}

	private String normalizeTicker(String ticker) {
		if (ticker == null || ticker.isBlank()) {
			throw badRequest("ticker 값은 비어 있을 수 없습니다.");
		}
		String normalized = ticker.trim().toUpperCase(Locale.ROOT);
		if (!normalized.matches("[A-Z0-9.\\-]{1,10}")) {
			throw badRequest("ticker 형식이 올바르지 않습니다: " + ticker);
		}
		return normalized;
	}

	private String normalizeDate(String value) {
		if (value == null || value.isBlank()) {
			return LocalDate.now().toString();
		}

		try {
			return LocalDate.parse(value.trim()).toString();
		} catch (DateTimeParseException exception) {
			throw badRequest("asOfDate 값은 yyyy-MM-dd 형식이어야 합니다.");
		}
	}

	private int clamp(Integer value, int defaultValue, int min, int max) {
		int normalized = value == null ? defaultValue : value;
		if (normalized < min) {
			return min;
		}
		return Math.min(normalized, max);
	}

	private double average(Double left, Double right) {
		return (Objects.requireNonNull(left) + Objects.requireNonNull(right)) / 2.0;
	}

	private double[] zScore(double[] values) {
		double mean = 0.0;
		for (double value : values) {
			mean += value;
		}
		mean = mean / values.length;

		double variance = 0.0;
		for (double value : values) {
			double diff = value - mean;
			variance += diff * diff;
		}
		variance = variance / values.length;
		double std = Math.sqrt(variance);

		double[] result = new double[values.length];
		if (std <= EPS) {
			return result;
		}

		for (int i = 0; i < values.length; i++) {
			result[i] = (values[i] - mean) / std;
		}
		return result;
	}

	private double round(double value, int scale) {
		double factor = Math.pow(10, scale);
		return Math.round(value * factor) / factor;
	}

	private ResponseStatusException badRequest(String message) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
	}

	public static record QuantBuildRequest(
			String asOfDate,
			Double capital,
			Integer topN,
			Double minWeightPct,
			Double maxWeightPct,
			FactorWeights factorWeights,
			List<StockFactorInput> universe) {
	}

	public static record FactorWeights(
			Double momentum,
			Double value,
			Double quality,
			Double lowVol) {
	}

	public static record StockFactorInput(
			String ticker,
			Double momentum3mPct,
			Double momentum6mPct,
			Double peRatio,
			Double roePct,
			Double grossMarginPct,
			Double volatility20dPct) {
	}

	public static record RankedStock(
			String ticker,
			Integer rank,
			Double compositeScore,
			Double momentumFactor,
			Double valueFactor,
			Double qualityFactor,
			Double lowVolFactor,
			StockFactorInput raw) {
	}

	public static record PortfolioAllocation(
			String ticker,
			Double weightPct,
			Double notionalUsd,
			Double score) {
	}

	public static record PortfolioSummary(
			Integer universeSize,
			Integer selectedCount,
			Double topWeightPct,
			Double hhi,
			Double estimatedOneDayVolPct) {
	}

	public static record QuantBuildResponse(
			String asOfDate,
			Double capital,
			FactorWeights normalizedFactorWeights,
			List<RankedStock> rankedStocks,
			List<PortfolioAllocation> targetPortfolio,
			PortfolioSummary summary,
			String disclaimer) {
	}

	public static record PositionWeight(
			String ticker,
			Double weightPct) {
	}

	public static record QuantRebalanceRequest(
			Double capital,
			List<PositionWeight> currentPositions,
			List<PositionWeight> targetPortfolio,
			Double tradeThresholdPct) {
	}

	public static record TradeInstruction(
			String ticker,
			String action,
			Double currentWeightPct,
			Double targetWeightPct,
			Double deltaWeightPct,
			Double deltaNotionalUsd) {
	}

	public static record RebalanceSummary(
			Integer tradeCount,
			Double grossTurnoverPct,
			Double netExposureChangePct,
			Double currentTotalWeightPct,
			Double targetTotalWeightPct) {
	}

	public static record QuantRebalanceResponse(
			Double capital,
			List<TradeInstruction> trades,
			RebalanceSummary summary,
			String disclaimer) {
	}
}
