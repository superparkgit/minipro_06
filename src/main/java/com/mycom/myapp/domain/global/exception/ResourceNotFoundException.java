package com.mycom.myapp.domain.global.exception;

/**
 * 요청한 리소스를 찾을 수 없을 때 발생. (404 Not Found)
 * 예: 존재하지 않는 게시글, 사용자, 예약 등
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super("존재하지 않는 " + resourceName + "입니다. id=" + id);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}