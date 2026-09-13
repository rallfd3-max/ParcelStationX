# TASKS_V2 — ParcelStationX Digital Twin 分阶段任务

> 执行模式：**一次 Codex 运行只允许完成一个 Phase。**
>
> Codex 每次启动时读取本文件，找到编号最小的 `TODO` 或 `IN_PROGRESS` Phase，只执行该 Phase。完成并通过验收后将其标记 `DONE`、写阶段日志、独立 commit/push，然后停止并报告下一 Phase。不得在同一次运行中继续实现下一 Phase。

## V2 Phase 0 — 基线冻结与详细审计

状态：`DONE`

目标：

- 确认分支为 `codex/visualization-v2`；
- 确认基线包含 `c7bf559` 的稳定 Java/Swing/JDBC 功能；
- 运行现有 Java tests；
- 记录现有 model/service/dao/API 可复用点；
- 审计课程技术限制；
- 形成 V2 实施清单与 migration 编号策略；
- 不创建 Vue 项目，不写 HTTP API，不写 Three.js 业务代码。

验收：

- `mvn clean test` 通过；
- `development-log/v2/PHASE_0.md` 完整；
- 原系统没有被修改破坏；
- 对 V2 数据迁移、API 层和前端入口形成明确实施计划。

---

## V2 Phase 1 — Java HTTP / JSON / Session 基础

状态：`DONE`

只实现后端 Web 基础，不实现 ShelfSlot migration 与 Vue。

范围：

- `ParcelStationWebApplication`；
- HttpServer；
- Router/Route；
- RequestContext；
- JSON codec（允许轻量 JSON 依赖，禁止 Web framework）；
- ApiResponse；
- ExceptionMapper；
- SessionManager；
- 登录/登出/me；
- role guard；
- health endpoint；
- 基础 Parcel 查询 API，只复用现有 Service/DAO。

测试：

- 临时随机端口；
- login success/failure；
- 401/403；
- me/logout；
- malformed JSON；
- 404/405；
- 不泄漏 stack/password/session。

验收：

- `mvn clean test`；
- curl/自动测试能完成 login -> me -> parcel query；
- Swing 仍可编译运行；
- 不存在 Spring/SpringBoot 等违规依赖。

---

## V2 Phase 2 — ShelfLayout / Slot / Relocation 数据模型与事务

状态：`DONE`

范围：

- migration SQL；
- ShelfLayout model/DAO；
- ShelfSlot model/DAO；
- ParcelRelocation model/DAO；
- Parcel.slotId + version；
- layout/slot seed；
- WarehouseLayoutService；
- RelocationService；
- relocate optimistic locking；
- warehouse/layout/slot/relocation REST API；
- 入库/出库与 slot 语义兼容。

必须在本 Phase 决定并文档化“待上架”的唯一语义。

测试：

- migration；
- slot 唯一约束；
- relocate success；
- occupied slot；
- stale version；
- invalid state；
- rollback；
- shelf occupied 一致性；
- event/relocation/log 同事务。

验收：

- 仅通过 API 即可查询完整 warehouse snapshot；
- 仅通过 API 即可将 Parcel 从无 Slot/Slot A 移到 Slot B；
- 冲突返回 409；
- `mvn clean test` 通过。

---

## V2 Phase 3 — Vue 基础、登录与 Dashboard

状态：`DONE`

本 Phase 才允许创建 `frontend/`。

范围：

- Vue 3 + TypeScript + Vite；
- Router；
- Pinia；
- API client；
- SessionStore；
- 全局 dark industrial layout；
- login；
- route guard；
- dashboard；
- ECharts；
- 角色菜单；
- loading/error/empty states。

禁止在此 Phase 开始完整 Three.js 或 2D drag。

验收：

- 真实 Java API 登录；
- Dashboard 使用真实 API；
- 不是静态假数据；
- ADMIN/STAFF 菜单差异；
- `npm run type-check`；
- `npm run test`；
- `npm run build`；
- `mvn clean test`。

---

## V2 Phase 4 — 2D Warehouse 拖拽作业中心

状态：`DONE`

范围：

- `/warehouse`；
- WarehouseStore/ParcelStore；
- 待上架列表；
- Shelf/Slot 2D grid；
- zoom/pan 或可用的区域导航；
- drag/drop；
- 可用/不可用颜色与文字反馈；
- relocate API；
- optimistic preview + rollback；
- 409 自动刷新；
- 非拖拽“移动到...”替代操作；
- Parcel 详情侧栏；
- 搜索/过滤。

测试：

- slot mapping；
- drag state；
- success commit；
- conflict rollback；
- occupied target；
- selected parcel；
- API error。

验收：

真实 API 下完成：待上架 -> Slot A -> Slot B，数据库/事件/日志同步，刷新页面后位置不丢失。

---

## V2 Phase 5 — Three.js Digital Twin 基础

状态：`DONE`

范围：

- `/digital-twin`；
- WarehouseScene；
- Renderer；
- CameraController；
- SceneIndex；
- ShelfBuilder；
- SlotBuilder；
- ParcelRenderer；
- OrbitControls；
- Raycaster click；
- primitive fallback environment；
- GLB loader 接口；
- 状态颜色；
- `focusParcel/focusShelf/focusSlot/reset/top/front`；
- 详情侧栏同步。

本 Phase 不做完整 3D drag。

测试：

- slot -> world position pure function；
- shelf transform；
- camera target math；
- scene index；
- selected parcel mapping。

验收：

- API 数据能生成正确 Shelf/Slot/Parcel；
- 点击 Parcel 得到正确业务 ID；
- 搜索 Parcel 后相机平滑移动到对应货架正面；
- 详情正确；
- 出库/重新加载状态正确。

---

## V2 Phase 6 — 3D Drag / Snap / 2D-3D 同步

状态：`DONE`

范围：

- DragController；
- ParcelInteraction；
- pointer drag；
- Slot raycast target；
- green/red drop feedback；
- snap preview；
- relocate API；
- success commit；
- failure animation rollback；
- 2D/3D/Pinia 同步；
- event/relocation history；
- 409 conflict refresh；
- 页面卸载资源释放。

验收：

- 3D 中移动 Parcel 后刷新页面位置仍正确；
- 目标占用不会产生错误 DB 状态；
- stale version 正确回滚；
- 2D 与 3D 同时反映最终结果；
- outbound 后 2D/3D 均移除。

---

## V2 Phase 7 — Blender 接入、Analytics、性能与视觉完善

状态：`TODO`

范围：

- `warehouse.glb` 加载能力；
- 创建 `docs/v2/BLENDER_MODEL_GUIDE.md`；
- 无 GLB fallback；
- `/analytics`；
- Dashboard/图表 polish；
- parcel 50/500/1000 性能 smoke；
- 必要时 InstancedMesh；
- dispose/RAF/listener 审计；
- 1366×768/1440×900/1920×1080 响应式；
- accessibility；
- reduced motion。

验收：

- 环境模型不影响业务货架动态生成；
- 500 Parcel 保持可操作；
- 重复进入/退出 3D 页面无重复 listener/RAF；
- 图表全部真实 API 数据。

---

## V2 Phase 8 — 真实 MySQL + 浏览器全链路最终验收

状态：`TODO`

范围：

- 真实 MySQL migration；
- 完整 integration tests；
- Java regression；
- frontend typecheck/test/build；
- Chrome/Edge 人工 smoke；
- 最终全链路；
- 更新 README；
- 更新 `docs/KNOWN_LIMITATIONS.md`；
- 生成 V2 test report / defense guide / demo script / technology traceability；
- 不再新增大功能。

最终质量门：

```bash
mvn clean test
mvn -Pintegration-test verify
cd frontend && npm run type-check
cd frontend && npm run test
cd frontend && npm run build
```

最终人工链路：登录 → Dashboard → 入库 → 待上架 → 2D 拖拽 → 3D 出现 → 自动定位 → 3D 换位 → relocation/event/log → 出库 → 3D 消失 → 利用率更新 → 异常/统计。

真实 MySQL 或浏览器验证无法执行时，Phase 8 保持 `IN_PROGRESS`，禁止虚假 DONE。
