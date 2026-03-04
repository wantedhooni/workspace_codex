package com.example.samplesecurebff;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.samplesecurebff.bff.DashboardAggregationService;
import com.example.samplesecurebff.downstream.CustomerDirectoryClient;
import com.example.samplesecurebff.downstream.CustomerProfile;
import com.example.samplesecurebff.downstream.PositionServiceClient;
import com.example.samplesecurebff.downstream.PositionView;
import com.example.samplesecurebff.downstream.RiskServiceClient;
import com.example.samplesecurebff.downstream.RiskSnapshot;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class DashboardAggregationServiceTests {

    @Test
    void aggregatesCustomerRiskAndPositions() {
        CustomerDirectoryClient customerClient = accountId -> new CustomerProfile(accountId, "Kim Trader", "PRO", "USD");
        PositionServiceClient positionClient = accountId -> List.of(
                new PositionView("AAPL", 10, new BigDecimal("1000.00")),
                new PositionView("NVDA", 5, new BigDecimal("2000.00"))
        );
        RiskServiceClient riskClient = accountId -> new RiskSnapshot(accountId, new BigDecimal("3000.00"), "MEDIUM");

        DashboardAggregationService service = new DashboardAggregationService(customerClient, positionClient, riskClient);

        var response = service.dashboard("ACC-100");

        assertThat(response.customer().accountId()).isEqualTo("ACC-100");
        assertThat(response.totalMarketValue()).isEqualByComparingTo("3000.00");
    }
}
