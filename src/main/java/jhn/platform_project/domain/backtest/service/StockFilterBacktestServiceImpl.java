package jhn.platform_project.domain.backtest.service;

import jhn.platform_project.domain.backtest.dto.*;
import jhn.platform_project.domain.backtest.entity.WeeklyMarketCap;
import jhn.platform_project.domain.backtest.entity.WeeklyTradingVolume;
import jhn.platform_project.domain.backtest.repository.WeeklyMarketCapRepository;
import jhn.platform_project.domain.backtest.repository.WeeklyTradingVolumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockFilterBacktestServiceImpl implements StockFilterBacktestService {

    private final WeeklyTradingVolumeRepository weeklyTradingVolumeRepository;
    private final WeeklyMarketCapRepository weeklyMarketCapRepository;

    private static final DateTimeFormatter BASIC_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DASH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public StockFilterBacktestResponse runBacktest(StockFilterBacktestRequest request) {
        validateRequest(request);

        String tradingStartDate = formatTextDateForQuery(request.getStartDate());
        String tradingEndDate = formatTextDateForQuery(request.getEndDate());

        Long marketCapStartDate = toBaseDateLong(request.getStartDate());
        Long marketCapEndDate = toBaseDateLong(request.getEndDate());

        List<WeeklyTradingVolume> tradingRows =
                weeklyTradingVolumeRepository.findByEndDateBetweenOrderByEndDateAscRankingAsc(
                        tradingStartDate,
                        tradingEndDate
                );

        List<WeeklyMarketCap> marketCapRows =
                weeklyMarketCapRepository.findByBaseDateBetweenOrderByBaseDateAscRankingAsc(
                        marketCapStartDate,
                        marketCapEndDate
                );

        Map<LocalDate, List<WeeklyTradingVolume>> tradingByDate =
                tradingRows.stream()
                        .collect(Collectors.groupingBy(
                                row -> parseFlexibleTextDate(row.getEndDate()),
                                TreeMap::new,
                                Collectors.toList()
                        ));

        Map<LocalDate, List<WeeklyMarketCap>> marketCapByDate =
                marketCapRows.stream()
                        .collect(Collectors.groupingBy(
                                row -> parseBaseDate(row.getBaseDate()),
                                TreeMap::new,
                                Collectors.toList()
                        ));

        List<LocalDate> availableDates = tradingByDate.keySet().stream()
                .filter(marketCapByDate::containsKey)
                .sorted()
                .toList();

        List<LocalDate> rebalanceDates = selectRebalanceDates(availableDates, request.getRebalancingCycle());

        List<RebalanceSnapshotDto> snapshots = new ArrayList<>();

        for (LocalDate rebalanceDate : rebalanceDates) {
            List<WeeklyTradingVolume> aggregatedTradingList =
                    aggregateTradingVolumesUntilRebalanceDate(
                            rebalanceDate,
                            tradingByDate,
                            request.getRebalancingCycle()
                    );

            List<WeeklyMarketCap> marketCapList =
                    marketCapByDate.getOrDefault(rebalanceDate, Collections.emptyList());

            RebalanceSnapshotDto snapshot = buildSnapshot(
                    rebalanceDate,
                    aggregatedTradingList,
                    marketCapList,
                    request
            );

            snapshots.add(snapshot);
        }

        return StockFilterBacktestResponse.builder()
                .message("종목 선별 백테스트 조회가 완료되었습니다.")
                .snapshots(snapshots)
                .build();
    }

    private void validateRequest(StockFilterBacktestRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다.");
        }

        validateRange(request.getTradingValueRankMin(), request.getTradingValueRankMax(), "거래대금 순위");
        validateRange(request.getMarketCapRankMin(), request.getMarketCapRankMax(), "시가총액 순위");
        validateRange(request.getTradingValueMin(), request.getTradingValueMax(), "거래대금 금액");
        validateRange(request.getMarketCapMin(), request.getMarketCapMax(), "시가총액 금액");
    }

    private void validateRange(Long min, Long max, String fieldName) {
        if (min != null && max != null && min > max) {
            throw new IllegalArgumentException(fieldName + " 최소값은 최대값보다 클 수 없습니다.");
        }
    }

    private List<LocalDate> selectRebalanceDates(List<LocalDate> availableDates, RebalancingCycle cycle) {
        if (availableDates.isEmpty()) {
            return Collections.emptyList();
        }

        if (cycle == RebalancingCycle.ONE_MONTH) {
            return selectMonthlyLastDates(availableDates);
        }

        int step = getCycleWeeks(cycle);

        // ✅ 주기만큼 금요일이 없으면 요청 자체가 성립 불가
        if (availableDates.size() < step) {
            throw new IllegalArgumentException("리밸런싱 주기(" + step + "주)보다 기간이 짧습니다. 최소 " + step + "주 이상 선택해주세요.");
        }

        List<LocalDate> result = new ArrayList<>();
        int startIndex = step - 1;

        // ✅ 마지막에 step 미만 남는 구간은 자연스럽게 제외됨
        for (int i = startIndex; i < availableDates.size(); i += step) {
            result.add(availableDates.get(i));
        }

        return result;
    }


    private List<LocalDate> selectMonthlyLastDates(List<LocalDate> availableDates) {
        Map<YearMonth, LocalDate> lastDateByMonth = new LinkedHashMap<>();

        for (LocalDate date : availableDates) {
            YearMonth yearMonth = YearMonth.from(date);
            lastDateByMonth.put(yearMonth, date);
        }

        return new ArrayList<>(lastDateByMonth.values());
    }

    private List<WeeklyTradingVolume> aggregateTradingVolumesUntilRebalanceDate(
            LocalDate rebalanceDate,
            Map<LocalDate, List<WeeklyTradingVolume>> tradingByDate,
            RebalancingCycle cycle
    ) {
        List<LocalDate> selectedDates;

        if (cycle == RebalancingCycle.ONE_MONTH) {
            YearMonth targetMonth = YearMonth.from(rebalanceDate);

            selectedDates = tradingByDate.keySet().stream()
                    .filter(date -> YearMonth.from(date).equals(targetMonth))
                    .filter(date -> !date.isAfter(rebalanceDate))
                    .sorted()
                    .toList();
        } else {
            int weeks = getCycleWeeks(cycle);

            List<LocalDate> targetDates = tradingByDate.keySet().stream()
                    .filter(date -> !date.isAfter(rebalanceDate))
                    .sorted()
                    .toList();

            if (targetDates.isEmpty()) {
                return Collections.emptyList();
            }

            int fromIndex = Math.max(0, targetDates.size() - weeks);
            selectedDates = targetDates.subList(fromIndex, targetDates.size());
        }

        if (selectedDates.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, TradingAggregate> aggregateMap = new LinkedHashMap<>();

        for (LocalDate date : selectedDates) {
            List<WeeklyTradingVolume> weeklyList = tradingByDate.getOrDefault(date, Collections.emptyList());

            for (WeeklyTradingVolume row : weeklyList) {
                TradingAggregate aggregate = aggregateMap.computeIfAbsent(
                        row.getTicker(),
                        ticker -> new TradingAggregate(
                                row.getTicker(),
                                row.getCompanyName(),
                                0L
                        )
                );

                aggregate.addTradingValue(row.getTradingValue());
            }
        }

        List<TradingAggregate> aggregates = new ArrayList<>(aggregateMap.values());

        // 누적 거래대금 기준 재정렬 후 순위 재부여
        aggregates.sort(Comparator.comparing(TradingAggregate::getTradingValue).reversed());

        List<WeeklyTradingVolume> result = new ArrayList<>();
        long rank = 1L;

        for (TradingAggregate aggregate : aggregates) {
            result.add(
                    WeeklyTradingVolume.builder()
                            .startDate(null)
                            .endDate(formatTextDateForQuery(rebalanceDate))
                            .ranking(rank++)
                            .ticker(aggregate.getTicker())
                            .companyName(aggregate.getCompanyName())
                            .tradingValue(aggregate.getTradingValue())
                            .build()
            );
        }

        return result;
    }

    private RebalanceSnapshotDto buildSnapshot(
            LocalDate rebalanceDate,
            List<WeeklyTradingVolume> tradingList,
            List<WeeklyMarketCap> marketCapList,
            StockFilterBacktestRequest request
    ) {
        Map<String, WeeklyTradingVolume> tradingMap = tradingList.stream()
                .collect(Collectors.toMap(
                        WeeklyTradingVolume::getTicker,
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<String, WeeklyMarketCap> marketCapMap = marketCapList.stream()
                .collect(Collectors.toMap(
                        WeeklyMarketCap::getTicker,
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Set<String> tickers = new LinkedHashSet<>();
        tickers.addAll(tradingMap.keySet());
        tickers.addAll(marketCapMap.keySet());

        List<SelectedStockDto> selectedStocks = new ArrayList<>();

        for (String ticker : tickers) {
            WeeklyTradingVolume trading = tradingMap.get(ticker);
            WeeklyMarketCap marketCap = marketCapMap.get(ticker);

            if (!passesTradingValueFilters(trading, request)) {
                continue;
            }

            if (!passesMarketCapFilters(marketCap, request)) {
                continue;
            }

            selectedStocks.add(
                    SelectedStockDto.builder()
                            .ticker(ticker)
                            .stockName(resolveStockName(trading, marketCap))
                            .tradingValueRank(trading != null ? trading.getRanking() : null)
                            .tradingValue(trading != null ? trading.getTradingValue() : null)
                            .marketCapRank(marketCap != null ? marketCap.getRanking() : null)
                            .marketCap(marketCap != null ? marketCap.getMarketCap() : null)
                            .closePrice(marketCap != null ? marketCap.getClosePrice() : null)
                            .build()
            );
        }

        selectedStocks.sort(Comparator
                .comparing((SelectedStockDto dto) -> dto.getTradingValueRank() == null ? Long.MAX_VALUE : dto.getTradingValueRank())
                .thenComparing(dto -> dto.getMarketCapRank() == null ? Long.MAX_VALUE : dto.getMarketCapRank())
        );

        return RebalanceSnapshotDto.builder()
                .rebalanceDate(rebalanceDate)
                .selectedCount(selectedStocks.size())
                .selectedStocks(selectedStocks)
                .build();
    }

    private boolean passesTradingValueFilters(WeeklyTradingVolume trading, StockFilterBacktestRequest request) {
        boolean hasRankFilter = request.getTradingValueRankMin() != null || request.getTradingValueRankMax() != null;
        boolean hasAmountFilter = request.getTradingValueMin() != null || request.getTradingValueMax() != null;

        if (!hasRankFilter && !hasAmountFilter) {
            return true;
        }

        if (trading == null) {
            return false;
        }

        if (request.getTradingValueRankMin() != null && trading.getRanking() < request.getTradingValueRankMin()) {
            return false;
        }

        if (request.getTradingValueRankMax() != null && trading.getRanking() > request.getTradingValueRankMax()) {
            return false;
        }

        if (request.getTradingValueMin() != null && trading.getTradingValue() < request.getTradingValueMin()) {
            return false;
        }

        if (request.getTradingValueMax() != null && trading.getTradingValue() > request.getTradingValueMax()) {
            return false;
        }

        return true;
    }

    private boolean passesMarketCapFilters(WeeklyMarketCap marketCap, StockFilterBacktestRequest request) {
        boolean hasRankFilter = request.getMarketCapRankMin() != null || request.getMarketCapRankMax() != null;
        boolean hasAmountFilter = request.getMarketCapMin() != null || request.getMarketCapMax() != null;

        if (!hasRankFilter && !hasAmountFilter) {
            return true;
        }

        if (marketCap == null) {
            return false;
        }

        if (request.getMarketCapRankMin() != null && marketCap.getRanking() < request.getMarketCapRankMin()) {
            return false;
        }

        if (request.getMarketCapRankMax() != null && marketCap.getRanking() > request.getMarketCapRankMax()) {
            return false;
        }

        if (request.getMarketCapMin() != null && marketCap.getMarketCap() < request.getMarketCapMin()) {
            return false;
        }

        if (request.getMarketCapMax() != null && marketCap.getMarketCap() > request.getMarketCapMax()) {
            return false;
        }

        return true;
    }

    private String resolveStockName(WeeklyTradingVolume trading, WeeklyMarketCap marketCap) {
        if (trading != null && trading.getCompanyName() != null) {
            return trading.getCompanyName();
        }

        if (marketCap != null && marketCap.getCompanyName() != null) {
            return marketCap.getCompanyName();
        }

        return "";
    }

    private int getCycleWeeks(RebalancingCycle cycle) {
        return switch (cycle) {
            case ONE_WEEK -> 1;
            case TWO_WEEKS -> 2;
            case THREE_WEEKS -> 3;
            default -> throw new IllegalArgumentException("주 단위 리밸런싱이 아닙니다.");
        };
    }

    private Long toBaseDateLong(LocalDate date) {
        return Long.parseLong(date.format(BASIC_DATE_FORMATTER));
    }

    private String formatTextDateForQuery(LocalDate date) {
        return date.format(BASIC_DATE_FORMATTER);
    }

    private LocalDate parseBaseDate(Long baseDate) {
        if (baseDate == null) {
            throw new IllegalArgumentException("base_date 값이 null 입니다.");
        }

        String value = String.valueOf(baseDate);
        return LocalDate.parse(value, BASIC_DATE_FORMATTER);
    }

    private LocalDate parseFlexibleTextDate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("텍스트 날짜 값이 비어 있습니다.");
        }

        if (value.contains("-")) {
            return LocalDate.parse(value, DASH_DATE_FORMATTER);
        }

        return LocalDate.parse(value, BASIC_DATE_FORMATTER);
    }

    private static class TradingAggregate {
        private final String ticker;
        private final String companyName;
        private long tradingValue;

        private TradingAggregate(String ticker, String companyName, long tradingValue) {
            this.ticker = ticker;
            this.companyName = companyName;
            this.tradingValue = tradingValue;
        }

        private void addTradingValue(Long value) {
            if (value != null) {
                this.tradingValue += value;
            }
        }

        private String getTicker() {
            return ticker;
        }

        private String getCompanyName() {
            return companyName;
        }

        private long getTradingValue() {
            return tradingValue;
        }
    }
}