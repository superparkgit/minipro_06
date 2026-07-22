package com.mycom.myapp.domain.review.entity;

public enum ReviewStatus {
    VISIBLE,
    HIDDEN,       // 트레이너 신고 → 관리자 심사 대기 중
    USER_DELETED, // 작성자 본인이 직접 삭제
    ADMIN_DELETED // 관리자가 신고 승인으로 삭제
}
