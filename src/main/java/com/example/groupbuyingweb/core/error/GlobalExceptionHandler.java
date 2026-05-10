package com.example.groupbuyingweb.core.error;

import com.example.groupbuyingweb.core.api.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    protected ApiResponse<?> handleBusinessException(BusinessException e) {
        return ApiResponse.error(e.getErrorCode().getStatus(), e.getMessage());
    }
}
