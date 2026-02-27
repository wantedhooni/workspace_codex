# QueryDSL 검색 패턴 (v0.1)

## 1) 원칙

- 필드별 검색 조건을 명시적으로 받는다.
- 값이 없으면 조건을 제외한다.
- 문자열은 `containsIgnoreCase`, 코드/상태는 `eq`, 기간은 `goe/loe`를 사용한다.
- 정렬/페이징은 `Pageable`로 통일한다.

## 2) Payload 패턴

```java
public final class OrderSearchPayload {
    private OrderSearchPayload() {}

    public record Req(
            String symbol,
            String status,
            java.time.LocalDate fromDate,
            java.time.LocalDate toDate,
            Long portfolioId
    ) {}

    public record Res(
            Long orderId,
            Long portfolioId,
            String symbol,
            String status,
            java.math.BigDecimal quantity,
            java.time.Instant createdAt
    ) {}
}
```

## 3) Repository 패턴

```java
public Page<OrderSearchPayload.Res> search(OrderSearchPayload.Req req, Pageable pageable) {
    QOrder order = QOrder.order;
    BooleanBuilder where = new BooleanBuilder();

    if (req.symbol() != null && !req.symbol().isBlank()) {
        where.and(order.symbol.containsIgnoreCase(req.symbol()));
    }
    if (req.status() != null && !req.status().isBlank()) {
        where.and(order.status.eq(req.status()));
    }
    if (req.portfolioId() != null) {
        where.and(order.portfolioId.eq(req.portfolioId()));
    }
    if (req.fromDate() != null) {
        where.and(order.tradeDate.goe(req.fromDate()));
    }
    if (req.toDate() != null) {
        where.and(order.tradeDate.loe(req.toDate()));
    }

    // 구현 시 fetchResults 대체: countQuery + contentQuery 분리
    return Page.empty(pageable);
}
```

## 4) 금지 패턴

- 단일 `keyword`로 모든 필드를 OR 검색
- 문자열 필드 `eq` 남용
- 페이지네이션 없는 대량 조회 API
