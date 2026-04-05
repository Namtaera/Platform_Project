package jhn.platform_project.global.error;

import lombok.Getter;

/**
 * 상한가(일별 조회) 도메인 전용 예외
 */
@Getter
public class UpperLimitException extends RuntimeException {

    private final UpperLimitErrorCode errorCode;
    private final Object[] args;

    public UpperLimitException(UpperLimitErrorCode errorCode, Object... args) {
        super(errorCode.getMessageTemplate());
        this.errorCode = errorCode;
        this.args = args;
    }
}