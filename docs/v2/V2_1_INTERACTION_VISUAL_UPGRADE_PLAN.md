# ParcelStationX V2.1 — 交互与数字孪生增强项目计划书

## 1. 改造背景

当前 V2 已具备 Vue 3、Java HttpServer、JDBC/MySQL、二维仓位、Three.js 数字孪生、快件查询、异常、统计和系统管理等基础能力，但真实浏览器演示暴露出若干影响使用体验和答辩展示效果的问题：

1. 二维仓位作业中心中，中央货架区域在拖拽时无法方便地滚动到下方仓位，导致下方空仓位难以作为 drop target。
2. 3D 数字孪生的真实货架数量较少，整体空间感不足，不像真实快递驿站。
3. 鼠标悬停在 3D 快件上时没有快件信息提示，只能点击后看右侧面板。
4. 3D 数字孪生缺少直接入库、出库的业务入口，3D 与业务操作结合还不够紧密。
5. “驾驶舱”和“分析”页面功能重复，导航层级冗余；首页应该成为可持续向下滚动的智慧驿站数据大屏。
6. 首页统计内容偏少，需要补充折线图、饼图/环图、货架利用率、库存趋势、异常/滞留等分析。

本轮目标不是重写 V2，而是在保持现有 Java Service/DAO/JDBC、Vue/Pinia、Three.js 架构稳定的基础上，完成 V2.1 的交互、业务和可视化增强。

---

## 2. V2.1 总体目标

最终用户体验应形成以下闭环：

登录 → 首页总览 → 二维仓位作业 → 入库 → 拖拽上架 → 3D 数字孪生查看 → 悬停查看信息 → 点击定位 → 3D 换位 → 出库 → 首页统计即时刷新。

视觉定位继续保持：

- 深色工业数字孪生；
- 蓝青色正常态；
- 橙色选中态；
- 绿色可放置；
- 黄色滞留；
- 红色异常/冲突；
- 数据大屏与真实业务操作结合。

禁止退回传统白底 CRUD 后台视觉。

---

## 3. 模块 A：二维仓位拖拽与滚动修复

### 3.1 当前问题

当前 WarehouseView 使用浏览器 HTML5 Drag & Drop。中央 `.shelf-canvas` 虽然设置 `overflow:auto`，但没有稳定的独立 viewport 高度，也没有拖拽临近边缘时的自动滚动逻辑。

结果：

- 页面可滚动，但拖拽中的鼠标很难把中央仓库移动到下方；
- 下方空仓位虽存在，却无法顺畅成为目标；
- 货架越多，问题越严重。

### 3.2 改造要求

将二维仓库改成三栏稳定作业区：

- 左：待上架快件；
- 中：独立可滚动货架画布；
- 右：选中快件详情与“移动到...”替代操作。

中央货架画布必须：

- `max-height`/`height` 基于可视区域计算；
- `overflow-y:auto`；
- 可正常使用鼠标滚轮；
- 拖拽过程中，当指针进入容器顶部/底部约 60px 区域时自动向上/向下滚动；
- 自动滚动速度随指针接近边缘而增加，但必须设置最大速度；
- 指针离开边缘后立即停止自动滚动；
- dragend/drop/cancel 后清理状态和 RAF/timer；
- 不能因为自动滚动导致重复 drop 或页面整体跳动。

建议新增：

`frontend/src/features/warehouseDragScroll.ts`

提供纯函数/控制器用于：

- 计算 edge scroll direction/speed；
- 启停 requestAnimationFrame；
- 在 drop/dragend 时释放。

同时保留无障碍替代路径：右侧“移动到...”下拉选择空闲 Slot 后确认移动。

### 3.3 验收

在 1366×768 下：

- A/B/C/... 多组货架超过一屏；
- 从左侧待上架拖拽快件；
- 指针悬停在中央货架画布底部时自动下滚；
- 能成功拖到最下方空仓位；
- 服务端 relocate 成功后刷新仍保持位置；
- 409 时回滚。

---

## 4. 模块 B：扩展真实 3D 快递驿站

### 4.1 原则

禁止为了“看起来多”只在 Three.js 前端复制假货架。

3D 中的业务货架必须继续来源于：

`Shelf -> ShelfLayout -> ShelfSlot -> Parcel`

前端只渲染后端真实数据。

### 4.2 推荐规模

默认演示仓库从当前少量货架扩展为 8 组业务货架：

- A-01 / A-02
- B-01 / B-02
- C-01 / C-02
- D-01 / D-02

建议每组 5 层 × 6 列 = 30 Slot，最终约 240 Slot。

如果当前数据库已经有用户数据，新增 migration 必须是增量、安全、可重复检查的，不得 DROP 现有数据。

推荐新增：

`src/main/resources/db/migration/V2_1_0__expand_demo_warehouse.sql`

需要同时添加：

- 新 shelves；
- 对应 shelf_layout；
- 对应 shelf_slots；
- 不移动现有 Parcel，除非明确做安全的数据迁移。

### 4.3 空间布局

3D 场景布局建议：

- 四个区域 A/B/C/D；
- 每区两组长货架；
- 货架之间保留主通道和次通道；
- 入口附近设置“待上架区”；
- 出口/服务台设置“取件区”；
- 地面增加区域标识、通道线和安全边界；
- 场景增加墙面/地面/服务台/入口作为环境，不影响业务 Shelf 动态生成。

Camera reset/top/front 必须根据所有 `ShelfLayout` 的 bounding box 自适应，不得继续假定只有 3 个货架。

### 4.4 性能

240 Slot + 50~200 Parcel 必须保持流畅。

若普通 Mesh 数量明显增加，应评估 InstancedMesh，但不要为了优化而破坏 parcelId/slotId 的拾取映射。

---

## 5. 模块 C：3D Hover 快件信息

### 5.1 当前问题

现有 `ParcelInteraction` 主要是 pointerup 点击拾取，并没有 hover tooltip；WarehouseScene 也没有完整接入 hover interaction。

### 5.2 新交互

鼠标移动到 3D Parcel 上时：

- 鼠标指针变为 pointer；
- 快件出现轻微高亮/描边；
- Canvas 上方显示 HTML Tooltip；
- tooltip 随鼠标移动，但不能超出 canvas 边界；
- pointerleave 时消失；
- dragging 状态下禁用普通 hover，避免冲突。

Tooltip 至少显示：

- 运单号；
- 快递公司；
- 状态；
- 区域 / 货架 / Slot；
- 收件人姓名；
- 脱敏手机号；
- 到站时间；
- 停留时长。

点击 Parcel 后继续保留右侧详情面板，并执行 persistent selection。

建议增强：

`ParcelInteraction.ts`

增加：

- `pointermove` raycast；
- hover enter/move/leave callback；
- cursor state；
- RAF throttle，避免每个 pointermove 做高成本重复计算。

新增 Vue 层：

`ParcelHoverTooltip.vue`

不得把 tooltip 内容作为 Three.js TextGeometry 绘制。

---

## 6. 模块 D：3D 页面直接入库与出库

### 6.1 入库

Digital Twin 顶部增加“快速入库”按钮，打开抽屉或 Modal。

字段至少：

- trackingNo；
- customer/mobile；
- courierCompany；
- remark（可选）。

必须复用现有 Java `ParcelService.inbound` 业务规则，不得在 Handler 重写业务逻辑。

如果缺少 Web API，新增：

`POST /api/parcels/inbound`

成功后：

- refresh WarehouseStore；
- 新 Parcel 作为 `slotId = null` 的待上架快件；
- 3D 场景在入口“待上架区”显示该 Parcel；
- 可从待上架区拖到真实 Slot，继续走 relocate transaction。

### 6.2 3D 待上架区

当前 3D 只绘制有 slot 的 Parcel。本轮新增 staging/inbound zone。

为 `slotId === null && status === IN_STOCK` 的 Parcel 生成临时场景位置，但这只是显示位置，不写入数据库 world XYZ。

推荐新增：

- `StagingZoneBuilder.ts`
- `WaitingParcelRenderer.ts`

等待快件仍然通过 parcelId 映射到真实业务对象，拖到 Slot 后调用 relocate API。

### 6.3 出库

选中在库快件后，右侧详情增加“出库”动作。

要求：

- 输入/确认 pickupCode；
- 二次确认；
- 调用服务端出库 API；
- 必须复用现有 `ParcelService.outbound` transaction；
- 成功后 refresh WarehouseStore；
- Parcel 从 2D/3D 场景中消失；
- Shelf occupied/Slot 状态/OperationLog/ParcelEvent 同步；
- Dashboard 首页数据同步更新。

如果 API 缺失，新增：

`POST /api/parcels/{id}/outbound`

禁止只在前端删除 Mesh。

---

## 7. 模块 E：首页重构：驾驶舱 + 分析合并

### 7.1 导航

当前：

- 驾驶舱
- 分析

改为：

- 首页

删除顶部单独“分析”入口。

为了兼容旧 URL：

`/analytics` 可以 redirect 到 `/dashboard#analytics`，不要直接 404。

### 7.2 首页使用纵向滚动大屏

Dashboard 不再限制为“第一屏看完所有内容”，允许页面自然向下滚动。

建议首页结构：

#### Section 1：实时总览

6 个核心指标：

- 今日入库；
- 今日出库；
- 当前库存；
- 异常件；
- 滞留件；
- 仓位利用率。

#### Section 2：趋势

- 最近 7/14/30 天入库与出库折线图；
- 库存量趋势折线/面积图。

#### Section 3：结构分布

- 快递公司占比饼图/环图；
- Parcel 状态占比饼图/环图；
- 异常类型分布饼图。

#### Section 4：空间利用率

- 各 Shelf 利用率横向柱状图；
- A/B/C/D 区域利用率；
- 空闲 Slot / 占用 Slot / 禁用 Slot。

#### Section 5：运营风险

- 滞留时长分布；
- 最近异常件；
- 高利用率货架 Top N；
- 最近操作动态。

### 7.3 图表要求

继续使用 ECharts，但 `DataChart` 需要升级为可配置图表，而不是只能展示一种基础图。

建议组件：

- `TrendLineChart.vue`
- `DistributionPieChart.vue`
- `UtilizationBarChart.vue`
- 或一个明确 typed 的 `DataChart` 支持 line/bar/pie/donut。

必须有：

- loading；
- empty；
- resize；
- dispose；
- tooltip；
- legend；
- 主题统一。

禁止 mock 数据冒充统计结果。

---

## 8. 模块 F：后端统计 API

当前首页/分析主要依赖即时 summary，无法支撑真实折线趋势。

新增/完善：

- `GET /api/dashboard/summary`
- `GET /api/dashboard/trends?days=7|14|30`
- `GET /api/dashboard/distributions`
- `GET /api/dashboard/recent-activity`

Service 层新增聚合逻辑，DAO 使用 JDBC PreparedStatement。

建议统计：

- daily inbound count；
- daily outbound count；
- end-of-day/current inventory trend；
- courier distribution；
- parcel status distribution；
- exception type distribution；
- shelf/zone utilization；
- dwell bucket distribution；
- recent operation logs。

不得把统计 SQL 写在 Vue 或 HTTP Handler。

---

## 9. 状态同步原则

所有业务 mutation：

Inbound / Relocate / Outbound / Exception / Layout change

成功后必须刷新对应服务器状态。

V2.1 暂不要求 WebSocket，采用 mutation success 后 store refresh 即可。

至少同步：

- Home/Dashboard；
- Warehouse 2D；
- Digital Twin 3D；
- Parcel detail；
- Exceptions；
- Settings layout。

---

## 10. 测试计划

### 前端

新增至少：

1. warehouse edge auto-scroll speed/direction；
2. dragend/drop cleanup；
3. bottom slot drop target；
4. hover state enter/move/leave；
5. tooltip view model；
6. waiting parcel staging position；
7. inbound success refresh；
8. outbound success removal；
9. outbound error rollback/error state；
10. `/analytics` redirect；
11. dashboard trend/distribution mapping；
12. chart empty/loading states。

### Java

新增至少：

1. inbound HTTP API；
2. outbound HTTP API；
3. auth required；
4. wrong pickup code；
5. transaction rollback；
6. dashboard trend API；
7. distribution API；
8. expanded warehouse migration/seed consistency。

### 质量门

```bash
mvn clean test
mvn -Pintegration-test verify
cd frontend
npm run type-check
npm run test
npm run build
```

真实 MySQL 不可用时，真实 integration 部分保持未验收，但不得阻塞可以自动完成的 H2/前端测试。

---

## 11. 推荐实施阶段

### V2.1 Phase A — Warehouse Drag UX

修复中央独立滚动、edge auto-scroll、下方 Slot drop、拖拽状态释放。

### V2.1 Phase B — Expanded Warehouse Data & 3D Scene

新增增量 migration、8 组货架/约 240 Slot、场景区域/通道、camera framing。

### V2.1 Phase C — 3D Hover + Inbound/Outbound

Hover tooltip、staging zone、inbound API/UI、outbound API/UI、状态同步。

### V2.1 Phase D — Home Analytics Merge

导航改首页，合并 Analytics，新增趋势/饼图/利用率/运营风险区块与统计 API。

### V2.1 Phase E — Full Regression

自动测试、MySQL、浏览器 1366×768/1440×900/1920×1080，全链路回归。

每个 Phase 独立 commit，并在通过自动质量门后自动进入下一 Phase；除非真实权限/凭据硬阻塞，不等待用户确认。

---

## 12. 最终验收场景

1. 登录进入“首页”，可以向下滚动看到完整实时指标、折线图、饼图、利用率和异常信息。
2. 顶部导航不再单独显示“分析”。
3. 仓库作业中央画布独立滚动，拖拽靠近底部会自动下滚，可以将快件拖到最下方空 Slot。
4. 3D 场景至少展示约 8 组真实业务货架，并具有清晰区域和通道。
5. 鼠标悬停任意 Parcel 可看到快件 Tooltip。
6. 点击 Parcel 右侧展示详情并支持镜头定位。
7. Digital Twin 可直接快速入库，新快件出现在待上架区。
8. 待上架 Parcel 可在 3D 中拖拽并 snap 到真实空 Slot。
9. 选中 Parcel 可输入取件码完成出库，成功后从 2D/3D 消失。
10. 入库、换位、出库后首页指标和图表刷新为服务器真实数据。
11. 409/错误取件码/服务端失败均不会产生错误 UI 或数据库状态。
12. 全部 Java/Frontend tests、type-check、build 通过，真实 MySQL 环境可用时 integration tests 也通过。
