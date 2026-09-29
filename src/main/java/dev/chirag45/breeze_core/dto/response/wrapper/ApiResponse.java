package dev.chirag45.breeze_core.dto.response.wrapper;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final int code;
    private final String message;
    private final String errorCode;
    private final T data;

    private ApiResponse(int code, String message, String errorCode, T data) {
        this.code = code;
        this.message = message;
        this.errorCode = errorCode;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Success");
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(1, message, null, data);
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return error(message, errorCode, null);
    }

    public static <T> ApiResponse<T> error(String message, String errorCode, T data) {
        return new ApiResponse<>(0, message, errorCode, data);
    }
}
