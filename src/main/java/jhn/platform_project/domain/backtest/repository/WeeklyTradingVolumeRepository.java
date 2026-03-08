package jhn.platform_project.domain.backtest.repository;

import jhn.platform_project.domain.backtest.entity.WeeklyTradingVolume;
import jhn.platform_project.domain.backtest.entity.WeeklyTradingVolumeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeeklyTradingVolumeRepository extends JpaRepository<WeeklyTradingVolume, WeeklyTradingVolumeId> {

    List<WeeklyTradingVolume> findByEndDateBetweenOrderByEndDateAscRankingAsc(
            String startDate,
            String endDate
    );

    List<WeeklyTradingVolume> findByEndDateOrderByRankingAsc(String endDate);
}