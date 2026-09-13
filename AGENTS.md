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

在 `codex/visualization-v2` 上，旧的 `TASKS.md` 仅作为 legacy V1 历史，不得把它当作当前开发任务。

### V2 最重要规则

- **一次 Codex 运行只完成一个 V2 Phase。**
- 找到 `TASKS_V2.md` 中编号最小的 TODO/IN_PROGRESS Phase，只实现它。
- 当前 Phase 完成：实现 → 编译/类型检查 → 测试 → 修复 → 再测试 → 阶段日志 → 更新状态 → 独立 commit → push → 停止。
- 禁止在同一次运行继续下一 Phase。
- 真实环境不足时可保持 IN_PROGRESS，但禁止虚假 DONE。
- 禁止直接修改/合并 main。

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

每个 Phase 独立 commit。

阶段日志：

```text
development-log/v2/PHASE_0.md
...
development-log/v2/PHASE_8.md
```

日志必须记录：完成内容、文件、命令、测试结果、修复、未完成项、阻塞和下一阶段输入。

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

若缺少真实 MySQL、浏览器或资产环境，必须保持对应 Phase IN_PROGRESS 并说明人工步骤。