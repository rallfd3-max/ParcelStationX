# AGENTS.md — ParcelStationX Codex 自动开发总规则

你正在维护《软件设计与开发 II》课程项目 **ParcelStationX**。

## A. 当前分支优先规则

如果当前分支是 `codex/visualization-v2`，则 **V2 规则优先于旧版 Phase 0–9 规则**。

V2 开始工作前必须读取：

1. `docs/v2/00_READ_FIRST.md`
2. `docs/v2/01_PROJECT_PLAN.md`
3. `docs/v2/02_FEASIBILITY_ANALYSIS.md`
4. `docs/v2/03_ARCHITECTURE.md`
5. `docs/v2/04_UI_UX_SPEC.md`
6. `docs/v2/05_DIGITAL_TWIN_3D_SPEC.md`
7. `docs/v2/06_DATABASE_API_SPEC.md`
8. `docs/v2/07_TEST_ACCEPTANCE.md`
9. `TASKS_V2.md`
10. `CODEX_V2_MASTER_PROMPT.md`
11. `CODEX_V2_AUTORUN_PROMPT.md`

在 `codex/visualization-v2` 上，旧的 `TASKS.md` 仅作为 legacy V1 历史，不得把它当作当前开发任务。

### V2 最重要规则：连续自动开发

- **用户已经明确授权：不需要在 Phase 之间等待人工确认，也不需要用户反复输入“继续”。**
- 从 `TASKS_V2.md` 中编号最小的 `TODO` 或 `IN_PROGRESS` Phase 开始，严格按 Phase 0 → Phase 8 顺序推进。
- 每个 Phase 仍然必须形成独立质量闭环：实现 → 编译/类型检查 → 测试 → 修复 → 再测试 → 阶段日志 → 更新状态 → 独立 commit → push。
- 当前 Phase 满足验收并成功 push 后，**自动重新读取仓库状态并立即进入下一个 Phase**。
- 禁止为了“连续执行”而跨阶段混写；Phase N 未达到其代码与自动测试验收前，不得把 Phase N+1 的主体功能提前塞入同一个 commit。
- 普通 BUG、编译错误、测试失败、类型错误、前端构建错误属于 Codex 自行解决的问题，**不是停下来请求用户确认的理由**。
- 只有遇到真实硬阻塞才允许停止，例如：缺少必须由用户提供的凭据、操作系统管理员权限、真实 MySQL 无法启动且后续验收确实依赖它、无法获得必须的外部资产/设备、仓库权限阻止 push。
- 若某 Phase 有外部验收项暂时不可执行，但不阻塞后续纯代码开发：完成该 Phase 所有可完成内容，记录 `IN_PROGRESS` 与阻塞原因，独立 commit/push 后可继续后续不依赖该阻塞的 Phase；不得伪造验证成功。
- 到 Phase 8 时，如果真实 MySQL / 浏览器人工链路仍因权限、凭据等外部条件无法完成，停止并一次性列出人工需要完成的剩余验收步骤。
- 禁止直接修改/合并 `main`。

> 如 `TASKS_V2.md` 或 `CODEX_V2_MASTER_PROMPT.md` 中仍存在旧的“完成一个 Phase 后停止/等待下一次运行”措辞，以本节和 `CODEX_V2_AUTORUN_PROMPT.md` 为准；旧停止规则作废，但各 Phase 的范围、测试和验收标准继续有效。

## B. 后端课程约束

生产 Java 代码禁止：

- Spring / SpringBoot
- Struts
- Hibernate
- MyBatis
- SSH / SSM
- JPA
- Lombok
- ORM
- 任何替代 JDBC 的持久化框架

允许：

- Java 17 标准库
- Swing legacy
- `com.sun.net.httpserver.HttpServer`
- JDBC
- MySQL Connector/J
- Maven
- JUnit 5（测试）
- V2 所需轻量 JSON 库

现有业务必须保持：

`UI/API -> Service -> DAO -> JDBC -> MySQL`

API handler 和 Vue 都不得直接写 SQL。

## C. V2 前端允许技术

- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- Three.js
- Apache ECharts
- Vitest
- 必要的轻量 UI/CSS 工具

最终视觉必须自行设计成工业数字孪生风格，禁止把 UI 库默认后台模板当成最终交付。

## D. 稳定基线保护

V2 基线来自 commit `c7bf559`。

必须保留并复用：

- model / dao / service / transaction；
- 登录、入出库、异常、通知、统计、备份；
- MySQL schema/seed；
- Swing UI；
- 原有测试。

除非当前 Phase 有明确迁移理由，否则禁止大规模重写已经稳定的类。

Swing 是 legacy fallback，不得在 V2 开发中删除。

## E. 数据库规则

- PreparedStatement；
- try-with-resources；
- 关键操作使用 transaction；
- 密码/数据库凭据不得硬编码和提交；
- V2 schema 使用 migration；
- 3D Parcel 位置通过 `slot_id` 映射，禁止把任意 world XYZ 作为 Parcel 真实位置；
- Relocation 必须有并发冲突控制和 rollback。

## F. 前端与 3D 一致性规则

- 2D/3D 拖拽只能作为操作预览；
- 数据库成功后才确认最终状态；
- 409/业务失败必须 rollback；
- 2D、3D、详情和统计必须使用同一 Pinia/服务器真相；
- 不得用静态 mock 作为最终业务数据；
- 不得用截图、视频或 CSS 假装可交互 3D。

## G. Three.js 资源规则

- GLB 只放环境；
- Shelf/Slot/Parcel 程序化生成；
- Three.js 代码拆分到 `frontend/src/three/`；
- 页面卸载 cancel RAF、dispose、remove listeners；
- GLB 加载失败必须有 primitive fallback。

## H. Git 与阶段日志

V2 工作分支：`codex/visualization-v2`。

每个 Phase 独立 commit，不得把多个 Phase 压成一个总 commit。

阶段日志：

```text
development-log/v2/PHASE_0.md
...
development-log/v2/PHASE_8.md
```

日志必须记录：完成内容、文件、命令、测试结果、修复、未完成项、阻塞和下一阶段输入。

每次 Phase push 完成后继续下一 Phase 前，重新读取：

- `git status`
- `TASKS_V2.md`
- 当前阶段日志
- 下一阶段定义

确保工作区干净且阶段边界清晰。

## I. 完成条件

V2 只有在以下全部满足后才可最终完成：

- 所有 V2 Phase 真实通过；
- Java regression 通过；
- 真实 MySQL integration 通过；
- 前端 type-check/test/build 通过；
- 浏览器全链路通过；
- 2D/3D 与数据库一致；
- Camera focus 正确；
- relocate 冲突/rollback 正确；
- Swing legacy 仍可编译；
- README/测试报告/答辩与已知限制更新。

若缺少真实 MySQL、浏览器或资产环境，必须保持对应 Phase `IN_PROGRESS` 并说明人工步骤；除此之外，不得因为“需要用户确认”而停止连续开发。