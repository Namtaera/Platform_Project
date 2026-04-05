package jhn.platform_project.domain.upperlimit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "daily_upper_limit")
@IdClass(DailyUpperLimitId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DailyUpperLimit {

    @Id
    @Column(name = "base_date")
    private Long baseDate; // YYYYMMDD

    @Id
    @Column(name = "market")
    private String market; // KOSPI/KOSDAQ

    @Id
    @Column(name = "ticker")
    private String ticker;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "close_price")
    private Long closePrice;

    @Column(name = "change_rate")
    private Double changeRate;

    @Column(name = "trading_value")
    private Long tradingValue;

    // ✅ DB 컬럼명이 다르면 여기만 수정
    @Column(name = "trading_volume")
    private Long tradingVolume;
}