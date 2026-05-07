package com.example.groupbuyingweb.core.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private int status;       // HTTP 상태 코드 (예: 200, 400, 404, 500)
    private String message;   // 결과 메시지 ("성공", "존재하지 않는 공구입니다" 등)
    private T data;           // 실제 전달할 데이터 (DTO가 여기에 들어감, 에러면 null)

    // 1. 성공했을 때 (데이터가 있는 경우)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "요청에 성공하였습니다.", data);
    }

    // 2. 성공했지만 전달할 데이터는 없는 경우 (예: 삭제 완료)
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(200, message, null);
    }

    // 3. 에러가 발생했을 때
    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}