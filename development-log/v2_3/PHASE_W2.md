# Phase W2 — Auto Layout, Resize & Safety Rules

## 目标与实施计划

- 提取 `ShelfAutoLayoutService`，以确定性 XZ footprint 算法支持 GRID、WIDE_MAIN_AISLE、TWO_SIDED_AISLE。
- 将 shelf gap、aisle gap、每排最大数量建模为校验后的 typed 配置；preview 与 apply 共用算法。
- 在 `ShelfManagementService` 中加入事务化 move、resize、enable/disable；所有 mutation 写 OperationLog。
- 扩容只创建缺失矩阵 Slot；缩容只软停用越界空 Slot；任何 active parcel 占用均拒绝并 rollback。
- 增加 ADMIN API 与 service/API 自动测试，随后运行 Java/前端完整质量门。

## 阶段输入审计

- W1 commit `f9562c4` 已推送，worktree 干净。
- ShelfLayout connection-aware upsert 已可安全复用。
- Parcel 以 `slot_id`/`shelf_id` 绑定业务位置，移动 ShelfLayout 不需要更新 Parcel。
- 现有 `WarehouseLayoutService.updateLayout` 非事务且不检测碰撞；W2 新结构 mutation 将集中进入 `ShelfManagementService`，旧 API 保留兼容。

## 完成内容

- 新增 `ShelfAutoLayoutService`、`ShelfAutoLayoutRequest`、`ShelfLayoutMode`，支持 GRID、WIDE_MAIN_AISLE、TWO_SIDED_AISLE，配置每排数量、货架间距和通道宽度。
- 自动布局基于 XZ AABB footprint，按现有布局最大 Z 外扩，候选之间和候选/现有布局之间均拒绝重叠；同输入产生相同坐标。
- W1 create/preview 已切换为自动布局算法，并支持请求指定模式和间距；新增 auto-layout preview/apply API 别名。
- `ShelfManagementService` 新增事务化 move、resize、enable/disable；move/resize 检测碰撞并记录日志。
- 扩容只补齐不存在的 level/column 组合；缩容保留历史 Slot 并软停用越界仓位，原 Slot ID 不变。
- 缩容命中 IN_STOCK/EXCEPTION parcel、停用含 active parcel 的货架均拒绝，事务 rollback 保持布局与 Slot 快照不变。
- 新增 ADMIN-only `PUT /api/admin/shelves/{id}`、`/{id}/layout`、`/{id}/enabled` 以及 `/api/admin/warehouse/auto-layout/{preview|apply}`。
- `ShelfSlotDaoImpl` 增加 connection-aware shelf 查询，确保 resize 的读写处于同一事务。

## 验证

- `mvn -q test`：60 tests 全部通过。
- `mvn -Pintegration-test verify`：构建与 60 个单元/API 测试通过；MySQL IT 2 个因无 `PARCEL_DB_*` skipped。
- `npm run type-check`：通过。
- `npm run test`：14 files / 37 tests 通过。
- `npm run build`：通过；仅有既有 chunk size warning。
- 新测试覆盖三种布局确定性/无碰撞、wide aisle 明确净宽、现有 footprint 避让、move 成功/碰撞 rollback、5×6→6×8、空缩容软停用、占用缩容与停用拒绝、STAFF 403。
- 浏览器：W2 未新增前端界面；随机端口 HTTP API 测试覆盖权限与结构 mutation。真实 UI 操作在 W3 完成。
- 修复记录：API 测试起初使用不带 ParcelDao 的兼容构造器，导致占用停用校验未获得 parcel 真相；改为与生产一致的完整注入后通过。

## 未完成项与下一阶段输入

- 真实 MySQL V2.3 integration 仍待 W5 环境验收。
- W3 使用本阶段 server preview/apply/move/resize/enable API 建设 Settings 布局管理器，并验证 WarehouseStore 驱动 2D/3D 同源刷新与 scene 生命周期。
