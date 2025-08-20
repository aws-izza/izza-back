package com.izza.search.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "기본 API 응답 래퍼")
public class BaseApiResponse<T> {
    @Schema(description = "성공 여부")
    private boolean success;
    
    @Schema(description = "응답 데이터")
    private T data;
    
    @Schema(description = "에러 메시지")
    private String message;

    public static <T> BaseApiResponse<T> ok() {
        return new BaseApiResponse<>(true, null, null);
    }

    public static <T> BaseApiResponse<T> ok(T data) {
        return new BaseApiResponse<>(true, data, null);
    }
    
    public static <T> BaseApiResponse<T> error(String message) {
        return new BaseApiResponse<>(false, null, message);
    }
}
