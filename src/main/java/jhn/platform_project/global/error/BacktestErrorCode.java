package jhn.platform_project.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 백테스트 도메인에서 발생하는 예외를 코드/상태/메시지 템플릿으로 표준화한다.
 * messageTemplate은 String.format(...)에 사용될 수 있다.
 */
@Getter
public enum BacktestErrorCode {

    /** 시작일 > 종료일 */
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "BACKTEST_001", "시작일은 종료일보다 늦을 수 없습니다."),

    /** min > max 범위 오류 (fieldName 포맷팅) */
    INVALID_RANGE(HttpStatus.BAD_REQUEST, "BACKTEST_002", "%s 최소값은 최대값보다 클 수 없습니다."),

    /** excludedMonths 값 범위(1~12) 오류 */
    INVALID_EXCLUDED_MONTH(HttpStatus.BAD_REQUEST, "BACKTEST_003", "excludedMonths는 1~12 사이의 월만 가능합니다."),

    /** 조회 기간에 존재하지 않는 월을 제외하려는 경우 */
    EXCLUDED_MONTH_NOT_IN_RANGE(HttpStatus.BAD_REQUEST, "BACKTEST_004",
            "제외 월(%d월)은 선택한 기간에 존재하지 않습니다. 기간 내 월만 제외할 수 있습니다."),

    /** 리밸런싱 주기보다 데이터(금요일)가 부족한 경우 */
    NOT_ENOUGH_REBALANCE_DATA(HttpStatus.BAD_REQUEST, "BACKTEST_005",
            "리밸런싱 주기(%d주)보다 기간이 짧습니다. 최소 %d주 이상 선택해주세요."),

    /** 월 제외/데이터 부재로 리밸런싱 가능한 날짜가 0인 경우 */
    NO_AVAILABLE_DATES(HttpStatus.BAD_REQUEST, "BACKTEST_006",
            "선택한 기간/제외월 조건으로 조회 가능한 리밸런싱 데이터가 없습니다.");


    /** HTTP 상태 코드 */
    private final HttpStatus status;

    /** 클라이언트/로그용 에러 코드 */
    private final String code;

    /** 사용자 메시지 템플릿 (String.format 대상) */
    private final String messageTemplate;

    BacktestErrorCode(HttpStatus status, String code, String messageTemplate) {
        this.status = status;
        this.code = code;
        this.messageTemplate = messageTemplate;
    }


}