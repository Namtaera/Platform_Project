package jhn.platform_project.domain.backtest.calc;

import jhn.platform_project.domain.backtest.dto.RebalancingCycle;
import jhn.platform_project.domain.backtest.entity.WeeklyTradingVolume;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 리밸런싱 주기 기준으로 거래대금을 누적합하고, 누적합으로 순위를 재부여한다.
 * - 1/2/3주: 리밸런싱일 기준 직전 N주 누적합
 * - 1개월: 리밸런싱 월의 누적합
 */
@Component
public class TradingValueAggregator {

    private static final DateTimeFormatter BASIC = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 누적 거래대금 결과를 WeeklyTradingVolume 형태로 재구성한다.
     * (ranking은 누적합 기준으로 새로 매김)
     */
    public List<WeeklyTradingVolume> aggregate(
            LocalDate rebalanceDate,
            Map<LocalDate, List<WeeklyTradingVolume>> tradingByDate,
            RebalancingCycle cycle
    ) {
        List<LocalDate> selectedDates = selectWindowDates(rebalanceDate, tradingByDate, cycle);
        if (selectedDates.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Agg> map = new LinkedHashMap<>();

        // ticker별 거래대금 누적합
        for (LocalDate d : selectedDates) {
            for (WeeklyTradingVolume row : tradingByDate.getOrDefault(d, Collections.emptyList())) {
                Agg agg = map.computeIfAbsent(
                        row.getTicker(),
                        t -> new Agg(row.getTicker(), row.getCompanyName())
                );
                agg.add(row.getTradingValue());
            }
        }

        // 누적합 내림차순 정렬 → 순위 재부여
        List<Agg> list = new ArrayList<>(map.values());
        list.sort(Comparator.comparing(Agg::value).reversed());

        List<WeeklyTradingVolume> result = new ArrayList<>();
        long rank = 1L;

        for (Agg a : list) {
            result.add(WeeklyTradingVolume.builder()
                    .startDate(null)
                    .endDate(rebalanceDate.format(BASIC))
                    .ranking(rank++)
                    .ticker(a.ticker)
                    .companyName(a.name)
                    .tradingValue(a.value)
                    .build());
        }

        return result;
    }

    /**
     * 누적합에 포함할 주간(금요일) 날짜들을 선택한다.
     * - ONE_MONTH: 같은 달의 금요일들(리밸런싱일 이하) 전부
     * - 그 외: 리밸런싱일 이하의 금요일 중 마지막 N개
     */
    private List<LocalDate> selectWindowDates(
            LocalDate rebalanceDate,
            Map<LocalDate, List<WeeklyTradingVolume>> tradingByDate,
            RebalancingCycle cycle
    ) {
        if (tradingByDate.isEmpty()) {
            return Collections.emptyList();
        }

        if (cycle == RebalancingCycle.ONE_MONTH) {
            YearMonth ym = YearMonth.from(rebalanceDate);
            return tradingByDate.keySet().stream()
                    .filter(d -> YearMonth.from(d).equals(ym))
                    .filter(d -> !d.isAfter(rebalanceDate))
                    .sorted()
                    .toList();
        }

        int weeks = switch (cycle) {
            case ONE_WEEK -> 1;
            case TWO_WEEKS -> 2;
            case THREE_WEEKS -> 3;
            default -> throw new IllegalArgumentException("주 단위 리밸런싱이 아닙니다.");
        };

        List<LocalDate> candidates = tradingByDate.keySet().stream()
                .filter(d -> !d.isAfter(rebalanceDate))
                .sorted()
                .toList();

        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        int from = Math.max(0, candidates.size() - weeks);
        return candidates.subList(from, candidates.size());
    }

    /** ticker 단위 누적합 임시 컨테이너 */
    private static class Agg {
        private final String ticker;
        private final String name;
        private long value = 0L;

        private Agg(String ticker, String name) {
            this.ticker = ticker;
            this.name = name;
        }

        private void add(Long v) {
            if (v != null) {
                value += v;
            }
        }

        private long value() {
            return value;
        }
    }
}