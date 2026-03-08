package jhn.platform_project.domain.backtest.service;

import jhn.platform_project.domain.backtest.dto.StockFilterBacktestRequest;
import jhn.platform_project.domain.backtest.dto.StockFilterBacktestResponse;

public interface StockFilterBacktestService {

    StockFilterBacktestResponse runBacktest(StockFilterBacktestRequest request);
}
