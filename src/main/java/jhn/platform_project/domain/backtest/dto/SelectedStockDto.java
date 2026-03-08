package jhn.platform_project.domain.backtest.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SelectedStockDto {

    private String ticker;
    private String stockName;

    private Long tradingValueRank;
    private Long tradingValue;

    private Long marketCapRank;
    private Long marketCap;
    private Long closePrice;
}