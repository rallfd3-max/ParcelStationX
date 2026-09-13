# V2 Phase 2 数据语义决策

## 待上架唯一语义

V2 统一使用 `Parcel.status = IN_STOCK AND Parcel.slot_id IS NULL` 表示“已入库、待上架”。不新增 `WAITING_SLOT` 枚举。

- `slot_id` 是空间占用的唯一真实来源。
- `shelf_id` 是兼容旧系统的派生/缓存字段：有 slot 时必须等于该 slot 的 shelf_id；待上架时为 NULL。
- `shelves.occupied` 保留作为统计缓存，只统计已分配 slot 的在库快件，并由上架、换位、出库事务同步维护。
- Parcel 的世界坐标不入库，由 `slot_id -> shelf_slots -> shelf_layout` 计算。

## 并发策略

relocate 使用 Parcel.version 乐观锁、目标 Slot 行锁、目标占用查询及 `parcels.slot_id` 唯一约束。成功事务同时更新 Parcel、货架占用缓存、ParcelRelocation、ParcelEvent 和 OperationLog；任何异常整体回滚。
