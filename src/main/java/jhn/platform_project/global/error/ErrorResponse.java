package jhn.platform_project.global.error;

import lombok.Builder;
import lombok.Getter;

/**
 * API 에러 응답 표준 포맷.
 * - code: 내부 에러 코드
 * - message: 사용자 메시지
 */
@Getter
@Builder
public class ErrorResponse {
    private String code;
    private String message;
}