package com.revy.mvpbanking.fx.application;

import com.revy.mvpbanking.fx.domain.FxRate;
import com.revy.mvpbanking.fx.domain.FxRateRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class FxRateService {

    private final FxRateRepository fxRateRepository;

    public FxRateService(FxRateRepository fxRateRepository) {
        this.fxRateRepository = fxRateRepository;
    }

    public List<FxRate> getRates() {
        return fxRateRepository.findAllByOrderByEffectiveAtDesc();
    }

    public FxRate getLatestRate(String baseCurrency, String quoteCurrency) {
        return fxRateRepository.findTopByBaseCurrencyAndQuoteCurrencyOrderByEffectiveAtDesc(baseCurrency, quoteCurrency)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "FX rate not found"));
    }
}
