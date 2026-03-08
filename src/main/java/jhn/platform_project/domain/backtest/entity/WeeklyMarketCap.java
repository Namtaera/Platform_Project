package jhn.platform_project.domain.backtest.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 기준일별 시가총액 순위 테이블
 */
@Entity
@Table(name = "weekly_market_cap")
@IdClass(WeeklyMarketCapId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WeeklyMarketCap {

    /** 기준일(yyyyMMdd) */
    @Id
    @Column(name = "base_date")
    private Long baseDate;

    /** 시가총액 순위 */
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

    /** 시가총액 */
    @Column(name = "market_cap")
    private Long marketCap;

    /** 종가 */
    @Column(name = "close_price")
    private Long closePrice;
}