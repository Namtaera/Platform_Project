package jhn.platform_project.domain.backtest.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 주간 거래대금 순위 테이블
 */
@Entity
@Table(name = "weekly_trading_volume")
@IdClass(WeeklyTradingVolumeId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WeeklyTradingVolume {

    /** 주간 시작일 */
    @Id
    @Column(name = "start_date")
    private String startDate;

    /** 주간 종료일 */
    @Id
    @Column(name = "end_date")
    private String endDate;

    /** 거래대금 순위 */
    @Id
    @Column(name = "ranking")
    private Long ranking;

    /** 종목 티커 */
    @Id
    @Column(name = "ticker")
    private String ticker;

    /** 종목명 */
    @Column(name = "company_name")
    private String companyName;

    /** 주간 거래대금 */
    @Column(name = "trading_value")
    private Long tradingValue;

    /** 주간 등락률(%) */
    @Column(name = "change_rate")
    private Double changeRate;
}