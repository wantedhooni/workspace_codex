package com.revy.mvpbanking.stock.application;

import com.revy.mvpbanking.stock.domain.StockOrder;
import com.revy.mvpbanking.stock.domain.StockOrderExecution;
import java.util.List;

public record StockOrderDetail(
        StockOrder order,
        List<StockOrderExecution> executions
) {
}
