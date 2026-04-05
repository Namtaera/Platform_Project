package jhn.platform_project.domain.upperlimit.repository;

import jhn.platform_project.domain.upperlimit.entity.DailyUpperLimit;
import jhn.platform_project.domain.upperlimit.entity.DailyUpperLimitId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DailyUpperLimitRepository extends JpaRepository<DailyUpperLimit, DailyUpperLimitId> {

    // market 전체(ALL)일 때
    List<DailyUpperLimit> findByBaseDateBetween(Long startBaseDate, Long endBaseDate);

    // 특정 market일 때
    List<DailyUpperLimit> findByBaseDateBetweenAndMarket(Long startBaseDate, Long endBaseDate, String market);
}