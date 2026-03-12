package jhn.platform_project.domain.backtest.validator;

import jhn.platform_project.domain.backtest.dto.StockFilterBacktestRequest;
import jhn.platform_project.global.error.BacktestErrorCode;
import jhn.platform_project.global.error.BacktestException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 백테스트 요청 파라미터를 검증한다.
 * (기간/범위/제외월 등) 유효하지 않으면 BacktestException을 발생시킨다.
 */
@Component
public class StockFilterBacktestRequestValidator {

    /** 요청 전체 검증 엔트리 포인트 */
    public void validate(StockFilterBacktestRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BacktestException(BacktestErrorCode.INVALID_DATE_RANGE);
        }

        validateRange(request.getTradingValueRankMin(), request.getTradingValueRankMax(), "거래대금 순위");
        validateRange(request.getMarketCapRankMin(), request.getMarketCapRankMax(), "시가총액 순위");
        validateRange(request.getTradingValueMin(), request.getTradingValueMax(), "거래대금 금액");
        validateRange(request.getMarketCapMin(), request.getMarketCapMax(), "시가총액 금액");

        validateExcludedMonths(request.getStartDate(), request.getEndDate(), request.getExcludedMonths());
    }

    /** min/max 형태의 범위 값 검증 */
    private void validateRange(Long min, Long max, String fieldName) {
        if (min != null && max != null && min > max) {
            throw new BacktestException(BacktestErrorCode.INVALID_RANGE, fieldName);
        }
    }

    /**
     * 제외월(excludedMonths) 검증
     * - 월은 1~12
     * - 선택한 기간에 존재하지 않는 월을 제외로 보내면 오류
     */
    private void validateExcludedMonths(LocalDate startDate, LocalDate endDate, List<Integer> excludedMonths) {
        if (excludedMonths == null || excludedMonths.isEmpty()) {
            return;
        }

        Set<Integer> unique = new HashSet<>();
        for (Integer m : excludedMonths) {
            if (m == null || m < 1 || m > 12) {
                throw new BacktestException(BacktestErrorCode.INVALID_EXCLUDED_MONTH);
            }
            unique.add(m);
        }

        // 기간에 포함되는 월(1~12) 집합 생성 (년도 무관)
        Set<Integer> monthsInRange = new HashSet<>();
        LocalDate cursor = startDate.withDayOfMonth(1);
        LocalDate endCursor = endDate.withDayOfMonth(1);

        while (!cursor.isAfter(endCursor)) {
            monthsInRange.add(cursor.getMonthValue());
            cursor = cursor.plusMonths(1);
        }

        for (Integer m : unique) {
            if (!monthsInRange.contains(m)) {
                throw new BacktestException(BacktestErrorCode.EXCLUDED_MONTH_NOT_IN_RANGE, m);
            }
        }
    }
}