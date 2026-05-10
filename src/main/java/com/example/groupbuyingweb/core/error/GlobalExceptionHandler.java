package com.example.groupbuyingweb.core.error;

import com.example.groupbuyingweb.core.api.ApiResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    protected ApiResponse<?> handleBusinessException(BusinessException e) {
        return ApiResponse.error(e.getErrorCode().getStatus(), e.getMessage());
    }

    // IllegalStateException
    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/alertAndBack"; // 사용자에게 알림창 띄우고 뒤로가기 하는 뷰(html)로 이동
    }

    // IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404"; // 404 페이지로 이동
    }
}
