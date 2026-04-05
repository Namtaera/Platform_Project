package jhn.platform_project.domain.upperlimit.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpperLimitSearchRequest {

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    /**
     * KOSPI | KOSDAQ | ALL(null도 ALL 취급 가능)
     */
    private String market;

    // 종가(원) 조건
    private Long closePriceMin;
    private Long closePriceMax;

    // 거래대금(원) 조건
    private Long tradingValueMin;
    private Long tradingValueMax;

    // 거래량(주) 조건
    private Long tradingVolumeMin;
    private Long tradingVolumeMax;
}