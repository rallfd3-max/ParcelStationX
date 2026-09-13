USE parcel_station_x;

INSERT IGNORE INTO users (id, username, password_hash, display_name, role, enabled, created_at)
VALUES
    (1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '系统管理员', 'ADMIN', TRUE, NOW()),
    (2, 'staff01', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '员工一', 'STAFF', TRUE, NOW()),
    (3, 'staff02', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '员工二', 'STAFF', TRUE, NOW());

INSERT IGNORE INTO shelves (id, shelf_code, zone_name, capacity, occupied, status, created_at)
VALUES
    (1, 'A-01', 'A区', 30, 0, 'ACTIVE', NOW()),
    (2, 'B-01', 'B区', 30, 0, 'ACTIVE', NOW()),
    (3, 'C-01', 'C区', 20, 0, 'ACTIVE', NOW());

INSERT IGNORE INTO shelf_layout
    (shelf_id, position_x, position_y, position_z, rotation_y, width, height, depth, columns_count, levels_count, updated_at)
VALUES
    (1, -4.0, 0.0, -1.5, 0.0, 3.0, 2.4, 0.8, 6, 5, NOW()),
    (2,  0.0, 0.0, -1.5, 0.0, 3.0, 2.4, 0.8, 6, 5, NOW()),
    (3,  4.0, 0.0, -1.5, 0.0, 2.5, 2.4, 0.8, 5, 4, NOW());

INSERT IGNORE INTO shelf_slots (id, shelf_id, slot_code, level_index, column_index, enabled, created_at)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 80)
SELECT n,
       CASE WHEN n <= 30 THEN 1 WHEN n <= 60 THEN 2 ELSE 3 END,
       CASE WHEN n <= 30 THEN CONCAT('A-01-', LPAD(FLOOR((n - 1) / 6) + 1, 2, '0'), '-', LPAD(MOD(n - 1, 6) + 1, 2, '0'))
            WHEN n <= 60 THEN CONCAT('B-01-', LPAD(FLOOR((n - 31) / 6) + 1, 2, '0'), '-', LPAD(MOD(n - 31, 6) + 1, 2, '0'))
            ELSE CONCAT('C-01-', LPAD(FLOOR((n - 61) / 5) + 1, 2, '0'), '-', LPAD(MOD(n - 61, 5) + 1, 2, '0')) END,
       CASE WHEN n <= 30 THEN FLOOR((n - 1) / 6) WHEN n <= 60 THEN FLOOR((n - 31) / 6) ELSE FLOOR((n - 61) / 5) END,
       CASE WHEN n <= 30 THEN MOD(n - 1, 6) WHEN n <= 60 THEN MOD(n - 31, 6) ELSE MOD(n - 61, 5) END,
       TRUE, NOW()
FROM seq;

INSERT IGNORE INTO customers (id, name, mobile, building, room, remark, created_at, updated_at)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 20)
SELECT 100 + n, CONCAT('演示客户', n), CONCAT('1390000', LPAD(n, 4, '0')),
       CONCAT(1 + MOD(n, 5), '栋'), CONCAT(100 + n), '批量演示数据', NOW(), NOW()
FROM seq;

INSERT IGNORE INTO parcels
    (id, tracking_no, courier_company, customer_id, shelf_id, pickup_code, status,
     arrived_at, picked_up_at, operator_id, remark, created_at, updated_at)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 50)
SELECT 1000 + n, CONCAT('DEMO', LPAD(n, 6, '0')),
       ELT(1 + MOD(n, 4), '顺丰', '中通', '圆通', '韵达'),
       100 + 1 + MOD(n - 1, 20), 1 + MOD(n - 1, 3), LPAD(200000 + n, 6, '0'),
       IF(n <= 10, 'EXCEPTION', 'IN_STOCK'), DATE_SUB(NOW(), INTERVAL MOD(n, 12) DAY),
       NULL, 1 + MOD(n, 3), '批量演示快件', NOW(), NOW()
FROM seq;

UPDATE parcels
SET slot_id = id - 1000,
    shelf_id = CASE WHEN id <= 1030 THEN 1 ELSE 2 END,
    version = 0
WHERE id BETWEEN 1001 AND 1050;

UPDATE shelves SET occupied = CASE id WHEN 1 THEN 30 WHEN 2 THEN 20 WHEN 3 THEN 0 ELSE occupied END
WHERE id IN (1, 2, 3);

INSERT IGNORE INTO exception_records
    (id, parcel_id, exception_type, description, status, created_by, created_at)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 10)
SELECT 2000 + n, 1000 + n, IF(MOD(n, 2) = 0, 'OVERDUE', 'DAMAGED'),
       CONCAT('演示异常场景 ', n), 'OPEN', 1 + MOD(n, 3), NOW()
FROM seq;

INSERT IGNORE INTO operation_logs
    (id, user_id, operation_type, target_type, target_id, description, created_at)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 30)
SELECT 3000 + n, 1 + MOD(n, 3), 'DEMO_SEED', 'PARCEL', 1000 + 1 + MOD(n - 1, 50),
       CONCAT('演示操作日志 ', n), NOW()
FROM seq;
