CREATE DATABASE IF NOT EXISTS model_route_agent
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE model_route_agent;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS conversation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    conversation_id VARCHAR(48) NOT NULL,
    title VARCHAR(120) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_conversation_business_id (conversation_id),
    KEY idx_conversation_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    task_type VARCHAR(20) NULL,
    model_id VARCHAR(100) NULL,
    route_json JSON NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_message_conversation_created (conversation_id, created_at, id),
    CONSTRAINT fk_message_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversation (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS conversation_id_sequence (
    sequence_id BIGINT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (sequence_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS file_operation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    operation_id VARCHAR(36) NOT NULL,
    conversation_id VARCHAR(48) NULL,
    root_id VARCHAR(100) NOT NULL,
    operation_type VARCHAR(32) NOT NULL,
    source_path VARCHAR(1024) NULL,
    target_path VARCHAR(1024) NULL,
    proposed_content MEDIUMTEXT NULL,
    before_content MEDIUMTEXT NULL,
    expected_before_hash VARCHAR(64) NULL,
    after_hash VARCHAR(64) NULL,
    approval_mode VARCHAR(20) NOT NULL,
    status VARCHAR(24) NOT NULL,
    error_message TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    approved_at DATETIME(6) NULL,
    executed_at DATETIME(6) NULL,
    rolled_back_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_operation_business_id (operation_id),
    KEY idx_file_operation_status_created (status, created_at),
    KEY idx_file_operation_executed (status, executed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
