package jhn.platform_project.domain.backtest.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockFilterBacktestResponse {

    private String message;
    private List<RebalanceSnapshotDto> snapshots;
}