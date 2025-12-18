-- ============================================================================
-- KTB Hackathon 8 - 회식 메뉴 추천 시스템
-- Database Schema DDL
-- ============================================================================
-- Author: KTB Team 8
-- Created: 2024
-- Database: PostgreSQL
-- Description: 회식 장소 추천을 위한 그룹, 사용자, 제출 정보 관리 스키마
-- ============================================================================

-- ============================================================================
-- Sequences
-- ============================================================================

-- 사용자 시퀀스
CREATE SEQUENCE IF NOT EXISTS user_seq
    START WITH 1
    INCREMENT BY 100
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 그룹 시퀀스
CREATE SEQUENCE IF NOT EXISTS group_seq
    START WITH 1
    INCREMENT BY 100
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 그룹 멤버 시퀀스
CREATE SEQUENCE IF NOT EXISTS group_member_seq
    START WITH 1
    INCREMENT BY 100
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 제출 정보 시퀀스
CREATE SEQUENCE IF NOT EXISTS userSubmission_seq
    START WITH 1
    INCREMENT BY 100
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- ============================================================================
-- Tables
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Table: user_identifier
-- Description: 사용자 계정 정보
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_identifier (
    id BIGINT PRIMARY KEY DEFAULT nextval('user_seq'),
    username VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,

    CONSTRAINT uk_username UNIQUE (username)
);

COMMENT ON TABLE user_identifier IS '사용자 계정 정보';
COMMENT ON COLUMN user_identifier.id IS '사용자 ID (PK)';
COMMENT ON COLUMN user_identifier.username IS '로그인 아이디 (고유)';
COMMENT ON COLUMN user_identifier.nickname IS '사용자 닉네임';
COMMENT ON COLUMN user_identifier.password IS '암호화된 비밀번호';

-- ----------------------------------------------------------------------------
-- Table: submit_group
-- Description: 회식 그룹 정보 (장소, 예산, 일정 등)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS submit_group (
    id BIGINT PRIMARY KEY DEFAULT nextval('group_seq'),
    max_capacity INTEGER NOT NULL DEFAULT 0,
    budget INTEGER NOT NULL DEFAULT 0,
    station VARCHAR(255) NOT NULL DEFAULT '',
    has_scheduled_date BOOLEAN NOT NULL DEFAULT FALSE,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    owner_id BIGINT,

    CONSTRAINT fk_group_owner
        FOREIGN KEY (owner_id)
        REFERENCES user_identifier(id)
        ON DELETE SET NULL
);

COMMENT ON TABLE submit_group IS '회식 그룹 정보';
COMMENT ON COLUMN submit_group.id IS '그룹 ID (PK)';
COMMENT ON COLUMN submit_group.max_capacity IS '최대 참여 인원';
COMMENT ON COLUMN submit_group.budget IS '총 예산';
COMMENT ON COLUMN submit_group.station IS '모임 장소 (역 이름)';
COMMENT ON COLUMN submit_group.has_scheduled_date IS '일정 확정 여부';
COMMENT ON COLUMN submit_group.start_date IS '시작 날짜';
COMMENT ON COLUMN submit_group.end_date IS '종료 날짜';
COMMENT ON COLUMN submit_group.owner_id IS '그룹 생성자(총무) ID (FK)';

-- ----------------------------------------------------------------------------
-- Table: group_member
-- Description: 그룹 멤버 관계 (그룹-사용자 다대다 관계)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS group_member (
    id BIGINT PRIMARY KEY DEFAULT nextval('group_member_seq'),
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_owner BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_group_member_group
        FOREIGN KEY (group_id)
        REFERENCES submit_group(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_group_member_user
        FOREIGN KEY (user_id)
        REFERENCES user_identifier(id)
        ON DELETE CASCADE,
    CONSTRAINT uk_group_user
        UNIQUE (group_id, user_id)
);

COMMENT ON TABLE group_member IS '그룹 멤버 관계';
COMMENT ON COLUMN group_member.id IS '그룹 멤버 ID (PK)';
COMMENT ON COLUMN group_member.group_id IS '그룹 ID (FK)';
COMMENT ON COLUMN group_member.user_id IS '사용자 ID (FK)';
COMMENT ON COLUMN group_member.is_owner IS '그룹 소유자(총무) 여부';

-- ----------------------------------------------------------------------------
-- Table: submission
-- Description: 사용자별 메뉴 선호도 제출 정보
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS submission (
    id BIGINT PRIMARY KEY DEFAULT nextval('userSubmission_seq'),
    group_id BIGINT NOT NULL,
    nickname VARCHAR(255) NOT NULL,
    preferred_foods TEXT,
    avoided_foods TEXT,
    excluded_foods TEXT,

    CONSTRAINT fk_submission_group
        FOREIGN KEY (group_id)
        REFERENCES submit_group(id)
        ON DELETE CASCADE,
    CONSTRAINT uk_submission_nickname
        UNIQUE (group_id, nickname)
);

COMMENT ON TABLE submission IS '사용자별 메뉴 선호도 제출 정보';
COMMENT ON COLUMN submission.id IS '제출 정보 ID (PK)';
COMMENT ON COLUMN submission.group_id IS '그룹 ID (FK)';
COMMENT ON COLUMN submission.nickname IS '제출자 닉네임';
COMMENT ON COLUMN submission.preferred_foods IS '선호 음식';
COMMENT ON COLUMN submission.avoided_foods IS '피하고 싶은 음식';
COMMENT ON COLUMN submission.excluded_foods IS '절대 제외 음식 (알레르기 등)';

-- ----------------------------------------------------------------------------
-- Table: event_date
-- Description: 제출 정보별 제외 날짜 (참석 불가능한 날짜)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS event_date (
    id BIGINT PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    submission_id BIGINT NOT NULL,
    date_value TIMESTAMP,

    CONSTRAINT fk_event_date_submission
        FOREIGN KEY (submission_id)
        REFERENCES submission(id)
        ON DELETE CASCADE
);

COMMENT ON TABLE event_date IS '제출 정보별 제외 날짜';
COMMENT ON COLUMN event_date.id IS '이벤트 날짜 ID (PK)';
COMMENT ON COLUMN event_date.submission_id IS '제출 정보 ID (FK)';
COMMENT ON COLUMN event_date.date_value IS '제외 날짜';

-- ============================================================================
-- Indexes
-- ============================================================================

-- 사용자 계정 인덱스
CREATE INDEX IF NOT EXISTS idx_user_username ON user_identifier(username);
CREATE INDEX IF NOT EXISTS idx_user_nickname ON user_identifier(nickname);

-- 그룹 인덱스
CREATE INDEX IF NOT EXISTS idx_group_owner ON submit_group(owner_id);
CREATE INDEX IF NOT EXISTS idx_group_station ON submit_group(station);
CREATE INDEX IF NOT EXISTS idx_group_dates ON submit_group(start_date, end_date);

-- 그룹 멤버 인덱스
CREATE INDEX IF NOT EXISTS idx_group_member_group ON group_member(group_id);
CREATE INDEX IF NOT EXISTS idx_group_member_user ON group_member(user_id);

-- 제출 정보 인덱스
CREATE INDEX IF NOT EXISTS idx_submission_group ON submission(group_id);
CREATE INDEX IF NOT EXISTS idx_submission_nickname ON submission(nickname);

-- 이벤트 날짜 인덱스
CREATE INDEX IF NOT EXISTS idx_event_date_submission ON event_date(submission_id);
CREATE INDEX IF NOT EXISTS idx_event_date_value ON event_date(date_value);

-- ============================================================================
-- Sample Data (Optional - for development only)
-- ============================================================================

-- Sample User
-- INSERT INTO user_identifier (id, username, nickname, password)
-- VALUES (1, 'admin', '관리자', '$2a$10$...');

-- Sample Group
-- INSERT INTO submit_group (id, max_capacity, budget, station, has_scheduled_date, owner_id)
-- VALUES (1, 5, 100000, '강남역', false, 1);

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================
