package jhn.platform_project.domain.backtest.controller;

import jhn.platform_project.domain.backtest.dto.StockFilterBacktestRequest;
import jhn.platform_project.domain.backtest.dto.StockFilterBacktestResponse;
import jhn.platform_project.domain.backtest.service.StockFilterBacktestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/backtests")
@RequiredArgsConstructor
public class StockFilterBacktestController {

    private final StockFilterBacktestService stockFilterBacktestService;

    @PostMapping("/stock-filters")
    public ResponseEntity<StockFilterBacktestResponse> runStockFilterBacktest(
            @Valid @RequestBody StockFilterBacktestRequest request
    ) {
        return ResponseEntity.ok(stockFilterBacktestService.runBacktest(request));
    }
}