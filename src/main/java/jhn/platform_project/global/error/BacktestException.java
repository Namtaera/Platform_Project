package jhn.platform_project.global.error;

import lombok.Getter;

/**
 * 백테스트 도메인 전용 RuntimeException.
 * errorCode와 포맷팅 인자(args)를 함께 전달한다.
 */
@Getter
public class BacktestException extends RuntimeException {

    /** 에러의 종류(상태/코드/템플릿 포함) */
    private final BacktestErrorCode errorCode;

    /** messageTemplate에 주입될 인자들(String.format 용) */
    private final Object[] args;

    public BacktestException(BacktestErrorCode errorCode, Object... args) {
        // 실제 응답 메시지는 GlobalExceptionHandler에서 String.format으로 확정
        super(errorCode.getMessageTemplate());
        this.errorCode = errorCode;
        this.args = args;
    }
}