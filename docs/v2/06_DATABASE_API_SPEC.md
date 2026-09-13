# V2 数据库与 REST API 规范

## 1. 数据库升级原则

- 使用 migration SQL，禁止直接破坏现有 schema。
- 现有表与数据必须可以迁移。
- 所有新外键/唯一约束必须有对应测试。
- Parcel 的 3D 位置不保存任意 x/y/z，而通过 `slot_id` 映射。

## 2. 新增表

### `shelf_layout`

建议字段：

```text
shelf_id BIGINT PK/FK
position_x DECIMAL
position_y DECIMAL
position_z DECIMAL
rotation_y DECIMAL
width DECIMAL
height DECIMAL
depth DECIMAL
columns_count INT
levels_count INT
updated_at DATETIME
```

用于描述货架在数字孪生场景中的 transform 和网格结构。

### `shelf_slots`

```text
id BIGINT PK
shelf_id BIGINT FK
slot_code VARCHAR UNIQUE
level_index INT
column_index INT
enabled BOOLEAN
created_at DATETIME
```

建议唯一约束：`(shelf_id, level_index, column_index)`。

### `parcel_relocations`

```text
id BIGINT PK
parcel_id BIGINT FK
from_slot_id BIGINT NULL
new_slot_id BIGINT NULL
operator_id BIGINT FK
reason VARCHAR
created_at DATETIME
```

### `parcels` 扩展

```text
slot_id BIGINT NULL FK shelf_slots(id)
version BIGINT NOT NULL DEFAULT 0
```

是否新增 `WAITING_SLOT` 状态由 Phase 2 审计决定。若不新增，则 `IN_STOCK + slot_id IS NULL` 作为“待上架”，必须写入文档并统一代码语义。

## 3. Slot 占用一致性

数据库的真实占用来源应尽量是 `parcels.slot_id`，不要同时维护多个容易漂移的 slot occupied 字段。

如果保留 shelves.occupied 作为统计缓存，则任何上架/移动/出库必须在同一事务中维护，并增加一致性测试：

```text
shelves.occupied == COUNT(current parcels assigned to slots under shelf)
```

## 4. Relocate 事务

请求：

```json
{
  "targetSlotId": 123,
  "expectedVersion": 5,
  "reason": "manual drag"
}
```

服务端流程：

1. 查询 Parcel；
2. 校验当前状态允许移动；
3. 比较 expectedVersion；
4. 查询 target Slot；
5. 校验 enabled；
6. 查询 target Slot 是否已有活动 Parcel；
7. 查询 from Slot / Shelf 与 to Shelf；
8. 更新 parcel.slot_id；
9. `version = version + 1`；
10. 必要时维护旧/新 shelf occupied；
11. 写 ParcelRelocation；
12. 写 ParcelEvent；
13. 写 OperationLog；
14. commit。

任意失败 rollback。

冲突返回 409：

- `PARCEL_VERSION_CONFLICT`；
- `SLOT_OCCUPIED`。

## 5. REST API

### Auth

```text
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/me
```

### Dashboard

```text
GET /api/dashboard/summary
GET /api/dashboard/trends?days=30
```

### Warehouse

```text
GET /api/warehouse
GET /api/warehouse/shelves
GET /api/warehouse/slots
GET /api/warehouse/layouts
```

`GET /api/warehouse` 可以返回前端初始化所需聚合快照，但要控制 payload。

### Parcels

```text
GET  /api/parcels
GET  /api/parcels/{id}
POST /api/parcels/inbound
POST /api/parcels/{id}/relocate
POST /api/parcels/{id}/outbound
GET  /api/parcels/{id}/events
GET  /api/parcels/{id}/relocations
```

查询支持：trackingNo、status、courier、shelfId、slotId、customerMobile、page/pageSize。

### Exceptions

```text
GET  /api/exceptions
POST /api/parcels/{id}/exception
POST /api/exceptions/{id}/resolve
```

### Statistics

```text
GET /api/statistics/summary
GET /api/statistics/trends
GET /api/statistics/shelf-utilization
GET /api/statistics/dwell
```

### Admin

```text
GET/POST/PUT /api/admin/users...
GET/PUT      /api/admin/layouts...
POST         /api/admin/backup
POST         /api/admin/restore
```

Backup/restore 是否直接通过 Web 暴露必须评估文件上传实现；如果超出课程需要，可在 V2 第一版保留 Swing/本地管理员操作，但前端要明确能力边界，不能放假按钮。

## 6. 响应格式

成功：

```json
{
  "success": true,
  "data": {},
  "message": null,
  "code": null
}
```

失败：

```json
{
  "success": false,
  "data": null,
  "message": "目标仓位已占用",
  "code": "SLOT_OCCUPIED"
}
```

## 7. DTO 规则

- API 不直接序列化 DAO/内部异常；
- 手机号在非必要页面默认脱敏；
- 日期统一 ISO-8601 字符串；
- enum 使用稳定字符串；
- Three.js 所需 transform 使用明确数值字段；
- response 中 Parcel 必须携带 `version`。

## 8. 登录与权限

ADMIN：全部业务、用户、布局、备份等。

STAFF：Dashboard、Warehouse、Digital Twin、Parcels、Exceptions、Analytics；禁止用户/布局敏感修改和备份恢复。

权限必须在服务端检查。

## 9. HTTP Server

建议组件：

```text
ApiServer
Router
Route
RequestContext
JsonCodec
ApiResponse
SessionManager
AuthFilter/AuthGuard
ExceptionMapper
```

Handler 不允许直接执行 SQL。

## 10. 测试要求

新增自动测试至少覆盖：

- migration 后现有 seed 可用；
- slot 唯一约束；
- relocate success；
- occupied slot -> 409；
- stale version -> 409；
- invalid state -> 422/业务错误；
- rollback 后 parcel/slot/shelf/event/log 均不产生半状态；
- STAFF 访问 admin API -> 403；
- 未登录 -> 401；
- login -> me -> logout；
- HTTP JSON error format 一致。