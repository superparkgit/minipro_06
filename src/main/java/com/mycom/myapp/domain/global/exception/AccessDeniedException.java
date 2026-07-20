package com.mycom.myapp.domain.global.exception;

/**
 * 요청한 작업에 대한 권한이 없을 때 발생. (403 Forbidden)
 * 예: 본인이 아닌 게시글 수정, 트레이너가 아닌 사용자의 답변 등록 등
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}