# V2 Phase 6 — 3D Drag / Snap / 2D-3D 同步

## 完成内容

- 新增 DragController 与独立 dragState 状态机：idle、dragging、pending、rolling-back。
- pointerdown 通过 Parcel Raycaster/SceneIndex 选中业务 ID；pointermove raycast Slot targets。
- 可用目标绿色、不可用目标红色；只允许 enabled 且未占用 Slot。
- 拖动过程为 Mesh preview；pointerup 后调用 WarehouseStore.relocate，最终状态来自 Java relocate transaction。
- 成功时保留 slot snap 并由服务器 Parcel/version 更新共享 store，Warehouse 2D 与详情立即同步。
- 无目标/API 失败使用 300ms 插值恢复原位置；409 由 store rollback 后刷新服务器 snapshot，DigitalTwin 依据 lastSyncAt 重建。
- 拖动期间禁用 OrbitControls，结束后恢复，避免相机与对象手势冲突。
- dispose 移除全部 pointer listeners，并延续 RAF、controls、ResizeObserver、geometry/material/renderer 与 SceneIndex 清理。
- PICKED_UP Parcel 在 2D slot mapping 和 3D build 中均过滤，服务器刷新后同时消失。

## 测试

- `npm run type-check`：成功。
- `npm run test`：5 files / 12 tests 全部通过；新增合法目标进入 pending、无效目标 rollback、idle 不可变测试。
- `npm run build`：成功，621 modules；bundle gzip 526.90 kB，Phase 7 分包处理。
- `mvn clean test`：33 tests 全部通过，后端 relocate/rollback/409 回归保持绿色。

## 关键文件

- `frontend/src/three/DragController.ts`
- `frontend/src/three/dragState.ts`
- `frontend/src/three/dragState.test.ts`
- `frontend/src/three/WarehouseScene.ts`
- `frontend/src/views/DigitalTwinView.vue`

## 风险与下一阶段

- 自动测试覆盖纯状态和真实服务端事务；WebGL 指针手感、相机与拖拽的人工 smoke 留在 Phase 8。
- Phase 7 接入 GLB loader、Analytics、路由分包、性能 smoke、响应式和可访问性完善。
