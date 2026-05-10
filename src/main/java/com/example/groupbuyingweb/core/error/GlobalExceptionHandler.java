package com.example.groupbuyingweb.core.error;

import com.example.groupbuyingweb.core.api.ApiResponse;
import com.example.groupbuyingweb.domain.enums.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 커스텀 비즈니스 예외 처리
    @ExceptionHandler(BusinessException.class)
    protected ApiResponse<?> handleBusinessException(BusinessException e) {
        return ApiResponse.error(e.getErrorCode().getStatus(), e.getMessage());
    }

    // 2. 잘못된 상태 예외 처리 (수량 초과 등)
    @ExceptionHandler(IllegalStateException.class)
    protected ApiResponse<?> handleIllegalStateException(IllegalStateException e) {
        // HTTP 400 Bad Request와 함께 에러 메시지 반환
        return ApiResponse.error(400, e.getMessage());
    }

    // 3. 잘못된 인자 예외 처리 (존재하지 않는 게시글 조회 등)
    @ExceptionHandler(IllegalArgumentException.class)
    protected ApiResponse<?> handleIllegalArgumentException(IllegalArgumentException e) {
        // HTTP 400 Bad Request와 함께 에러 메시지 반환
        return ApiResponse.error(400, e.getMessage());
    }

    // 4. @Valid 데이터 검증 실패 예외 처리 (DTO 필드 에러)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ApiResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().isEmpty()
                ? "요청값이 올바르지 않습니다."
                : e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        return ApiResponse.error(400, message);
    }

    // 5. 파라미터 검증 실패 예외 처리
    @ExceptionHandler(ConstraintViolationException.class)
    protected ApiResponse<?> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse("요청값이 올바르지 않습니다.");

        return ApiResponse.error(400, message);
    }

    // 6. 필수 파라미터 누락 예외 처리
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ApiResponse<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return ApiResponse.error(400, "필수 요청 파라미터가 누락되었습니다.");
    }

    // 7. 그 외 모든 잡히지 않은 서버 내부 에러 처리
    @ExceptionHandler(Exception.class)
    protected ApiResponse<?> handleException(Exception e) {
        // 실제 운영 환경에서는 e.printStackTrace()나 로거(log.error)를 통해 로그를 남겨야 합니다.
        e.printStackTrace();
        return ApiResponse.error(
                ErrorCode.INTERNAL_SERVER_ERROR.getStatus(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        );
    }
}