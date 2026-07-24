-- 로컬 통합 테스트 전용. 기존 로컬 데이터를 모두 삭제합니다.
-- 모든 계정 비밀번호: password123!
USE jpa_fitness;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;

DELETE FROM review_reports;
DELETE FROM review_replies;
DELETE FROM reviews;
DELETE FROM comments;
DELETE FROM posts;
DELETE FROM reservation;
DELETE FROM program_trainer;
DELETE FROM program;
DELETE FROM user_user_roles;
DELETE FROM refresh_tokens;
DELETE FROM users;
DELETE FROM user_roles;

INSERT INTO user_roles (id, role_name) VALUES
  (1, 'ROLE_USER'), (2, 'ROLE_TRAINER'), (3, 'ROLE_ADMIN');

INSERT INTO users (id, email, password, name, created_at) VALUES
  (1, 'admin@test.com', '$2a$10$QOWvCmvrEEZizoCDKRcM2OpQILXcd/oLgUQJhbCLbufDv5qcljHiu', '관리자', NOW()),
  (2, 'trainer1@test.com', '$2a$10$QOWvCmvrEEZizoCDKRcM2OpQILXcd/oLgUQJhbCLbufDv5qcljHiu', '김트레이너', NOW()),
  (3, 'trainer2@test.com', '$2a$10$QOWvCmvrEEZizoCDKRcM2OpQILXcd/oLgUQJhbCLbufDv5qcljHiu', '이코치', NOW()),
  (4, 'user1@test.com', '$2a$10$QOWvCmvrEEZizoCDKRcM2OpQILXcd/oLgUQJhbCLbufDv5qcljHiu', '박회원', NOW()),
  (5, 'user2@test.com', '$2a$10$QOWvCmvrEEZizoCDKRcM2OpQILXcd/oLgUQJhbCLbufDv5qcljHiu', '최회원', NOW()),
  (6, 'user3@test.com', '$2a$10$QOWvCmvrEEZizoCDKRcM2OpQILXcd/oLgUQJhbCLbufDv5qcljHiu', '정회원', NOW());

INSERT INTO user_user_roles (user_id, role_id) VALUES
  (1, 1), (1, 3), (2, 1), (2, 2), (3, 1), (3, 2), (4, 1), (5, 1), (6, 1);

INSERT INTO program
  (id, title, type, capacity, start_at, end_at, description, status, created_at, updated_at)
VALUES
  (1, '초급 웨이트 트레이닝', 'GROUP', 10,
   TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00'),
   TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '11:00:00'),
   '기구 사용법과 기본 자세를 배우는 수업입니다.', 'OPEN', NOW(), NOW()),
  (2, '저녁 다이어트 서킷', 'GROUP', 8,
   TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '19:00:00'),
   TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '20:00:00'),
   '유산소와 근력 운동을 결합한 수업입니다.', 'OPEN', NOW(), NOW()),
  (3, '모닝 요가', 'GROUP', 12,
   TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '08:00:00'),
   TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:00:00'),
   '호흡과 스트레칭 중심 수업입니다.', 'OPEN', NOW(), NOW()),
  (4, '1:1 퍼스널 트레이닝', 'PT', 1,
   TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '18:30:00'),
   TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '19:30:00'),
   '개인 맞춤 1:1 수업입니다.', 'OPEN', NOW(), NOW()),
  (5, '[완료] 코어 강화 클래스', 'GROUP', 6,
   TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '18:00:00'),
   TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '19:00:00'),
   '리뷰 테스트용 완료 수업입니다.', 'COMPLETED', NOW(), NOW());

INSERT INTO program_trainer (id, program_id, trainer_id, assignment_role, assigned_at) VALUES
  (1, 1, 2, 'MAIN', NOW()), (2, 2, 2, 'MAIN', NOW()),
  (3, 3, 3, 'MAIN', NOW()), (4, 4, 3, 'MAIN', NOW()),
  (5, 5, 2, 'MAIN', NOW()), (6, 1, 3, 'ASSISTANT', NOW());

INSERT INTO reservation (id, user_id, program_id, status, attendance_status, created_at) VALUES
  (1, 4, 1, 'APPROVED', 'NOT_CHECKED', NOW()),
  (2, 5, 1, 'PENDING', 'NOT_CHECKED', NOW()),
  (3, 6, 1, 'PENDING', 'NOT_CHECKED', NOW()),
  (4, 4, 3, 'APPROVED', 'NOT_CHECKED', NOW()),
  (5, 5, 2, 'CANCELED', 'NOT_CHECKED', NOW()),
  (6, 4, 5, 'APPROVED', 'ATTENDED', NOW()),
  (7, 5, 5, 'APPROVED', 'NO_SHOW', NOW());

INSERT INTO posts (id, writer_id, category, title, content, view_count, created_at, updated_at) VALUES
  (1, 5, 'QUESTION', '예약 승인까지 얼마나 걸리나요?', '대기 중인 예약은 언제 승인되나요?', 12, NOW(), NOW()),
  (2, 1, 'NOTICE', '센터 이용 안내', '수업 시작 10분 전까지 입장해 주세요.', 34, NOW(), NOW()),
  (3, 6, 'QUESTION', '예약 취소 방법 문의', '승인 전후 취소 방법이 궁금합니다.', 5, NOW(), NOW());

INSERT INTO comments (id, post_id, writer_id, content, created_at, updated_at) VALUES
  (1, 1, 2, '트레이너 확인 후 승인됩니다.', NOW(), NOW()),
  (2, 1, 4, '저도 같은 프로그램을 신청했어요.', NOW(), NOW()),
  (3, 3, 1, '승인 전에는 즉시 취소됩니다.', NOW(), NOW());

INSERT INTO reviews
  (id, reservation_id, user_id, program_id, trainer_id, rating, content, status, created_at, updated_at)
VALUES
  (1, 6, 4, 5, 2, 5, '자세 교정을 꼼꼼히 봐주셔서 좋았습니다.', 'VISIBLE', NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;
SELECT '테스트 데이터 입력 완료' AS result;
