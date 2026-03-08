package jhn.platform_project.domain.backtest.entity;

import lombok.*;

import java.io.Serializable;

/**
 * WeeklyMarketCap 복합키
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WeeklyMarketCapId implements Serializable {

    /** 기준일 */
    private Long baseDate;

    /** 시가총액 순위 */
    private Long ranking;

    /** 종목 티커 */
    private String ticker;
}