package com.example.springai.quant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.example.springai.quant.QuantPortfolioService.FactorWeights;
import com.example.springai.quant.QuantPortfolioService.PortfolioAllocation;
import com.example.springai.quant.QuantPortfolioService.PositionWeight;
import com.example.springai.quant.QuantPortfolioService.QuantBuildRequest;
import com.example.springai.quant.QuantPortfolioService.QuantBuildResponse;
import com.example.springai.quant.QuantPortfolioService.QuantRebalanceRequest;
import com.example.springai.quant.QuantPortfolioService.QuantRebalanceResponse;
import com.example.springai.quant.QuantPortfolioService.StockFactorInput;
import com.example.springai.quant.QuantPortfolioService.TradeInstruction;

class QuantPortfolioServiceTests {

	private final QuantPortfolioService service = new QuantPortfolioService();

	@Test
	void buildPortfolio_shouldCreateRankAndTargetWeights() {
		QuantBuildRequest request = new QuantBuildRequest(
				"2026-02-08",
				100_000.0,
				4,
				5.0,
				40.0,
				new FactorWeights(0.4, 0.2, 0.3, 0.1),
				List.of(
						new StockFactorInput("AAPL", 8.0, 18.0, 28.0, 45.0, 44.0, 2.1),
						new StockFactorInput("MSFT", 6.0, 14.0, 32.0, 38.0, 68.0, 1.8),
						new StockFactorInput("NVDA", 12.0, 35.0, 40.0, 55.0, 75.0, 3.9),
						new StockFactorInput("JNJ", 1.0, 4.0, 16.0, 28.0, 70.0, 1.2),
						new StockFactorInput("XOM", -2.0, 3.0, 11.0, 22.0, 31.0, 2.4),
						new StockFactorInput("TSLA", -8.0, -5.0, 70.0, 15.0, 19.0, 5.1)));

		QuantBuildResponse response = service.buildPortfolio(request);

		assertNotNull(response);
		assertEquals(6, response.rankedStocks().size());
		assertEquals(4, response.targetPortfolio().size());

		double weightSum = response.targetPortfolio().stream()
				.mapToDouble(PortfolioAllocation::weightPct)
				.sum();
		assertEquals(100.0, weightSum, 0.01);

		double notionalSum = response.targetPortfolio().stream()
				.mapToDouble(PortfolioAllocation::notionalUsd)
				.sum();
		assertEquals(response.capital(), notionalSum, 5.0);

		for (int i = 0; i < response.rankedStocks().size() - 1; i++) {
			double current = response.rankedStocks().get(i).compositeScore();
			double next = response.rankedStocks().get(i + 1).compositeScore();
			assertTrue(current >= next, "rankedStocks는 compositeScore 내림차순이어야 합니다.");
		}
	}

	@Test
	void rebalance_shouldGenerateExpectedTrades() {
		QuantRebalanceRequest request = new QuantRebalanceRequest(
				100_000.0,
				List.of(
						new PositionWeight("AAPL", 20.0),
						new PositionWeight("MSFT", 15.0),
						new PositionWeight("TSLA", 5.0)),
				List.of(
						new PositionWeight("AAPL", 25.0),
						new PositionWeight("MSFT", 10.0),
						new PositionWeight("NVDA", 20.0)),
				1.0);

		QuantRebalanceResponse response = service.rebalance(request);
		assertNotNull(response);
		assertEquals(4, response.trades().size());
		assertEquals(35.0, response.summary().grossTurnoverPct(), 0.001);
		assertEquals(15.0, response.summary().netExposureChangePct(), 0.001);

		Map<String, TradeInstruction> tradeByTicker = response.trades().stream()
				.collect(Collectors.toMap(TradeInstruction::ticker, Function.identity()));

		assertEquals("BUY", tradeByTicker.get("AAPL").action());
		assertEquals(5.0, tradeByTicker.get("AAPL").deltaWeightPct(), 0.001);

		assertEquals("SELL", tradeByTicker.get("MSFT").action());
		assertEquals(-5.0, tradeByTicker.get("MSFT").deltaWeightPct(), 0.001);

		assertEquals("BUY", tradeByTicker.get("NVDA").action());
		assertEquals(20.0, tradeByTicker.get("NVDA").deltaWeightPct(), 0.001);

		assertEquals("SELL", tradeByTicker.get("TSLA").action());
		assertEquals(-5.0, tradeByTicker.get("TSLA").deltaWeightPct(), 0.001);
	}
}
