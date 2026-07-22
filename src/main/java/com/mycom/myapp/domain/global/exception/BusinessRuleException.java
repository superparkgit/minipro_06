package com.mycom.myapp.domain.global.exception;

/**
 * 비즈니스 규칙을 위반했을 때 발생. (409 Conflict)
 * 예: 이미 리뷰를 작성한 예약, 중복 답변 등록, 잘못된 상태 전이 등
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
