# V2 Phase 5 — Three.js Digital Twin 基础

## 完成内容

- 新增 `/digital-twin`，从真实 WarehouseStore/API snapshot 构建环境、货架、Slot 与 Parcel。
- Three.js 拆分为 WarehouseScene、WarehouseRenderer、CameraController、SceneIndex、ShelfBuilder、SlotBuilder、ParcelRenderer、ParcelInteraction、EnvironmentLoader、sceneMath。
- OrbitControls 支持旋转、缩放、平移；Raycaster 点击 Parcel 通过 SceneIndex 返回真实 parcelId。
- 搜索/点击可调用 focusParcel；同时实现 focusShelf、focusSlot、resetCamera、topView、frontView 和 cancelAnimation，使用 800ms ease-out。
- Parcel 位置严格由 slotId -> ShelfSlot -> ShelfLayout -> world transform 计算，无任意业务 XYZ。
- 正常/异常 Parcel 使用蓝青/红色；选中详情与 Pinia selectedParcel 同步。
- GLB loader 以 EnvironmentLoader 接口预留，本阶段使用可靠 primitive floor fallback。
- 页面卸载取消 RAF/相机动画、移除 pointer、断开 ResizeObserver、dispose controls/geometry/material/renderer 并清空 SceneIndex。

## 测试

- `npm run type-check`：成功（修复 WarehouseScene class field 与 method 间缺少分号的首轮语法错误后通过）。
- `npm run test`：4 files / 10 tests 全部通过；新增 slot world transform、rotation/front/camera target、SceneIndex child hit/clear 测试。
- `npm run build`：成功，620 modules；bundle gzip 526.16 kB，Phase 7 必须路由分包。
- Java 本阶段无生产改动，最近完整 `mvn clean test` 为 33/33 通过。

## 关键文件

- `frontend/src/three/*`
- `frontend/src/views/DigitalTwinView.vue`
- `frontend/src/router/index.ts`
- `frontend/src/three/sceneMath.test.ts`

## 未完成项与下一阶段

- Phase 5 按要求未实现完整 3D drag。
- Phase 6 在 ParcelInteraction 之上加入 DragController、Slot raycast feedback、snap preview 与 WarehouseStore relocate action，并保持现有 dispose 边界。
- WebGL 人工视觉验收留在 Phase 8；纯计算和生产构建已自动验证。
