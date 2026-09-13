# CODEX_V2_1_UPGRADE_PROMPT

你现在负责继续开发 `ParcelStationX Digital Twin v2.1`。

仓库：`https://github.com/rallfd3-max/ParcelStationX`

工作分支：`codex/visualization-v2`

## 0. 启动前必须读取

按顺序读取：

1. `AGENTS.md`
2. `docs/v2/V2_1_INTERACTION_VISUAL_UPGRADE_PLAN.md`
3. `TASKS_V2_1.md`
4. 当前 `TASKS_V2.md`
5. `docs/v2/V2_FINAL_FUNCTIONAL_AUDIT.md`
6. 当前相关实现：`WarehouseView.vue`、`DigitalTwinView.vue`、`DashboardView.vue`、`AnalyticsView.vue`、`frontend/src/styles/main.css`、`frontend/src/stores/warehouse.ts`、`frontend/src/three/*`、Java API/Service/DAO。

不要依赖本提示词替代仓库文档。

## 1. 执行模式

按照 `TASKS_V2_1.md` 的 Phase A → E 自动连续开发。

每个 Phase 必须形成独立闭环：

实现 → 编译/类型检查 → 测试 → 修复 → 再测试 → 更新日志/任务状态 → 独立 commit → push → 自动进入下一 Phase。

不需要等待用户说“继续”。

只有真实硬阻塞（MySQL 凭据、管理员权限、不可控浏览器/外部环境）才允许停。普通 BUG、测试失败、布局错误、Three.js 交互错误都必须自行修复。

不要修改/合并 main，不创建最终 PR。

## 2. 现有稳定资产必须保留

禁止推倒重写。

必须继续复用：

- Java 17 HttpServer
- Service/DAO/JDBC/MySQL
- ParcelService
- RelocationService
- ExceptionService
- WarehouseLayoutService
- Vue 3 / Pinia / Router
- Three.js scene architecture
- 已有认证/权限
- 现有 2D relocate 事务与 409 rollback
- Swing legacy

禁止 Spring/SpringBoot/MyBatis/Hibernate/JPA/ORM/Lombok。

## 3. Phase A：修复二维仓库拖拽到下方空仓位

当前问题是真实浏览器中拖着待上架 Parcel 时，中央货架区域无法顺畅滚到下方，导致底部空 Slot 很难被 drop。

不要只把整个页面高度改大。

要求：

- `shelf-canvas` 成为独立垂直 scroll viewport；
- 高度基于可视区域，1366x768 必须可用；
- 鼠标滚轮可以独立滚动中央仓库；
- HTML5 dragover 时实现 edge auto-scroll；
- pointer/drag 接近中央容器顶部约 60px 自动向上滚；
- 接近底部约 60px 自动向下滚；
- 越靠近边缘速度越快，但有最大速度；
- 离开边缘停止；
- drop/dragend/cancel/unmount 必须 cancel RAF/timer；
- 不允许拖拽状态残留；
- 不能触发重复 relocate；
- 保留右侧“移动到...”替代方式；
- 409 仍 rollback 并 refresh。

建议把滚动计算和控制逻辑拆出 `frontend/src/features/warehouseDragScroll.ts`，避免塞进 Vue template。

同时审查是否需要给已有在库 Parcel 增加 draggable 能力；如果当前产品目标允许 Slot A→Slot B 的 2D 拖拽，则补齐并复用同一 relocate 流程。

自动测试至少覆盖 edge direction/speed/cleanup。

浏览器验收：从待上架列表拖一个 Parcel，通过边缘自动滚动放到最下方空 Slot，刷新后位置仍正确。

## 4. Phase B：扩展 3D 仓库真实货架规模

当前 3D 循环渲染真实 Shelf/Layout/Slot，这是正确架构，不能改成前端假复制。

新增安全的 V2.1 增量 migration，例如：

`src/main/resources/db/migration/V2_1_0__expand_demo_warehouse.sql`

默认目标：

- A-01 / A-02
- B-01 / B-02
- C-01 / C-02
- D-01 / D-02

约 8 组 Shelf，默认每组约 5x6 Slot，总量约 240 Slot。

要求：

- 不 DROP 现有数据；
- 不删除现有 Parcel；
- migration 应先检查/安全插入，避免重复执行破坏结构；
- layout 形成真正的多区域、多通道仓库，而不是一排贴在一起；
- 增加入库待上架区、服务/取件区、地面区域线、区域标签；
- GLB 仍只作为环境资产，业务 Shelf/Slot/Parcel 动态生成；
- `resetCamera/topView/frontView` 根据所有 ShelfLayout 的 bounding box 自适应 framing；
- 不要假设只有三个 shelf；
- 50~200 Parcel 性能仍应流畅。

如果真实 MySQL 当前不可修改，可先完善 migration 与 H2 测试，但 Phase E 仍需真实环境验收。

## 5. Phase C：3D Hover Tooltip

现有 `ParcelInteraction.ts` 只有点击/拾取能力，不满足 hover 信息需求。

必须接入或重构它：

- `pointermove` raycast Parcel；
- hover enter/move/leave；
- cursor pointer；
- hover parcel 轻微高亮；
- 使用 RAF throttle 或等效方案避免高频 raycast；
- dragging 时关闭普通 hover；
- pointerleave/cancel/dispose 清理。

新增：

`ParcelHoverTooltip.vue`

Tooltip 是 HTML overlay，不使用 TextGeometry。

至少显示：

- 运单号
- 快递公司
- 状态
- Zone / Shelf / Slot
- 收件人
- 脱敏手机号
- 到站时间
- 停留时长

Tooltip 跟随鼠标但必须 clamp 在 canvas 范围内。

点击 Parcel 后仍然进入 persistent selection，并保留右侧详情面板和 camera focus。

## 6. Phase C：3D 直接入库

Digital Twin 增加“快速入库”按钮，打开 Modal/Drawer。

至少字段：

- trackingNo
- mobile/customer
- courierCompany
- remark optional

先审计现有 Java Web API。如果没有真正的 inbound API，新增：

`POST /api/parcels/inbound`

但必须复用现有 `ParcelService.inbound`，HTTP handler 不得复制业务规则或直接写 SQL。

成功后：

- WarehouseStore refresh；
- 新 Parcel 为真实 `slotId=null` / `IN_STOCK`；
- 在 3D 的入库/待上架 staging zone 可见；
- 不能把 staging world XYZ 写入 Parcel 数据库；
- waiting parcel 仍使用 parcelId 作为真实业务 ID；
- waiting parcel 可以被 3D drag 到空 Slot，提交现有 relocate transaction。

建议新增：

- `StagingZoneBuilder.ts`
- `WaitingParcelRenderer.ts`

staging 的位置由场景规则计算，不是业务持久化位置。

## 7. Phase C：3D 直接出库

选中在库 Parcel 后，右侧详情加入“出库”。

交互：

- 输入 pickupCode；
- 二次确认；
- 调用后端；
- success 后 refresh。

如果 API 缺失，新增：

`POST /api/parcels/{id}/outbound`

必须复用 `ParcelService.outbound` transaction。

成功后必须：

- Parcel status=PICKED_UP；
- shelf occupied/slot 状态正确；
- ParcelEvent/OperationLog 正确；
- 2D 中移除；
- 3D 中移除；
- selected/hover state 清理；
- 首页 summary/trend 数据刷新。

错误 pickup code、状态冲突、数据库失败都不得只改前端。

## 8. Phase D：首页替代驾驶舱并合并分析

顶部导航把“驾驶舱”改成“首页”。

删除顶部单独“分析”入口。

旧 `/analytics` 不允许 404，redirect 到 `/dashboard#analytics`。

Dashboard 改成自然向下滚动的长页，不要为了追求“一屏”而压缩所有图表。

首页必须包含：

### 第一段：实时总览

- 今日入库
- 今日出库
- 当前库存
- 异常件
- 滞留件
- 仓位利用率

### 第二段：运营趋势

- 7/14/30 天切换
- 入库折线
- 出库折线
- 库存趋势折线或面积图

### 第三段：业务分布

- 快递公司占比饼图/环图
- Parcel 状态占比饼图/环图
- 异常类型占比饼图

### 第四段：空间利用

- Shelf 利用率柱状图
- Zone 利用率
- 空闲/占用/禁用 Slot 结构

### 第五段：运营风险与动态

- 滞留时长分布
- 最近异常件
- 高利用率货架 Top N
- 最近操作记录

继续使用 ECharts。

可以重构 DataChart，或拆分 TrendLineChart/DistributionPieChart/UtilizationBarChart，但必须保证：

- resize
- dispose
- tooltip
- legend
- loading
- empty
- dark industrial theme

不得使用前端 mock 假趋势。

## 9. Phase D：真实统计 API

审计当前 dashboard API；若只有 summary，新增：

- `GET /api/dashboard/trends?days=7|14|30`
- `GET /api/dashboard/distributions`
- `GET /api/dashboard/recent-activity`

统计业务放 Service，SQL 放 DAO/JDBC PreparedStatement。

至少返回：

- daily inbound
- daily outbound
- inventory trend
- courier distribution
- parcel status distribution
- exception type distribution
- shelf/zone utilization
- dwell buckets
- recent operation logs

禁止在 Vue 或 Http Handler 内写 SQL。

## 10. 状态同步

Inbound / Relocate / Outbound / Exception / Layout change 成功后必须保证相关 store 可重新获取服务器真相。

本版本不需要 WebSocket。

mutation 成功后主动 refresh 即可。

同步至少覆盖：

- 首页
- Warehouse 2D
- Digital Twin 3D
- Parcel detail
- Exception
- Settings layout

## 11. 测试要求

每 Phase 都要执行相关自动测试。

Java：

`mvn clean test`

真实 MySQL 环境可用时：

`mvn -Pintegration-test verify`

Frontend：

`npm run type-check`
`npm run test`
`npm run build`

新增测试至少覆盖：

- warehouse edge auto-scroll
- drag cleanup
- bottom slot mapping/drop state
- hover enter/move/leave
- tooltip mapping
- staging position
- inbound API
- outbound API
- wrong pickup code
- transaction rollback
- analytics redirect
- trend/distribution API mapping
- chart loading/empty
- expanded warehouse migration consistency

不得删除失败测试换取绿色结果。

## 12. 使用电脑操纵能力做真实浏览器验收

如果环境提供 Computer Use / 浏览器控制，请实际启动后端和前端并验证。

重点分辨率：

- 1366x768
- 1440x900
- 1920x1080

真实检查：

1. Warehouse 中央区域能滚动；
2. 拖住待上架 Parcel 靠近底部会自动下滚；
3. 能放到最下方空 Slot；
4. 3D 中能看到约 8 组真实货架；
5. hover Parcel 显示 tooltip；
6. quick inbound 后 Parcel 出现在 staging；
7. staging Parcel 可拖到空 Slot；
8. selected Parcel 可用 pickupCode 出库；
9. 2D/3D 同步移除；
10. 首页可以自然下滑看到折线/饼图/利用率/风险；
11. 顶部没有单独“分析”；
12. `/analytics` 自动跳回首页分析区域。

发现浏览器 BUG 自行修复，不要只写文档。

## 13. Git 与日志

在 `codex/visualization-v2` 工作。

每个 V2.1 Phase 独立 commit，例如：

- `v2.1-phase-a: fix scrollable warehouse drag workflow`
- `v2.1-phase-b: expand warehouse digital twin layout`
- `v2.1-phase-c: add 3d hover inbound and outbound`
- `v2.1-phase-d: merge home analytics dashboard`
- `v2.1-phase-e: complete interaction regression`

建议新增：

`development-log/v2_1/PHASE_A.md` ... `PHASE_E.md`

不要 merge main，不创建最终 PR。

## 14. 最终完成条件

只有以下全部满足，V2.1 才能完成：

- 下方 Slot 拖拽可用；
- 中央 Warehouse 可独立滚动；
- drag edge auto-scroll 正常；
- 真实业务货架扩展到目标规模；
- 3D hover tooltip 正常；
- 3D quick inbound 正常；
- staging zone 正常；
- 3D outbound 正常；
- 2D/3D/API/DB 状态一致；
- 首页替代驾驶舱；
- 分析并入首页；
- 至少有真实折线图、饼图/环图和利用率图；
- Java tests 通过；
- frontend type-check/test/build 通过；
- 真实 MySQL 可用时 integration 通过；
- 不破坏原 V2 已完成页面和权限。

现在立即读取计划书和任务表，从 V2.1 Phase A 开始，完成后自动继续 Phase B/C/D/E，不等待用户确认。