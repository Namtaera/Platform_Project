package jhn.platform_project.domain.backtest.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RebalanceSnapshotDto {

    private LocalDate rebalanceDate;
    private Integer selectedCount;
    private List<SelectedStockDto> selectedStocks;
}