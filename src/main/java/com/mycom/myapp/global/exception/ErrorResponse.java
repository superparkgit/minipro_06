package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {

    private final int status;
    private final String error;
    private final String message;

    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
            .status(errorCode.getHttpStatus().value())
            .error(errorCode.getHttpStatus().getReasonPhrase())
            .message(errorCode.getMessage())
            .build();
    }

    public static ErrorResponse of(HttpStatus httpStatus, String message) {
        return ErrorResponse.builder()
            .status(httpStatus.value())
            .error(httpStatus.getReasonPhrase())
            .message(message)
            .build();
    }
}
