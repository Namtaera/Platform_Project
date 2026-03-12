package jhn.platform_project.domain.backtest.calc;

import jhn.platform_project.domain.backtest.dto.RebalanceSnapshotDto;
import jhn.platform_project.domain.backtest.dto.StockFilterBacktestRequest;
import jhn.platform_project.domain.backtest.entity.WeeklyMarketCap;
import jhn.platform_project.domain.backtest.entity.WeeklyTradingVolume;
import jhn.platform_project.global.error.BacktestErrorCode;
import jhn.platform_project.global.error.BacktestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 백테스트 계산 오케스트레이터.
 * 인덱싱 → 월 제외 → 리밸런싱 일정 선택 → 거래대금 누적/재순위 → 스냅샷 조립을 순서대로 수행한다.
 */

@Component
@RequiredArgsConstructor
public class StockFilterBacktestCalculator {

    private final BacktestDataIndexer indexer;
    private final ExcludedMonthFilter excludedMonthFilter;
    private final RebalanceScheduleSelector scheduleSelector;
    private final TradingValueAggregator tradingValueAggregator;
    private final SnapshotAssembler snapshotAssembler;

    public List<RebalanceSnapshotDto> calculateSnapshots(
            StockFilterBacktestRequest request,
            List<WeeklyTradingVolume> tradingRows,
            List<WeeklyMarketCap> marketCapRows
    ) {
        Map<LocalDate, List<WeeklyTradingVolume>> tradingByDate = indexer.indexTradingByEndDate(tradingRows);
        Map<LocalDate, List<WeeklyMarketCap>> marketCapByDate = indexer.indexMarketCapByBaseDate(marketCapRows);

        excludedMonthFilter.apply(tradingByDate, marketCapByDate, request.getExcludedMonths());

        List<LocalDate> availableDates = tradingByDate.keySet().stream()
                .filter(marketCapByDate::containsKey)
                .sorted()
                .toList();

        if (availableDates.isEmpty()) {
            throw new BacktestException(BacktestErrorCode.NO_AVAILABLE_DATES);
        }

        List<LocalDate> rebalanceDates = scheduleSelector.select(availableDates, request.getRebalancingCycle());
        if (rebalanceDates.isEmpty()) {
            return Collections.emptyList();
        }

        return rebalanceDates.stream().map(rebalanceDate -> {
            List<WeeklyTradingVolume> aggregatedTrading =
                    tradingValueAggregator.aggregate(rebalanceDate, tradingByDate, request.getRebalancingCycle());

            List<WeeklyMarketCap> marketCapList =
                    marketCapByDate.getOrDefault(rebalanceDate, Collections.emptyList());

            return snapshotAssembler.assemble(rebalanceDate, aggregatedTrading, marketCapList, request);
        }).toList();
    }
}