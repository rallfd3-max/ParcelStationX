# V2.1.1 Phase H2 — Parcel Camera Focus Correctness

## 完成内容

- 新增 `WarehouseScene.focusParcel(parcelId)`，不再由 Vue 把 slotId 当作 Parcel 聚焦参数。
- 新增 `resolveParcelFocus`，严格解析 `parcelId -> Parcel -> slotId -> ShelfSlot -> shelfId -> ShelfLayout -> Object3D`。
- Parcel Object3D 在聚焦前执行 `updateWorldMatrix(true,false)`，并以 `getWorldPosition()` 作为精确 OrbitControls target。
- CameraController 新增 `focusParcelWorld`，货架 rotationY 只决定靠近方向，不覆盖目标位置。
- 点击/拖拽选中回调同时选择同一 parcelId 并调用场景聚焦，右侧详情、Mesh 与相机语义一致。
- 实现其他货架旋转后 AABB 的线段遮挡检测，候选顺序为正面、正面左右偏移、反面兜底。

## 自动测试

- 同一货架第一列/第六列 target.x 不同。
- 不同货架 target 不同。
- 前排 rotationY=0 从 +Z 通道侧靠近；后排 rotationY=PI 从 -Z 通道侧靠近；rotationY=PI/2 从正确 X 方向靠近。
- target 与 Parcel Mesh 世界坐标误差小于 1e-8。
- trackingNo、shelfId、slotCode 与聚焦解析结果一致。
- 传入 slotId 而非 parcelId 时明确返回 null。
- `npm run type-check`、30 个前端测试、`npm run build` 全部通过。

## 发现并修复

- 首轮测试发现遮挡策略在默认正面被同列货架挡住后会过早选反面；调整候选排序，优先通道侧横向偏移。

## 下一阶段

- H3 使用真实浏览器检查图表 DOM 类型标记及六个代表性快件的详情/聚焦链路。
