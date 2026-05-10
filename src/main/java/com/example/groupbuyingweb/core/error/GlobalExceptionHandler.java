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

    @ExceptionHandler(BusinessException.class)
    protected ApiResponse<?> handleBusinessException(BusinessException e) {
        return ApiResponse.error(e.getErrorCode().getStatus(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ApiResponse<?> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .isEmpty()
                ? "요청값이 올바르지 않습니다."
                : e.getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();

        return ApiResponse.error(400, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ApiResponse<?> handleConstraintViolationException(
            ConstraintViolationException e
    ) {
        String message = e.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse("요청값이 올바르지 않습니다.");

        return ApiResponse.error(400, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ApiResponse<?> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e
    ) {
        return ApiResponse.error(400, "필수 요청 파라미터가 누락되었습니다.");
    }

    @ExceptionHandler(Exception.class)
    protected ApiResponse<?> handleException(Exception e) {
        return ApiResponse.error(
                ErrorCode.INTERNAL_SERVER_ERROR.getStatus(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        );
    }
}