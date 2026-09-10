USE parcel_station_x;

INSERT IGNORE INTO users (id, username, password_hash, display_name, role, enabled, created_at)
VALUES
    (1, 'admin', '240be518fabd2724ddb4a58c7cd1d87e7fe8d67ef77673428f122d04b0326a5b', '系统管理员', 'ADMIN', TRUE, NOW()),
    (2, 'staff01', '240be518fabd2724ddb4a58c7cd1d87e7fe8d67ef77673428f122d04b0326a5b', '员工一', 'STAFF', TRUE, NOW()),
    (3, 'staff02', '240be518fabd2724ddb4a58c7cd1d87e7fe8d67ef77673428f122d04b0326a5b', '员工二', 'STAFF', TRUE, NOW());

INSERT IGNORE INTO shelves (id, shelf_code, zone_name, capacity, occupied, status, created_at)
VALUES
    (1, 'A-01', 'A区', 30, 0, 'ACTIVE', NOW()),
    (2, 'B-01', 'B区', 30, 0, 'ACTIVE', NOW()),
    (3, 'C-01', 'C区', 20, 0, 'ACTIVE', NOW());
