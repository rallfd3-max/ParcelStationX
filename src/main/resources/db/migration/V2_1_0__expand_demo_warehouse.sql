-- ParcelStationX V2.1 forward-only expansion. Safe to run repeatedly after V2_0_1.
INSERT INTO shelves (shelf_code, zone_name, capacity, occupied, status, created_at)
VALUES
    ('A-01', 'A区', 30, 0, 'ACTIVE', NOW()), ('A-02', 'A区', 30, 0, 'ACTIVE', NOW()),
    ('B-01', 'B区', 30, 0, 'ACTIVE', NOW()), ('B-02', 'B区', 30, 0, 'ACTIVE', NOW()),
    ('C-01', 'C区', 30, 0, 'ACTIVE', NOW()), ('C-02', 'C区', 30, 0, 'ACTIVE', NOW()),
    ('D-01', 'D区', 30, 0, 'ACTIVE', NOW()), ('D-02', 'D区', 30, 0, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE zone_name=VALUES(zone_name),capacity=VALUES(capacity),status='ACTIVE';

INSERT INTO shelf_layout
    (shelf_id,position_x,position_y,position_z,rotation_y,width,height,depth,columns_count,levels_count,updated_at)
SELECT s.id,p.x,0,p.z,p.rotation_y,3.6,2.6,0.8,6,5,NOW()
FROM shelves s
JOIN (
    SELECT 'A-01' code,-6.0 x,-3.0 z,0.0 rotation_y UNION ALL
    SELECT 'A-02',-2.0,-3.0,0.0 UNION ALL
    SELECT 'B-01', 2.0,-3.0,0.0 UNION ALL
    SELECT 'B-02', 6.0,-3.0,0.0 UNION ALL
    SELECT 'C-01',-6.0, 2.0,3.141593 UNION ALL
    SELECT 'C-02',-2.0, 2.0,3.141593 UNION ALL
    SELECT 'D-01', 2.0, 2.0,3.141593 UNION ALL
    SELECT 'D-02', 6.0, 2.0,3.141593
) p ON p.code=s.shelf_code
ON DUPLICATE KEY UPDATE position_x=VALUES(position_x),position_y=VALUES(position_y),position_z=VALUES(position_z),
    rotation_y=VALUES(rotation_y),width=VALUES(width),height=VALUES(height),depth=VALUES(depth),
    columns_count=VALUES(columns_count),levels_count=VALUES(levels_count),updated_at=VALUES(updated_at);

INSERT IGNORE INTO shelf_slots (shelf_id,slot_code,level_index,column_index,enabled,created_at)
WITH RECURSIVE levels(n) AS (SELECT 0 UNION ALL SELECT n+1 FROM levels WHERE n<4),
columns(n) AS (SELECT 0 UNION ALL SELECT n+1 FROM columns WHERE n<5)
SELECT s.id,CONCAT(s.shelf_code,'-',LPAD(levels.n+1,2,'0'),'-',LPAD(columns.n+1,2,'0')),
       levels.n,columns.n,TRUE,NOW()
FROM shelves s CROSS JOIN levels CROSS JOIN columns
WHERE s.shelf_code IN ('A-01','A-02','B-01','B-02','C-01','C-02','D-01','D-02');

UPDATE shelves s
SET s.occupied=(SELECT COUNT(*) FROM shelf_slots ss JOIN parcels p ON p.slot_id=ss.id
                WHERE ss.shelf_id=s.id AND p.status IN ('IN_STOCK','EXCEPTION'))
WHERE s.shelf_code IN ('A-01','A-02','B-01','B-02','C-01','C-02','D-01','D-02');
