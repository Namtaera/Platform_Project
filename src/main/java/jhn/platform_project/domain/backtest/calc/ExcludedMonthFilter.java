package jhn.platform_project.domain.backtest.calc;

import jhn.platform_project.domain.backtest.entity.WeeklyMarketCap;
import jhn.platform_project.domain.backtest.entity.WeeklyTradingVolume;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * excludedMonths(1~12)에 해당하는 월의 데이터를 인덱스(Map)에서 제거한다.
 * 제외된 월은 리밸런싱 날짜/집계/결과에서 모두 자동 제외된다.
 */

@Component
public class ExcludedMonthFilter {

    public void apply(
            Map<LocalDate, List<WeeklyTradingVolume>> tradingByDate,
            Map<LocalDate, List<WeeklyMarketCap>> marketCapByDate,
            List<Integer> excludedMonths
    ) {
        if (excludedMonths == null || excludedMonths.isEmpty()) return;

        Set<Integer> excluded = excludedMonths.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        tradingByDate.keySet().removeIf(d -> excluded.contains(d.getMonthValue()));
        marketCapByDate.keySet().removeIf(d -> excluded.contains(d.getMonthValue()));
    }
}