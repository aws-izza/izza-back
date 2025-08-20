package com.izza.exception;

import com.izza.search.presentation.dto.response.BaseApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseApiResponse<Object>> handleBusinessException(BusinessException e) {
        log.warn("Business exception occurred: {}", e.getMessage());
        return ResponseEntity.status(e.getHttpStatus())
                .body(BaseApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<BaseApiResponse<Object>> handleDatabaseException(DatabaseException e) {
        log.error("Database exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getHttpStatus())
                .body(BaseApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<BaseApiResponse<Object>> handleDataAccessException(DataAccessException e) {
        log.error("Data access exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("데이터베이스 접근 중 오류가 발생했습니다"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument exception occurred: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BaseApiResponse<Object>> handleIllegalStateException(IllegalStateException e) {
        log.warn("Illegal state exception occurred: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseApiResponse<Object>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation exception occurred: {}", e.getMessage());
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("입력 값이 유효하지 않습니다");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseApiResponse.error(message));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseApiResponse<Object>> handleMissingParameterException(MissingServletRequestParameterException e) {
        log.warn("Missing parameter exception occurred: {}", e.getMessage());
        String message = String.format("필수 파라미터가 누락되었습니다: %s", e.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseApiResponse.error(message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseApiResponse<Object>> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch exception occurred: {}", e.getMessage());
        String message = String.format("파라미터 타입이 올바르지 않습니다: %s", e.getName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseApiResponse.error(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseApiResponse<Object>> handleGeneralException(Exception e) {
        log.error("Unexpected exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("내부 서버 오류가 발생했습니다"));
    }
}