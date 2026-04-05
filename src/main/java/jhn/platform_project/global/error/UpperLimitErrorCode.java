package jhn.platform_project.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 상한가(일별 조회) API 전용 에러코드
 */
@Getter
public enum UpperLimitErrorCode {

    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "UPPER_001", "시작일은 종료일보다 늦을 수 없습니다."),
    INVALID_RANGE(HttpStatus.BAD_REQUEST, "UPPER_002", "%s 최소값은 최대값보다 클 수 없습니다."),
    INVALID_MARKET(HttpStatus.BAD_REQUEST, "UPPER_003", "market 값이 올바르지 않습니다. (KOSPI/KOSDAQ/ALL)"),
    NO_DATA(HttpStatus.BAD_REQUEST, "UPPER_004", "선택한 기간/조건으로 조회 가능한 데이터가 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String messageTemplate;

    UpperLimitErrorCode(HttpStatus status, String code, String messageTemplate) {
        this.status = status;
        this.code = code;
        this.messageTemplate = messageTemplate;
    }
}