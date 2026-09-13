# ParcelStationX V2 — READ FIRST

本目录定义 `codex/visualization-v2` 分支的全部升级规则。

## 目标

将现有 ParcelStationX Java/Swing/JDBC 课程系统升级为 **ParcelStationX Digital Twin v2.0 — 智慧快递驿站数字孪生可视化管理平台**。

现有稳定业务代码必须保留。V2 的核心是新增 Web 可视化层、Java HTTP API、二维拖拽仓位、三维数字孪生仓库和数据驾驶舱，而不是推倒重写原系统。

## 强制阅读顺序

Codex 每次开始工作前必须按顺序读取：

1. `/AGENTS.md`
2. `/docs/v2/00_READ_FIRST.md`
3. `/docs/v2/01_PROJECT_PLAN.md`
4. `/docs/v2/02_FEASIBILITY_ANALYSIS.md`
5. `/docs/v2/03_ARCHITECTURE.md`
6. `/docs/v2/04_UI_UX_SPEC.md`
7. `/docs/v2/05_DIGITAL_TWIN_3D_SPEC.md`
8. `/docs/v2/06_DATABASE_API_SPEC.md`
9. `/docs/v2/07_TEST_ACCEPTANCE.md`
10. `/TASKS_V2.md`
11. `/CODEX_V2_MASTER_PROMPT.md`

## 最高优先级规则

- **禁止一次性完成 V2。** 必须按 `TASKS_V2.md` 的 Phase 顺序逐阶段开发。
- 每个 Phase 必须形成：读取要求 → 实现本阶段 → 编译/类型检查 → 测试 → 修复 → 再测试 → 更新阶段日志 → 更新任务状态 → 独立 Git commit → push。
- 某阶段未通过验收，不得提前将后续阶段标记 DONE。
- 普通编译错误、测试失败、类型错误、UI Bug 不属于人工阻塞，Codex 必须自行修复。
- 只有真实环境缺失、权限不足、凭据缺失、需求冲突无法根据仓库判断时，才允许停止并说明阻塞。
- 禁止删除现有 Swing 版本。Swing 作为 legacy fallback 保留，直到 V2 最终验收后仍保留在仓库中。
- 禁止 Spring/SpringBoot/MyBatis/Hibernate/JPA/ORM/SSM/SSH/Lombok。
- Java Web 层优先使用 Java 17 `com.sun.net.httpserver.HttpServer`。
- 前端允许 Vue 3、TypeScript、Vite、Vue Router、Pinia、Three.js、ECharts；可使用轻量 UI 库，但不得把默认后台模板当作最终设计。
- 不得用静态假数据替代 API 作为最终实现。
- 不得用截图、CSS 假 3D 或预渲染视频伪装 Three.js 交互。
- 不得只移动 2D/3D 画面而不更新数据库。

## V2 核心验收链路

登录 → Dashboard → 新增/选择客户 → 快件入库 → 待上架区出现 → 2D 拖拽到仓位 → 数据库事务成功 → 3D 场景同步出现 → 点击快件 → 相机飞到对应货架正面 → 右侧详情展示 → 3D 拖拽到另一仓位 → relocation/event/log 持久化 → 出库 → 3D 快件消失 → 货架利用率与 Dashboard 更新。

## 本规划提交的边界

本次 planning baseline **只允许创建/更新设计文档、任务清单和 Codex 执行规则**。不得在同一 planning commit 中创建 Vue 项目、REST API、数据库 migration 或 Three.js 业务代码。
