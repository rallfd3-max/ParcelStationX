# V2 Phase 4 — 2D Warehouse 拖拽作业中心

## 完成内容

- `/warehouse` 已替换占位页，形成待上架列表、二维货架/Slot 网格与快件详情三栏工作区。
- WarehouseStore 从真实 `/api/warehouse` 初始化 shelves/layouts/slots/parcels，共享 selected parcel、loading、error、notice 与同步时间。
- 支持运单号/快递公司搜索、待上架筛选、Parcel 选择和 Slot 占用映射。
- HTML5 drag/drop 实现 preview -> relocate API -> server response commit；pending 状态阻止重复提交。
- 失败恢复原对象；409 后重新拉取服务器 snapshot，以服务器状态为准并显示明确提示。
- Slot 以空闲、占用、可放、不可放、停用的颜色和文字双重反馈。
- 提供无拖拽替代：选择 Parcel -> 选择空闲仓位 -> 确认移动。
- 详情栏展示 tracking、courier、status、slot、arrivedAt、version、remark。
- 1366px 下保持三栏可操作并允许中间画布滚动。

## 测试

- `npm run type-check`：成功。
- `npm run test`：3 files / 7 tests 全部通过。
- 新增 WarehouseStore 3 项：snapshot/成功 commit、409 rollback + refresh、occupied target 本地拒绝。
- `npm run build`：成功，605 modules；保留 Phase 7 处理的 chunk size warning。
- `mvn clean test`：33 tests 全部通过。

## 关键文件

- `frontend/src/stores/warehouse.ts`
- `frontend/src/stores/warehouse.test.ts`
- `frontend/src/views/WarehouseView.vue`
- `frontend/src/types/api.ts`
- `frontend/src/router/index.ts`
- `frontend/src/styles/main.css`

## 问题、修复、风险与下一阶段

- 所有自动化质量门首次通过，无测试删除或跳过。
- 当前无真实 MySQL/浏览器人工环境，因此持久化由 Phase 2 的真实 HTTP + H2 JDBC transaction test 证明；Phase 8 仍需人工全链路确认刷新后位置不丢失。
- Phase 5 可复用 WarehouseStore 作为 3D 唯一前端状态源；Three.js 模块必须保持拆分，本阶段未引入 Three.js。
