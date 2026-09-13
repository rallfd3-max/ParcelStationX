# V2.1 Phase C — 3D Hover + Inbound/Outbound

## 完成内容

- 新增 3D 快件 hover raycast 控制器，并在拖拽状态下关闭 hover，避免交互冲突。
- 新增 HTML tooltip，显示运单号、快递公司、状态、货架/仓位、客户、脱敏手机号、到站时间和停留时长。
- 将 `slotId=null && status=IN_STOCK` 的快件渲染到 3D 入库暂存区，并加入 SceneIndex，因此可继续拖拽到真实仓位。
- 数字孪生页新增快速入库表单，调用现有 `ParcelService.inbound` API；成功后刷新 WarehouseStore 并选中新件。
- 右侧详情新增取件码输入与两步确认出库，调用现有 `ParcelService.outbound` API。
- 3D 换位、入库、出库成功后均刷新仓库快照与选中详情；Dashboard/2D 页面后续进入时读取相同实时数据。

## 新增/修改文件

- `frontend/src/three/HoverController.ts`
- `frontend/src/three/StagingParcelRenderer.ts`
- `frontend/src/features/parcelTooltip.ts`
- `frontend/src/features/parcelTooltip.test.ts`
- `frontend/src/three/DragController.ts`
- `frontend/src/three/WarehouseScene.ts`
- `frontend/src/views/DigitalTwinView.vue`
- `frontend/src/styles/main.css`
- `TASKS_V2_1.md`

## 测试结果

- `npm run type-check`：通过。
- `npm test -- --run`：10 个测试文件、25 个测试全部通过。
- `npm run build`：通过；仅保留既有 Three.js chunk 体积警告。
- `mvn clean test`：37 个 Java 测试全部通过。
- tooltip 单测覆盖暂存区、客户脱敏字段及小时/天停留时长。

## 真实浏览器验收

- 在数字孪生页创建运单 `V21C20260913`，页面提示已进入 3D 入库暂存区。
- 搜索定位后详情显示 `IN_STOCK`、`入库暂存区`、演示客户、脱敏手机号和取件码。
- 输入真实取件码后，第一步展示明确的二次确认提示；再次确认成功，状态刷新为 `PICKED_UP`。
- 内置浏览器不回传 WebGL 像素，因此 hover 像素命中和 3D 拖动由 Raycaster/SceneIndex 实现审查及既有拖拽状态、映射测试覆盖；页面运行无控制台错误。

## 发现的问题与修复

- 初版详情地址误写为 `/api/parcel-details/{id}`，真实浏览器返回接口不存在；修正为已注册的 `/api/parcels/{id}/details`。
- 原生 `confirm()` 会阻塞自动化浏览器输入通道；改为页面内两步确认，兼顾可测试性和误操作保护。

## 剩余风险 / 下一阶段影响

- WebGL 截图能力限制留待 Phase E 在可用浏览器能力范围内复核。
- Phase D 可直接复用本阶段统一刷新后的真实快件状态构建首页统计。
