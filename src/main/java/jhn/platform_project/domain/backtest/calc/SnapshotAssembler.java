package jhn.platform_project.domain.backtest.calc;

import jhn.platform_project.domain.backtest.dto.RebalanceSnapshotDto;
import jhn.platform_project.domain.backtest.dto.SelectedStockDto;
import jhn.platform_project.domain.backtest.dto.StockFilterBacktestRequest;
import jhn.platform_project.domain.backtest.entity.WeeklyMarketCap;
import jhn.platform_project.domain.backtest.entity.WeeklyTradingVolume;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * (누적 거래대금 결과 + 리밸런싱일 시가총액 스냅샷 + 필터 조건)으로
 * 최종 SelectedStockDto 목록과 RebalanceSnapshotDto를 생성한다.
 */

@Component
public class SnapshotAssembler {

    public RebalanceSnapshotDto assemble(
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

        List<SelectedStockDto> selected = new ArrayList<>();

        for (String ticker : tickers) {
            WeeklyTradingVolume t = tradingMap.get(ticker);
            WeeklyMarketCap m = marketCapMap.get(ticker);

            if (!passesTradingFilters(t, request)) continue;
            if (!passesMarketCapFilters(m, request)) continue;

            selected.add(SelectedStockDto.builder()
                    .ticker(ticker)
                    .stockName(resolveName(t, m))
                    .tradingValueRank(t != null ? t.getRanking() : null)
                    .tradingValue(t != null ? t.getTradingValue() : null)
                    .marketCapRank(m != null ? m.getRanking() : null)
                    .marketCap(m != null ? m.getMarketCap() : null)
                    .closePrice(m != null ? m.getClosePrice() : null)
                    .build());
        }

        selected.sort(Comparator
                .comparing((SelectedStockDto dto) -> dto.getTradingValueRank() == null ? Long.MAX_VALUE : dto.getTradingValueRank())
                .thenComparing(dto -> dto.getMarketCapRank() == null ? Long.MAX_VALUE : dto.getMarketCapRank())
        );

        return RebalanceSnapshotDto.builder()
                .rebalanceDate(rebalanceDate)
                .selectedCount(selected.size())
                .selectedStocks(selected)
                .build();
    }

    private boolean passesTradingFilters(WeeklyTradingVolume trading, StockFilterBacktestRequest request) {
        boolean hasRank = request.getTradingValueRankMin() != null || request.getTradingValueRankMax() != null;
        boolean hasAmt = request.getTradingValueMin() != null || request.getTradingValueMax() != null;

        if (!hasRank && !hasAmt) return true;
        if (trading == null) return false;

        if (request.getTradingValueRankMin() != null && trading.getRanking() < request.getTradingValueRankMin()) return false;
        if (request.getTradingValueRankMax() != null && trading.getRanking() > request.getTradingValueRankMax()) return false;

        if (request.getTradingValueMin() != null && trading.getTradingValue() < request.getTradingValueMin()) return false;
        if (request.getTradingValueMax() != null && trading.getTradingValue() > request.getTradingValueMax()) return false;

        return true;
    }

    private boolean passesMarketCapFilters(WeeklyMarketCap marketCap, StockFilterBacktestRequest request) {
        boolean hasRank = request.getMarketCapRankMin() != null || request.getMarketCapRankMax() != null;
        boolean hasAmt = request.getMarketCapMin() != null || request.getMarketCapMax() != null;

        if (!hasRank && !hasAmt) return true;
        if (marketCap == null) return false;

        if (request.getMarketCapRankMin() != null && marketCap.getRanking() < request.getMarketCapRankMin()) return false;
        if (request.getMarketCapRankMax() != null && marketCap.getRanking() > request.getMarketCapRankMax()) return false;

        if (request.getMarketCapMin() != null && marketCap.getMarketCap() < request.getMarketCapMin()) return false;
        if (request.getMarketCapMax() != null && marketCap.getMarketCap() > request.getMarketCapMax()) return false;

        return true;
    }

    private String resolveName(WeeklyTradingVolume t, WeeklyMarketCap m) {
        if (t != null && t.getCompanyName() != null) return t.getCompanyName();
        if (m != null && m.getCompanyName() != null) return m.getCompanyName();
        return "";
    }
}