package com.example.springai.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springai.quant.QuantPortfolioService;
import com.example.springai.quant.QuantPortfolioService.QuantBuildRequest;
import com.example.springai.quant.QuantPortfolioService.QuantBuildResponse;
import com.example.springai.quant.QuantPortfolioService.QuantRebalanceRequest;
import com.example.springai.quant.QuantPortfolioService.QuantRebalanceResponse;

@RestController
@RequestMapping("/api/ai/us-market/quant")
public class QuantInvestmentController {

	private final QuantPortfolioService quantPortfolioService;

	public QuantInvestmentController(QuantPortfolioService quantPortfolioService) {
		this.quantPortfolioService = quantPortfolioService;
	}

	@PostMapping("/build")
	public QuantBuildResponse build(@RequestBody QuantBuildRequest request) {
		return quantPortfolioService.buildPortfolio(request);
	}

	@PostMapping("/rebalance")
	public QuantRebalanceResponse rebalance(@RequestBody QuantRebalanceRequest request) {
		return quantPortfolioService.rebalance(request);
	}
}
