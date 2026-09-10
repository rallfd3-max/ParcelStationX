# 数据库设计草案

数据库：`parcel_station_x`

## 1. users

```text
id BIGINT PK AI
username VARCHAR(50) UNIQUE NOT NULL
password_hash VARCHAR(255) NOT NULL
display_name VARCHAR(50) NOT NULL
role VARCHAR(20) NOT NULL
enabled BOOLEAN NOT NULL
created_at DATETIME NOT NULL
last_login_at DATETIME NULL
```

## 2. customers

```text
id BIGINT PK AI
name VARCHAR(50) NOT NULL
mobile VARCHAR(20) NOT NULL
building VARCHAR(50)
room VARCHAR(50)
remark VARCHAR(255)
created_at DATETIME NOT NULL
updated_at DATETIME NOT NULL
```

索引：

- idx_customer_mobile
- idx_customer_name

## 3. shelves

```text
id BIGINT PK AI
shelf_code VARCHAR(30) UNIQUE NOT NULL
zone VARCHAR(30) NOT NULL
capacity INT NOT NULL
occupied INT NOT NULL DEFAULT 0
status VARCHAR(20) NOT NULL
created_at DATETIME NOT NULL
```

约束逻辑：

- capacity > 0
- occupied >= 0
- occupied <= capacity

## 4. parcels

```text
id BIGINT PK AI
tracking_no VARCHAR(100) UNIQUE NOT NULL
courier_company VARCHAR(50) NOT NULL
customer_id BIGINT NOT NULL
shelf_id BIGINT NULL
pickup_code VARCHAR(20) NOT NULL
status VARCHAR(30) NOT NULL
arrived_at DATETIME NOT NULL
picked_up_at DATETIME NULL
operator_id BIGINT NOT NULL
remark VARCHAR(255)
created_at DATETIME NOT NULL
updated_at DATETIME NOT NULL
```

索引：

- uk_tracking_no
- idx_pickup_code
- idx_parcel_status
- idx_parcel_customer
- idx_parcel_shelf
- idx_arrived_at

## 5. parcel_events

```text
id BIGINT PK AI
parcel_id BIGINT NOT NULL
event_type VARCHAR(30) NOT NULL
from_status VARCHAR(30)
to_status VARCHAR(30)
operator_id BIGINT
description VARCHAR(255)
created_at DATETIME NOT NULL
```

## 6. exception_records

```text
id BIGINT PK AI
parcel_id BIGINT NOT NULL
exception_type VARCHAR(30) NOT NULL
description VARCHAR(500)
status VARCHAR(20) NOT NULL
created_by BIGINT NOT NULL
handled_by BIGINT NULL
created_at DATETIME NOT NULL
handled_at DATETIME NULL
resolution VARCHAR(500)
```

## 7. notification_records

```text
id BIGINT PK AI
parcel_id BIGINT NOT NULL
customer_id BIGINT NOT NULL
notification_type VARCHAR(30) NOT NULL
target VARCHAR(100) NOT NULL
content VARCHAR(500) NOT NULL
status VARCHAR(20) NOT NULL
retry_count INT NOT NULL DEFAULT 0
created_at DATETIME NOT NULL
sent_at DATETIME NULL
error_message VARCHAR(255)
```

## 8. operation_logs

```text
id BIGINT PK AI
user_id BIGINT
operation_type VARCHAR(50) NOT NULL
target_type VARCHAR(50)
target_id BIGINT
description VARCHAR(500)
created_at DATETIME NOT NULL
```

## 9. 外键策略

建议正式 schema 使用外键，但删除核心记录时尽量使用业务状态而不是物理删除。

例如：

- 用户禁用，不直接删除；
- 货架停用，不直接删除；
- 快件保留历史，不物理删除。

## 10. 初始演示账号

seed.sql 中生成：

```text
admin / 初始密码（README 明确）
staff01
staff02
```

注意：

实际代码中应使用哈希存储；课程项目可自己用 JDK `MessageDigest` 实现 SHA-256 + salt，不引入安全框架。
