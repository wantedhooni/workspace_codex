package com.example.samplesecurebff.bff;

import com.example.samplesecurebff.downstream.CustomerDirectoryClient;
import com.example.samplesecurebff.downstream.PositionServiceClient;
import com.example.samplesecurebff.downstream.PositionView;
import com.example.samplesecurebff.downstream.RiskServiceClient;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DashboardAggregationService {

    private final CustomerDirectoryClient customerDirectoryClient;
    private final PositionServiceClient positionServiceClient;
    private final RiskServiceClient riskServiceClient;

    public DashboardAggregationService(
            CustomerDirectoryClient customerDirectoryClient,
            PositionServiceClient positionServiceClient,
            RiskServiceClient riskServiceClient
    ) {
        this.customerDirectoryClient = customerDirectoryClient;
        this.positionServiceClient = positionServiceClient;
        this.riskServiceClient = riskServiceClient;
    }

    public AccountDashboardResponse dashboard(String accountId) {
        List<PositionView> positions = positionServiceClient.getPositions(accountId);
        BigDecimal totalMarketValue = positions.stream()
                .map(PositionView::marketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AccountDashboardResponse(
                customerDirectoryClient.getCustomer(accountId),
                positions,
                riskServiceClient.getRisk(accountId),
                totalMarketValue
        );
    }
}
