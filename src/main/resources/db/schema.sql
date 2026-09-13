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

CREATE TABLE IF NOT EXISTS shelf_layout (
    shelf_id BIGINT PRIMARY KEY,
    position_x DECIMAL(10,3) NOT NULL, position_y DECIMAL(10,3) NOT NULL, position_z DECIMAL(10,3) NOT NULL,
    rotation_y DECIMAL(10,6) NOT NULL DEFAULT 0,
    width DECIMAL(10,3) NOT NULL, height DECIMAL(10,3) NOT NULL, depth DECIMAL(10,3) NOT NULL,
    columns_count INT NOT NULL, levels_count INT NOT NULL, updated_at DATETIME NOT NULL,
    CONSTRAINT fk_layout_shelf FOREIGN KEY (shelf_id) REFERENCES shelves(id),
    CHECK (width > 0), CHECK (height > 0), CHECK (depth > 0),
    CHECK (columns_count > 0), CHECK (levels_count > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS shelf_slots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT, shelf_id BIGINT NOT NULL,
    slot_code VARCHAR(50) NOT NULL UNIQUE, level_index INT NOT NULL, column_index INT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE, created_at DATETIME NOT NULL,
    CONSTRAINT fk_slot_shelf FOREIGN KEY (shelf_id) REFERENCES shelves(id),
    CONSTRAINT uq_slot_grid UNIQUE (shelf_id, level_index, column_index),
    CHECK (level_index >= 0), CHECK (column_index >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS parcels (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tracking_no VARCHAR(100) NOT NULL UNIQUE, courier_company VARCHAR(50) NOT NULL,
    customer_id BIGINT NOT NULL, shelf_id BIGINT NULL, pickup_code VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL, arrived_at DATETIME NOT NULL, picked_up_at DATETIME NULL,
    operator_id BIGINT NOT NULL, remark VARCHAR(255), created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
    slot_id BIGINT NULL, version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_parcel_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_parcel_shelf FOREIGN KEY (shelf_id) REFERENCES shelves(id),
    CONSTRAINT fk_parcel_operator FOREIGN KEY (operator_id) REFERENCES users(id),
    CONSTRAINT fk_parcel_slot FOREIGN KEY (slot_id) REFERENCES shelf_slots(id),
    CONSTRAINT uq_parcel_active_slot UNIQUE (slot_id),
    INDEX idx_pickup_code (pickup_code), INDEX idx_parcel_status (status), INDEX idx_parcel_customer (customer_id),
    INDEX idx_parcel_shelf (shelf_id), INDEX idx_arrived_at (arrived_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS parcel_relocations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT, parcel_id BIGINT NOT NULL,
    from_slot_id BIGINT NULL, new_slot_id BIGINT NULL, operator_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL, created_at DATETIME NOT NULL,
    CONSTRAINT fk_relocation_parcel FOREIGN KEY (parcel_id) REFERENCES parcels(id),
    CONSTRAINT fk_relocation_from_slot FOREIGN KEY (from_slot_id) REFERENCES shelf_slots(id),
    CONSTRAINT fk_relocation_new_slot FOREIGN KEY (new_slot_id) REFERENCES shelf_slots(id),
    CONSTRAINT fk_relocation_operator FOREIGN KEY (operator_id) REFERENCES users(id),
    INDEX idx_relocation_parcel_time (parcel_id, created_at)
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
