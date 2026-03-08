package jhn.platform_project.domain.backtest.repository;

import jhn.platform_project.domain.backtest.entity.WeeklyMarketCap;
import jhn.platform_project.domain.backtest.entity.WeeklyMarketCapId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeeklyMarketCapRepository extends JpaRepository<WeeklyMarketCap, WeeklyMarketCapId> {

    List<WeeklyMarketCap> findByBaseDateBetweenOrderByBaseDateAscRankingAsc(
            Long startDate,
            Long endDate
    );

    List<WeeklyMarketCap> findByBaseDateOrderByRankingAsc(Long baseDate);
}