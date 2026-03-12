package jhn.platform_project.global.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * BacktestException을 HTTP 응답으로 변환하는 전역 예외 처리기.
 * - messageTemplate + args를 String.format으로 확정
 * - 동일 포맷의 ErrorResponse 반환
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 백테스트 도메인 예외 처리
     * - 상태코드: errorCode.status
     * - 바디: { code, message }
     */
    @ExceptionHandler(BacktestException.class)
    public ResponseEntity<ErrorResponse> handleBacktestException(BacktestException e) {
        BacktestErrorCode code = e.getErrorCode();
        String msg = String.format(code.getMessageTemplate(), e.getArgs());

        // 운영에서는 warn 또는 info 정책에 맞게 조정 가능
        log.warn("[{}] {}", code.getCode(), msg);

        return ResponseEntity.status(code.getStatus())
                .body(ErrorResponse.builder()
                        .code(code.getCode())
                        .message(msg)
                        .build());
    }
}