package com.derivops.mvp.exchangerate.application;

import com.derivops.mvp.common.BadRequestException;
import com.derivops.mvp.common.NotFoundException;
import com.derivops.mvp.exchangerate.ExchangeRate;
import com.derivops.mvp.exchangerate.dto.ExchangeRateQuoteResponse;
import com.derivops.mvp.exchangerate.dto.ExchangeRateResponse;
import com.derivops.mvp.exchangerate.dto.UpsertExchangeRateRequest;
import com.derivops.mvp.exchangerate.infrastructure.ExchangeRateRepository;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ExchangeRateService {

    private static final int RATE_SCALE = 8;
    private static final int AMOUNT_SCALE = 4;

    private final ExchangeRateRepository exchangeRateRepository;

    @Transactional(readOnly = true)
    public Page<ExchangeRateResponse> list(String fromCurrency, String toCurrency, LocalDate rateDate, Pageable pageable) {
        Specification<ExchangeRate> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (fromCurrency != null && !fromCurrency.isBlank()) {
                predicates.add(cb.equal(root.get("fromCurrency"), normalizeCurrency(fromCurrency)));
            }
            if (toCurrency != null && !toCurrency.isBlank()) {
                predicates.add(cb.equal(root.get("toCurrency"), normalizeCurrency(toCurrency)));
            }
            if (rateDate != null) {
                predicates.add(cb.equal(root.get("rateDate"), rateDate));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Pageable sortedPageable = pageable.getSort().isSorted()
                ? pageable
                : org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.desc("rateDate"), Sort.Order.asc("fromCurrency"), Sort.Order.asc("toCurrency"), Sort.Order.desc("id"))
        );

        return exchangeRateRepository.findAll(spec, sortedPageable).map(this::toResponse);
    }

    @Transactional
    public ExchangeRateResponse upsert(UpsertExchangeRateRequest request) {
        String fromCurrency = normalizeCurrency(request.fromCurrency());
        String toCurrency = normalizeCurrency(request.toCurrency());
        if (fromCurrency.equals(toCurrency)) {
            throw new BadRequestException("fromCurrency and toCurrency must be different");
        }

        ExchangeRate entity = exchangeRateRepository.findByFromCurrencyAndToCurrencyAndRateDate(fromCurrency, toCurrency, request.rateDate())
                .orElseGet(ExchangeRate::new);
        entity.setFromCurrency(fromCurrency);
        entity.setToCurrency(toCurrency);
        entity.setRateDate(request.rateDate());
        entity.setRate(request.rate().setScale(RATE_SCALE, RoundingMode.HALF_UP));
        entity.setSource(request.source().trim());
        return toResponse(exchangeRateRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public ExchangeRateQuoteResponse quote(String fromCurrency, String toCurrency, BigDecimal amount, LocalDate rateDate) {
        String normalizedFrom = normalizeCurrency(fromCurrency);
        String normalizedTo = normalizeCurrency(toCurrency);
        if (normalizedFrom.equals(normalizedTo)) {
            throw new BadRequestException("fromCurrency and toCurrency must be different");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new BadRequestException("amount must be positive");
        }

        LocalDate targetDate = rateDate == null ? LocalDate.now() : rateDate;
        Optional<ResolvedRate> direct = resolve(normalizedFrom, normalizedTo, targetDate, false);
        Optional<ResolvedRate> inverse = resolve(normalizedTo, normalizedFrom, targetDate, true);
        ResolvedRate resolved = direct.or(() -> inverse)
                .orElseThrow(() -> new NotFoundException("Exchange rate not found for pair: " + normalizedFrom + "/" + normalizedTo));

        BigDecimal convertedAmount = amount.multiply(resolved.rate()).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        return new ExchangeRateQuoteResponse(
                normalizedFrom,
                normalizedTo,
                amount.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP),
                resolved.rate(),
                convertedAmount,
                resolved.rateDate(),
                resolved.source(),
                resolved.quoteMode()
        );
    }

    private Optional<ResolvedRate> resolve(String fromCurrency, String toCurrency, LocalDate rateDate, boolean inverse) {
        Optional<ExchangeRate> found = exchangeRateRepository
                .findTopByFromCurrencyAndToCurrencyAndRateDateLessThanEqualOrderByRateDateDescIdDesc(fromCurrency, toCurrency, rateDate);
        if (found.isEmpty()) {
            found = exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByRateDateDescIdDesc(fromCurrency, toCurrency);
        }
        return found.map(rate -> inverse
                ? new ResolvedRate(
                BigDecimal.ONE.divide(rate.getRate(), RATE_SCALE, RoundingMode.HALF_UP),
                rate.getRateDate(),
                rate.getSource(),
                "INVERSE"
        )
                : new ResolvedRate(rate.getRate(), rate.getRateDate(), rate.getSource(), "DIRECT"));
    }

    private ExchangeRateResponse toResponse(ExchangeRate entity) {
        return new ExchangeRateResponse(
                entity.getId(),
                entity.getFromCurrency(),
                entity.getToCurrency(),
                entity.getRateDate(),
                entity.getRate(),
                entity.getSource(),
                entity.getCreatedAt()
        );
    }

    private String normalizeCurrency(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("Currency is required");
        }
        return raw.trim().toUpperCase();
    }

    private record ResolvedRate(
            BigDecimal rate,
            LocalDate rateDate,
            String source,
            String quoteMode
    ) {
    }
}
