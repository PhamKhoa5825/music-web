package com.example.music_web.exception;

import com.example.music_web.Entity.User;
import com.example.music_web.dto.ApiResponse;
import com.example.music_web.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice // Dùng @RestControllerAdvice nếu bạn làm thuần API
@RequiredArgsConstructor // Inject LogService
public class GlobalExceptionHandler {

    // 1. Bắt lỗi Validation (Ví dụ: password quá ngắn)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidation(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError().getDefaultMessage();

        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setCode(400);
        apiResponse.setMessage(enumKey); // Trả về message lỗi

        return ResponseEntity.badRequest().body(apiResponse);
    }

    private final SystemLogService logService;

    // Helper method để lấy user hiện tại (nếu có)
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null; // Khách vãng lai hoặc chưa login
    }

    // 1. Bắt lỗi Runtime (Lỗi hệ thống 500)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleRuntimeException(RuntimeException exception) {
        // Ghi log lỗi
        User currentUser = getCurrentUser();
        String errorMsg = exception.getMessage();

        // Nếu có user thì ghi log gắn với user đó
        if (currentUser != null) {
            logService.log(currentUser, "SYSTEM_ERROR", "Error: " + errorMsg);
        }

        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setCode(500);
        apiResponse.setMessage(errorMsg);
        return ResponseEntity.badRequest().body(apiResponse);
    }


    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException exception) {
        ApiResponse apiResponse = new ApiResponse();
        ErrorCode errorCode = exception.getErrorCode();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(exception.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    // Bạn có thể thêm các Exception khác tùy chỉnh tại đây
}