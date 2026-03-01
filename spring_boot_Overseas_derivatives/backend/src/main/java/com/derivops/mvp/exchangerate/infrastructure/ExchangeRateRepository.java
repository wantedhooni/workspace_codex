package com.derivops.mvp.exchangerate.infrastructure;

import com.derivops.mvp.exchangerate.ExchangeRate;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long>, JpaSpecificationExecutor<ExchangeRate> {

    Optional<ExchangeRate> findByFromCurrencyAndToCurrencyAndRateDate(String fromCurrency, String toCurrency, LocalDate rateDate);

    Optional<ExchangeRate> findTopByFromCurrencyAndToCurrencyAndRateDateLessThanEqualOrderByRateDateDescIdDesc(
            String fromCurrency,
            String toCurrency,
            LocalDate rateDate
    );

    Optional<ExchangeRate> findTopByFromCurrencyAndToCurrencyOrderByRateDateDescIdDesc(String fromCurrency, String toCurrency);
}
