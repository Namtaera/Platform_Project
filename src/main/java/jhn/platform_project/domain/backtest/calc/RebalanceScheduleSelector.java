package jhn.platform_project.domain.backtest.calc;

import jhn.platform_project.domain.backtest.dto.RebalancingCycle;
import jhn.platform_project.global.error.BacktestErrorCode;
import jhn.platform_project.global.error.BacktestException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * availableDates(금요일 리스트)로부터 리밸런싱 실행일(rebalanceDate)을 선택한다.
 * - 1/2/3주: N번째 금요일마다 선택
 * - 1개월: 월별 마지막 금요일 선택
 */

@Component
public class RebalanceScheduleSelector {

    public List<LocalDate> select(List<LocalDate> availableDates, RebalancingCycle cycle) {
        if (availableDates == null || availableDates.isEmpty()) {
            return Collections.emptyList();
        }

        if (cycle == RebalancingCycle.ONE_MONTH) {
            return selectMonthlyLastDates(availableDates);
        }

        int step = getWeeks(cycle);

        if (availableDates.size() < step) {
            throw new BacktestException(BacktestErrorCode.NOT_ENOUGH_REBALANCE_DATA, step, step);
        }

        List<LocalDate> result = new ArrayList<>();
        int startIndex = step - 1;
        for (int i = startIndex; i < availableDates.size(); i += step) {
            result.add(availableDates.get(i));
        }
        return result;
    }

    private List<LocalDate> selectMonthlyLastDates(List<LocalDate> availableDates) {
        Map<YearMonth, LocalDate> lastDateByMonth = new LinkedHashMap<>();
        for (LocalDate date : availableDates) {
            lastDateByMonth.put(YearMonth.from(date), date);
        }
        return new ArrayList<>(lastDateByMonth.values());
    }

    private int getWeeks(RebalancingCycle cycle) {
        return switch (cycle) {
            case ONE_WEEK -> 1;
            case TWO_WEEKS -> 2;
            case THREE_WEEKS -> 3;
            default -> throw new IllegalArgumentException("주 단위 리밸런싱이 아닙니다.");
        };
    }
}