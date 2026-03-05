package com.revy.mvpbanking.fx.presentation;

import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.fx.application.FxRateService;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("user-api")
@RequestMapping("/api/user/fx-rates")
public class UserFxRateController {

    private final FxRateService fxRateService;

    public UserFxRateController(FxRateService fxRateService) {
        this.fxRateService = fxRateService;
    }

    @GetMapping
    public ApiResponse<List<FxRateResponse>> list() {
        return ApiResponse.ok(fxRateService.getRates().stream().map(FxRateResponse::from).toList());
    }
}
