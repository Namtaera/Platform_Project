package jhn.platform_project.domain.upperlimit.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpperLimitRowDto {

    private String market;      // KOSPI/KOSDAQ
    private String ticker;
    private String companyName;

    private Long closePrice;    // 종가(원)
    private Double changeRate;  // 등락률(%)

    private Long tradingVolume; // 거래량(주)
    private Long tradingValue;  // 거래대금(원)
}