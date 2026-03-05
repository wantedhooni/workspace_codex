package com.revy.mvpbanking.admin.application;

import com.revy.mvpbanking.account.domain.Account;
import com.revy.mvpbanking.approval.domain.ApprovalRequest;
import com.revy.mvpbanking.customer.domain.Customer;
import com.revy.mvpbanking.exchange.domain.ExchangeRequest;
import com.revy.mvpbanking.fx.domain.FxRate;
import com.revy.mvpbanking.funding.domain.FundingRequest;
import com.revy.mvpbanking.stock.domain.StockOrder;
import com.revy.mvpbanking.stock.domain.StockQuote;
import java.util.List;

record AdminOverviewDataset(
        List<ApprovalRequest> approvals,
        List<Customer> customers,
        List<Account> accounts,
        List<FundingRequest> fundingRequests,
        List<ExchangeRequest> exchangeRequests,
        List<StockOrder> stockOrders,
        List<FxRate> latestFxRates,
        List<StockQuote> latestStockQuotes
) {
}
