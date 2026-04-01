package jhn.platform_project.domain.backtest.calc;

import jhn.platform_project.domain.backtest.dto.RebalanceSnapshotDto;
import jhn.platform_project.domain.backtest.dto.SelectedStockDto;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * ticker가 여러 스냅샷에 반복될 경우 최초 1회만 남기고 이후는 제거한다.
 */
@Component
public class FirstEntryOnlyPostProcessor {

    public List<RebalanceSnapshotDto> apply(List<RebalanceSnapshotDto> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            return snapshots;
        }

        Set<String> seen = new HashSet<>();
        List<RebalanceSnapshotDto> result = new ArrayList<>();

        for (RebalanceSnapshotDto snapshot : snapshots) {
            List<SelectedStockDto> filtered = new ArrayList<>();

            for (SelectedStockDto stock : snapshot.getSelectedStocks()) {
                String ticker = stock.getTicker();
                if (ticker == null) {
                    continue;
                }
                if (seen.contains(ticker)) {
                    continue;
                }
                seen.add(ticker);
                filtered.add(stock);
            }

            // ✅ 스냅샷을 유지할지/버릴지 정책:
            // - 유지하면 selectedCount=0 스냅샷도 내려감
            // - 보통은 비어있으면 빼는 게 UI에 깔끔함 (추천)
            if (!filtered.isEmpty()) {
                result.add(RebalanceSnapshotDto.builder()
                        .rebalanceDate(snapshot.getRebalanceDate())
                        .selectedCount(filtered.size())
                        .selectedStocks(filtered)
                        .build());
            }
        }

        return result;
    }
}