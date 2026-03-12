package jhn.platform_project.domain.backtest.service;

import jhn.platform_project.domain.backtest.calc.StockFilterBacktestCalculator;
import jhn.platform_project.domain.backtest.dto.StockFilterBacktestRequest;
import jhn.platform_project.domain.backtest.dto.StockFilterBacktestResponse;
import jhn.platform_project.domain.backtest.repository.WeeklyMarketCapRepository;
import jhn.platform_project.domain.backtest.repository.WeeklyTradingVolumeRepository;
import jhn.platform_project.domain.backtest.validator.StockFilterBacktestRequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockFilterBacktestServiceImpl implements StockFilterBacktestService {

    private final WeeklyTradingVolumeRepository weeklyTradingVolumeRepository;
    private final WeeklyMarketCapRepository weeklyMarketCapRepository;

    private final StockFilterBacktestRequestValidator validator;
    private final StockFilterBacktestCalculator calculator;

    private static final DateTimeFormatter BASIC_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public StockFilterBacktestResponse runBacktest(StockFilterBacktestRequest request) {
        validator.validate(request);

        String tradingStartDate = request.getStartDate().format(BASIC_DATE_FORMATTER);
        String tradingEndDate = request.getEndDate().format(BASIC_DATE_FORMATTER);

        Long marketCapStartDate = Long.parseLong(request.getStartDate().format(BASIC_DATE_FORMATTER));
        Long marketCapEndDate = Long.parseLong(request.getEndDate().format(BASIC_DATE_FORMATTER));

        var tradingRows = weeklyTradingVolumeRepository
                .findByEndDateBetweenOrderByEndDateAscRankingAsc(tradingStartDate, tradingEndDate);

        var marketCapRows = weeklyMarketCapRepository
                .findByBaseDateBetweenOrderByBaseDateAscRankingAsc(marketCapStartDate, marketCapEndDate);

        var snapshots = calculator.calculateSnapshots(request, tradingRows, marketCapRows);

        return StockFilterBacktestResponse.builder()
                .message("종목 선별 백테스트 조회가 완료되었습니다.")
                .snapshots(snapshots)
                .build();
    }
}