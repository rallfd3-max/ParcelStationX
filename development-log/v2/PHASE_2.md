# V2 Phase 2 — ShelfLayout / Slot / Relocation 数据模型与事务

## 目标与完成情况

Phase 2 已完成。新增数字孪生布局/仓位/移动模型、JDBC DAO、V2 migration、seed、warehouse snapshot 与 relocate REST API。H2 MySQL mode 已验证事务、约束、冲突和回滚；真实 MySQL 仍按计划在 Phase 8 验收。

## 唯一业务语义

待上架唯一表示为 `IN_STOCK + slot_id IS NULL`，不新增 WAITING_SLOT。slot_id 是空间占用真相；有 slot 时 shelf_id 必须等于 slot.shelf_id。shelves.occupied 只统计已占 Slot 的实体快件并在 relocate/outbound 事务维护。详见 `docs/v2/08_PHASE_2_DATA_DECISIONS.md`。

## 数据库与模型

- 新增 ShelfLayout、ShelfSlot、ParcelRelocation。
- Parcel 新增 slotId/version；保留兼容构造器供 legacy 代码和旧序列化测试逐步迁移。
- 新增 `V2_0_1__digital_twin_layout.sql`，只做向前 CREATE/ALTER，不删除旧数据。
- schema.sql 支持全新 V2 安装；seed.sql 创建 3 个 layout、80 个 slot，并将 50 个演示快件一致映射到 slot/shelf。
- `parcels.slot_id` UNIQUE 防止一个 Slot 被多个快件占用；slot_code 与 shelf/level/column 均唯一。

## DAO 与 Service

- 新增 ShelfLayoutDaoImpl、ShelfSlotDaoImpl、ParcelRelocationDaoImpl，全程 PreparedStatement/try-with-resources。
- ParcelDaoImpl 新增 slot 查询、`FOR UPDATE` 查询及 `WHERE id=? AND version=?` 乐观锁更新。
- WarehouseLayoutService 返回 shelves/layouts/slots/parcels 聚合快照。
- RelocationService 在单个 TransactionRunner 中完成：状态/version 校验、目标 slot 行锁、占用校验、parcel 更新、跨货架 occupied 更新、relocation/event/log 写入。
- 入库现在创建待上架快件，不提前占用货架；出库清空 slot 并减少实际货架占用。
- ExceptionService 保留 slot 并递增 version，避免状态流转覆盖空间版本。

## REST API

- `GET /api/warehouse`
- `POST /api/parcels/{id}/relocate`
- `GET /api/parcels/{id}/relocations`

relocate 请求为 targetSlotId/expectedVersion/reason。版本冲突或仓位占用返回 409；非法业务状态返回 422；响应含最新 Parcel.version。

## 测试与修复

- 首轮回归：32 tests，2 failures。原因是旧 ParcelServiceTest 仍断言“入库即占用货架”。测试按已批准的待上架语义更新，并保留重复运单、停用货架、错误取件码、重复出库和事务回滚覆盖。
- API 首轮：warehouse JSON 因 Java time module 未注册返回 500；新增 Jackson JSR-310 module 并注册，修复日期序列化。
- 最终 `mvn clean test`：33 tests，0 failures，0 errors，0 skipped，BUILD SUCCESS。
- 新增 6 个 Phase 2 测试，覆盖 DAO/slot grid 唯一约束、relocate success、occupied、stale version、invalid state、外键失败整体 rollback、真实 HTTP warehouse/relocate/409 流程。
- `mvn spotless:apply`：成功。

## 关键修改文件

- `src/main/resources/db/schema.sql`
- `src/main/resources/db/seed.sql`
- `src/main/resources/db/migration/V2_0_1__digital_twin_layout.sql`
- `src/main/java/com/parcelstationx/model/{ShelfLayout,ShelfSlot,ParcelRelocation,Parcel}.java`
- `src/main/java/com/parcelstationx/dao/*` 与 `dao/impl/*`
- `src/main/java/com/parcelstationx/service/{WarehouseLayoutService,RelocationService}.java`
- `src/main/java/com/parcelstationx/api/http/ApiServer.java`
- `src/main/java/com/parcelstationx/api/dto/*`
- `src/test/java/com/parcelstationx/service/RelocationServiceTest.java`

## 未完成项、风险与下一阶段输入

- 当前主机无 MySQL CLI，未声称完成真实 MySQL migration；Phase 8 必须执行 `mvn -Pintegration-test verify` 和 migration 人工核验。
- V1 backup snapshot 尚未扩展为独立保存 layout/slot/relocation；现有 Parcel slot/version 会随 Parcel 序列化。完整 V2 backup/restore 边界在 settings/final phase 再审计。
- Phase 3 可以创建 frontend，并通过 Bearer token 接真实 login、warehouse/statistics API；不得用 mock 代替 Dashboard 最终数据。
