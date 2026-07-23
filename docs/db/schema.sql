-- Fit Reserve - 트레이닝 프로그램 예약 서비스
-- 1차 머지 origin/main (37a432e) 기준 MySQL 8 스키마
-- 로컬 테스트 전용: 같은 이름의 기존 테이블을 삭제하고 다시 생성합니다.
CREATE DATABASE IF NOT EXISTS jpa_fitness
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE jpa_fitness;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS review_reports;
DROP TABLE IF EXISTS review_replies;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS program_trainer;
DROP TABLE IF EXISTS program;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS user_user_roles;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  email VARCHAR(255),
  password VARCHAR(255),
  name VARCHAR(255),
  created_at DATETIME(6),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE user_roles (
  id BIGINT NOT NULL AUTO_INCREMENT,
  role_name VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES user_roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refresh_tokens (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT,
  token VARCHAR(255),
  expires_at DATETIME(6),
  created_at DATETIME(6),
  PRIMARY KEY (id),
  CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE program (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  type VARCHAR(255) NOT NULL,
  capacity INT NOT NULL,
  start_at DATETIME(6) NOT NULL,
  end_at DATETIME(6) NOT NULL,
  description TEXT,
  status VARCHAR(255) NOT NULL,
  created_at DATETIME(6),
  updated_at DATETIME(6),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE program_trainer (
  id BIGINT NOT NULL AUTO_INCREMENT,
  program_id BIGINT NOT NULL,
  trainer_id BIGINT NOT NULL,
  assignment_role VARCHAR(20) NOT NULL,
  assigned_at DATETIME(6),
  PRIMARY KEY (id),
  CONSTRAINT uk_program_trainer UNIQUE (program_id, trainer_id),
  CONSTRAINT fk_program_trainer_program FOREIGN KEY (program_id) REFERENCES program (id),
  CONSTRAINT fk_program_trainer_user FOREIGN KEY (trainer_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reservation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  program_id BIGINT NOT NULL,
  status VARCHAR(255) NOT NULL,
  attendance_status VARCHAR(255) NOT NULL,
  created_at DATETIME(6),
  PRIMARY KEY (id),
  CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_reservation_program FOREIGN KEY (program_id) REFERENCES program (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE posts (
  id BIGINT NOT NULL AUTO_INCREMENT,
  writer_id BIGINT NOT NULL,
  category VARCHAR(255) NOT NULL,
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  view_count INT NOT NULL DEFAULT 0,
  created_at DATETIME(6),
  updated_at DATETIME(6),
  PRIMARY KEY (id),
  CONSTRAINT fk_post_writer FOREIGN KEY (writer_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE comments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  writer_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  created_at DATETIME(6),
  updated_at DATETIME(6),
  PRIMARY KEY (id),
  CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES posts (id),
  CONSTRAINT fk_comment_writer FOREIGN KEY (writer_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reviews (
  id BIGINT NOT NULL AUTO_INCREMENT,
  reservation_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  program_id BIGINT NOT NULL,
  trainer_id BIGINT NOT NULL,
  rating INT NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(255) NOT NULL,
  created_at DATETIME(6),
  updated_at DATETIME(6),
  PRIMARY KEY (id),
  CONSTRAINT uk_review_reservation UNIQUE (reservation_id),
  CONSTRAINT fk_review_reservation FOREIGN KEY (reservation_id) REFERENCES reservation (id),
  CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_review_program FOREIGN KEY (program_id) REFERENCES program (id),
  CONSTRAINT fk_review_trainer FOREIGN KEY (trainer_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE review_replies (
  id BIGINT NOT NULL AUTO_INCREMENT,
  review_id BIGINT NOT NULL,
  trainer_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  created_at DATETIME(6),
  updated_at DATETIME(6),
  PRIMARY KEY (id),
  CONSTRAINT uk_review_reply_review UNIQUE (review_id),
  CONSTRAINT fk_review_reply_review FOREIGN KEY (review_id) REFERENCES reviews (id),
  CONSTRAINT fk_review_reply_trainer FOREIGN KEY (trainer_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE review_reports (
  id BIGINT NOT NULL AUTO_INCREMENT,
  review_id BIGINT NOT NULL,
  reporter_id BIGINT NOT NULL,
  reason TEXT NOT NULL,
  status VARCHAR(255) NOT NULL,
  created_at DATETIME(6),
  resolved_at DATETIME(6),
  PRIMARY KEY (id),
  CONSTRAINT fk_review_report_review FOREIGN KEY (review_id) REFERENCES reviews (id),
  CONSTRAINT fk_review_report_user FOREIGN KEY (reporter_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
