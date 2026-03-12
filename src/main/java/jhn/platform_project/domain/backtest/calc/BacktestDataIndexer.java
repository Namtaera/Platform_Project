package jhn.platform_project.domain.backtest.calc;

import jhn.platform_project.domain.backtest.entity.WeeklyMarketCap;
import jhn.platform_project.domain.backtest.entity.WeeklyTradingVolume;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 백테스트 원천 Row 데이터를 날짜 기준(Map<LocalDate, List<...>>)으로 인덱싱한다.
 * - trading: endDate(금요일) 기준
 * - marketCap: baseDate(금요일) 기준
 */

@Component
public class BacktestDataIndexer {

    private static final DateTimeFormatter BASIC = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DASH = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Map<LocalDate, List<WeeklyTradingVolume>> indexTradingByEndDate(List<WeeklyTradingVolume> tradingRows) {
        return tradingRows.stream().collect(Collectors.groupingBy(
                row -> parseFlexible(row.getEndDate()),
                TreeMap::new,
                Collectors.toList()
        ));
    }

    public Map<LocalDate, List<WeeklyMarketCap>> indexMarketCapByBaseDate(List<WeeklyMarketCap> marketCapRows) {
        return marketCapRows.stream().collect(Collectors.groupingBy(
                row -> LocalDate.parse(String.valueOf(row.getBaseDate()), BASIC),
                TreeMap::new,
                Collectors.toList()
        ));
    }

    private LocalDate parseFlexible(String value) {
        if (value.contains("-")) {
            return LocalDate.parse(value, DASH);
        }
        return LocalDate.parse(value, BASIC);
    }
}