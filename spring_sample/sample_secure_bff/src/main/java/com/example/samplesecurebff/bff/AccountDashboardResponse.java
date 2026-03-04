package com.example.samplesecurebff.bff;

import com.example.samplesecurebff.downstream.CustomerProfile;
import com.example.samplesecurebff.downstream.PositionView;
import com.example.samplesecurebff.downstream.RiskSnapshot;
import java.math.BigDecimal;
import java.util.List;

public record AccountDashboardResponse(
        CustomerProfile customer,
        List<PositionView> positions,
        RiskSnapshot risk,
        BigDecimal totalMarketValue
) {
}
