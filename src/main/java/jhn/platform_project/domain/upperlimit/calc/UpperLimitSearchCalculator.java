package jhn.platform_project.domain.upperlimit.calc;

import jhn.platform_project.domain.upperlimit.dto.DailySnapshotDto;
import jhn.platform_project.domain.upperlimit.dto.UpperLimitRowDto;
import jhn.platform_project.domain.upperlimit.dto.UpperLimitSearchRequest;
import jhn.platform_project.domain.upperlimit.entity.DailyUpperLimit;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class UpperLimitSearchCalculator {

    private static final DateTimeFormatter BASIC = DateTimeFormatter.ofPattern("yyyyMMdd");

    public List<DailySnapshotDto> buildSnapshots(
            UpperLimitSearchRequest request,
            List<DailyUpperLimit> rows
    ) {
        // 1) 조건 필터링(값 조건은 메모리 필터로 처리)
        List<DailyUpperLimit> filtered = rows.stream()
                .filter(r -> passesFilters(r, request))
                .toList();

        // 2) 날짜별 그룹핑
        Map<LocalDate, List<DailyUpperLimit>> byDate = filtered.stream()
                .collect(Collectors.groupingBy(
                        r -> LocalDate.parse(String.valueOf(r.getBaseDate()), BASIC),
                        TreeMap::new,
                        Collectors.toList()
                ));

        // 3) 날짜별 정렬 + DTO 변환
        List<DailySnapshotDto> snapshots = new ArrayList<>();

        for (Map.Entry<LocalDate, List<DailyUpperLimit>> entry : byDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<DailyUpperLimit> dayRows = entry.getValue();

            // 같은 날짜 내: 거래대금(tradingValue) 내림차순
            dayRows.sort(Comparator.comparing(DailyUpperLimit::getTradingValue, Comparator.nullsLast(Long::compareTo)).reversed());

            List<UpperLimitRowDto> mapped = dayRows.stream()
                    .map(this::toRowDto)
                    .toList();

            snapshots.add(DailySnapshotDto.builder()
                    .baseDate(date)
                    .count(mapped.size())
                    .rows(mapped)
                    .build());
        }

        return snapshots;
    }

    private boolean passesFilters(DailyUpperLimit r, UpperLimitSearchRequest req) {
        if (req.getClosePriceMin() != null && (r.getClosePrice() == null || r.getClosePrice() < req.getClosePriceMin())) return false;
        if (req.getClosePriceMax() != null && (r.getClosePrice() == null || r.getClosePrice() > req.getClosePriceMax())) return false;

        if (req.getTradingValueMin() != null && (r.getTradingValue() == null || r.getTradingValue() < req.getTradingValueMin())) return false;
        if (req.getTradingValueMax() != null && (r.getTradingValue() == null || r.getTradingValue() > req.getTradingValueMax())) return false;

        if (req.getTradingVolumeMin() != null && (r.getTradingVolume() == null || r.getTradingVolume() < req.getTradingVolumeMin())) return false;
        if (req.getTradingVolumeMax() != null && (r.getTradingVolume() == null || r.getTradingVolume() > req.getTradingVolumeMax())) return false;

        return true;
    }

    private UpperLimitRowDto toRowDto(DailyUpperLimit r) {
        return UpperLimitRowDto.builder()
                .market(r.getMarket())
                .ticker(r.getTicker())
                .companyName(r.getCompanyName())
                .closePrice(r.getClosePrice())
                .changeRate(r.getChangeRate())
                .tradingVolume(r.getTradingVolume())
                .tradingValue(r.getTradingValue())
                .build();
    }
}