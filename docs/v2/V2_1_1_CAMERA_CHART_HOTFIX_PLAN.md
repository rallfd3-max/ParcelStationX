# ParcelStationX V2.1.1 — 3D 镜头定位与真实统计图热修计划

## 1. 背景

V2.1 浏览器实测发现两类问题：

1. 首页虽然存在“趋势/分布”区域，但当前 `DataChart.vue` 只注册 `BarChart` 且 `series.type` 固定为 `bar`，因此入库/出库/库存趋势并非真实折线图，快递公司/状态/异常分布也并非真实饼图。
2. 3D 数字孪生中，点击第二排/第二列货架上的快件后，镜头存在定位到最前排/最前面的视觉错误。相机定位必须与实际被点击的 Parcel Mesh 世界坐标一致，并正确处理不同 `ShelfLayout.rotationY`、前后排货架以及遮挡关系。

本热修禁止重写现有 V2.1 架构，仅修正图表类型和相机定位正确性。

---

## 2. 首页图表增强

### 2.1 折线统计图

至少实现一张真实折线图，推荐将“流量与库存趋势”整合为一个多序列 Line Chart：

- 最近 7/14/30 天；
- 入库量；
- 出库量；
- 库存量；
- tooltip；
- legend；
- 平滑折线可选；
- resize/dispose；
- loading/empty 状态；
- 数据必须来自真实 dashboard trends API，不得 mock。

如库存量尺度明显不同，可采用双 yAxis 或拆为两张折线图，但不得继续用柱图冒充趋势。

### 2.2 饼图/环图

至少实现一张真实 Pie/Donut Chart，推荐：

- 快递公司占比；
- Parcel 状态占比；
- 异常类型占比。

至少首页必须可见一张饼图。建议快递公司占比使用 donut，显示：

- legend；
- 百分比；
- tooltip；
- 空数据状态；
- 总量中心文字（可选）。

数据必须来自 `dashboard/distributions`。

### 2.3 组件结构

禁止继续用只支持柱状图的 `DataChart.vue` 承担所有类型。

可选方案：

- 将 `DataChart.vue` 改造成 typed chart component，支持 `type: 'bar' | 'line' | 'pie' | 'donut'`；或
- 新建 `TrendLineChart.vue`、`DistributionPieChart.vue`、`UtilizationBarChart.vue`。

无论采用哪种，都必须只注册实际需要的 ECharts chart/component，正确 dispose。

---

## 3. 3D Parcel 镜头定位修复

### 3.1 正确性原则

点击任意 Parcel 后，镜头必须聚焦到该 Parcel 所在真实 Shelf/Slot，而不是某个固定前排货架。

定位链路必须可追踪：

`parcelId -> Parcel -> slotId -> ShelfSlot -> shelfId -> ShelfLayout -> Parcel Object3D world position`

相机 target 优先使用 SceneIndex 中实际 Parcel Mesh 的 `getWorldPosition()`，避免渲染位置与重新计算位置出现漂移。

### 3.2 CameraController API

建议把现有：

`focusParcel(slotId)`

升级为真正的 Parcel 语义：

`focusParcel(parcelId, parcelWorldPosition, shelfLayout)`

或由 `WarehouseScene.focusParcel(parcelId)` 负责：

1. 从 SceneIndex 获取 Parcel Object3D；
2. `object.updateWorldMatrix(true, false)`；
3. `object.getWorldPosition(target)`；
4. 从业务数据找到 slot/shelf/layout；
5. 由 CameraController 根据 target + shelf front 计算视角；
6. 平滑飞行；
7. OrbitControls.target 最终精确落在 Parcel；
8. 选中快件保持高亮。

不要继续在 Vue 中把 `slotId` 当作 parcel focus 的唯一输入。

### 3.3 前后排与遮挡

当前 V2.1 货架存在两排：

- A/B：`z=-3`, `rotationY=0`
- C/D：`z=2`, `rotationY≈PI`

相机必须从对应货架面向通道的一侧靠近目标。

增加：

- 根据 `shelfFront(layout)` 计算目标货架正面；
- camera position 与 target 保持同一 slot 的 x/y 对齐，而不是总对齐 shelf center；
- 对后排/第二排货架进行遮挡检查；
- 若从默认 front 方向的 camera->target 射线被其他 shelf bounding box 阻挡，尝试反向或侧向偏移候选视角；
- 选择距离目标合理且不穿过货架的候选 camera position。

课程项目可实现简化版：至少保证 A-01/A-02/B-01/B-02/C-01/C-02/D-01/D-02 八组货架中的任意 Slot 都能聚焦到正确 Shelf，不被另一排货架遮住。

### 3.4 点击映射验证

检查 `ParcelInteraction` / `DragController` / `SceneIndex`：

- Raycaster 命中的 child mesh 必须能正确回溯到 parcelId；
- parcelId 不能误映射为 slotId；
- selectedParcelId 与点击对象必须一致；
- 点击后右侧详情的 trackingNo/shelf/slot 必须和镜头目标一致。

### 3.5 自动化测试

新增 scene/camera math 测试：

- 同一货架第 1 列和第 6 列产生不同 target.x；
- A-01 与 A-02 聚焦点不同；
- 前排 A/B 与后排 C/D 相机位于对应货架正面；
- C/D 不应最终聚焦到 A/B；
- camera target 与 Parcel Mesh world position 在容差内一致；
- rotationY=0 / PI / PI/2 场景正确；
- focusParcel 不接受错误的 parcelId/slotId 混用。

浏览器人工验收至少点击：

- 第一排左侧快件；
- 第一排右侧快件；
- 第二排左侧快件；
- 第二排右侧快件；
- 同一货架第 1 列；
- 同一货架最后一列。

每次镜头都必须飞到被点击快件对应的正确货架正面。

---

## 4. 质量门

必须执行：

- `mvn clean test`
- `cd frontend && npm run type-check`
- `npm run test`
- `npm run build`

真实 MySQL/浏览器可用时继续真实回归。

本热修完成前，不得仅通过调整 camera distance 或增加 CSS 来宣称解决。
