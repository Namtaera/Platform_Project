package jhn.platform_project.domain.backtest.service;

import jhn.platform_project.domain.backtest.calc.FirstEntryOnlyPostProcessor;
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

    // ✅ 추가: 최초 진입 종목만 남기는 후처리기
    private final FirstEntryOnlyPostProcessor firstEntryOnlyPostProcessor;

    private static final DateTimeFormatter BASIC_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public StockFilterBacktestResponse runBacktest(StockFilterBacktestRequest request) {
        validator.validate(request);

        var snapshots = calculateSnapshots(request);

        return StockFilterBacktestResponse.builder()
                .message("종목 선별 백테스트 조회가 완료되었습니다.")
                .snapshots(snapshots)
                .build();
    }

    @Override
    public StockFilterBacktestResponse runBacktestFirstEntryOnly(StockFilterBacktestRequest request) {
        validator.validate(request);

        var snapshots = calculateSnapshots(request);

        // ✅ 여기서만 후처리
        var processed = firstEntryOnlyPostProcessor.apply(snapshots);

        return StockFilterBacktestResponse.builder()
                .message("종목 선별 백테스트 조회(최초 진입 종목만)가 완료되었습니다.")
                .snapshots(processed)
                .build();
    }

    // ✅ 중복 제거: 두 API가 공통으로 쓰는 계산 부분을 private 메서드로 분리
    private java.util.List<jhn.platform_project.domain.backtest.dto.RebalanceSnapshotDto> calculateSnapshots(
            StockFilterBacktestRequest request
    ) {
        String tradingStartDate = request.getStartDate().format(BASIC_DATE_FORMATTER);
        String tradingEndDate = request.getEndDate().format(BASIC_DATE_FORMATTER);

        Long marketCapStartDate = Long.parseLong(request.getStartDate().format(BASIC_DATE_FORMATTER));
        Long marketCapEndDate = Long.parseLong(request.getEndDate().format(BASIC_DATE_FORMATTER));

        var tradingRows = weeklyTradingVolumeRepository
                .findByEndDateBetweenOrderByEndDateAscRankingAsc(tradingStartDate, tradingEndDate);

        var marketCapRows = weeklyMarketCapRepository
                .findByBaseDateBetweenOrderByBaseDateAscRankingAsc(marketCapStartDate, marketCapEndDate);

        return calculator.calculateSnapshots(request, tradingRows, marketCapRows);
    }
}