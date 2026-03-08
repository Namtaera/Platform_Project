package jhn.platform_project.domain.backtest.entity;

import lombok.*;

import java.io.Serializable;

/**
 * WeeklyTradingVolume 복합키
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WeeklyTradingVolumeId implements Serializable {

    /** 주간 시작일 */
    private String startDate;

    /** 주간 종료일 */
    private String endDate;

    /** 거래대금 순위 */
    private Long ranking;

    /** 종목 티커 */
    private String ticker;
}