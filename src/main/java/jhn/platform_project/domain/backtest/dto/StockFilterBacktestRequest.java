package jhn.platform_project.domain.backtest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockFilterBacktestRequest {

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private RebalancingCycle rebalancingCycle;

    /**
     * 거래대금 순위 조건
     */
    private Long tradingValueRankMin;
    private Long tradingValueRankMax;

    /**
     * 거래대금 금액 조건 (원 단위)
     */
    private Long tradingValueMin;
    private Long tradingValueMax;

    /**
     * 시가총액 순위 조건
     */
    private Long marketCapRankMin;
    private Long marketCapRankMax;

    /**
     * 시가총액 금액 조건 (원 단위)
     */
    private Long marketCapMin;
    private Long marketCapMax;
}