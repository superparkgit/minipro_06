package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // 회원
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 역할입니다."),

    // 수업
    CLASS_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 수업입니다."),
    CLASS_ACCESS_DENIED(HttpStatus.FORBIDDEN, "수업 수정/삭제 권한이 없습니다."),

    // 예약
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 예약입니다."),
    DUPLICATE_RESERVATION(HttpStatus.CONFLICT, "이미 예약된 수업입니다."),
    CAPACITY_EXCEEDED(HttpStatus.CONFLICT, "정원이 초과되었습니다."),
    RESERVATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "예약 취소 권한이 없습니다."),
    ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "이미 취소된 예약입니다."),

    // 게시글/댓글
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "게시글 수정/삭제 권한이 없습니다."),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "댓글 삭제 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
