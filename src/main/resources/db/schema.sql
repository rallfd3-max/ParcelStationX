CREATE DATABASE IF NOT EXISTS parcel_station_x CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE parcel_station_x;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    last_login_at DATETIME NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    mobile VARCHAR(20) NOT NULL,
    building VARCHAR(50), room VARCHAR(50), remark VARCHAR(255),
    created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
    INDEX idx_customer_mobile (mobile), INDEX idx_customer_name (name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS shelves (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shelf_code VARCHAR(30) NOT NULL UNIQUE, zone_name VARCHAR(30) NOT NULL,
    capacity INT NOT NULL, occupied INT NOT NULL DEFAULT 0, status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    CHECK (capacity > 0), CHECK (occupied >= 0), CHECK (occupied <= capacity)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS parcels (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tracking_no VARCHAR(100) NOT NULL UNIQUE, courier_company VARCHAR(50) NOT NULL,
    customer_id BIGINT NOT NULL, shelf_id BIGINT NULL, pickup_code VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL, arrived_at DATETIME NOT NULL, picked_up_at DATETIME NULL,
    operator_id BIGINT NOT NULL, remark VARCHAR(255), created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
    CONSTRAINT fk_parcel_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_parcel_shelf FOREIGN KEY (shelf_id) REFERENCES shelves(id),
    CONSTRAINT fk_parcel_operator FOREIGN KEY (operator_id) REFERENCES users(id),
    INDEX idx_pickup_code (pickup_code), INDEX idx_parcel_status (status), INDEX idx_parcel_customer (customer_id),
    INDEX idx_parcel_shelf (shelf_id), INDEX idx_arrived_at (arrived_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS parcel_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT, parcel_id BIGINT NOT NULL, event_type VARCHAR(30) NOT NULL,
    from_status VARCHAR(30), to_status VARCHAR(30), operator_id BIGINT, description VARCHAR(255), created_at DATETIME NOT NULL,
    CONSTRAINT fk_event_parcel FOREIGN KEY (parcel_id) REFERENCES parcels(id),
    CONSTRAINT fk_event_operator FOREIGN KEY (operator_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS exception_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT, parcel_id BIGINT NOT NULL, exception_type VARCHAR(30) NOT NULL,
    description VARCHAR(500), status VARCHAR(20) NOT NULL, created_by BIGINT NOT NULL, handled_by BIGINT NULL,
    created_at DATETIME NOT NULL, handled_at DATETIME NULL, resolution VARCHAR(500),
    CONSTRAINT fk_exception_parcel FOREIGN KEY (parcel_id) REFERENCES parcels(id),
    CONSTRAINT fk_exception_creator FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_exception_handler FOREIGN KEY (handled_by) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS notification_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT, parcel_id BIGINT NOT NULL, customer_id BIGINT NOT NULL,
    notification_type VARCHAR(30) NOT NULL, target VARCHAR(100) NOT NULL, content VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL, retry_count INT NOT NULL DEFAULT 0, created_at DATETIME NOT NULL,
    sent_at DATETIME NULL, error_message VARCHAR(255),
    CONSTRAINT fk_notification_parcel FOREIGN KEY (parcel_id) REFERENCES parcels(id),
    CONSTRAINT fk_notification_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT, operation_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(50), target_id BIGINT, description VARCHAR(500), created_at DATETIME NOT NULL,
    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;
