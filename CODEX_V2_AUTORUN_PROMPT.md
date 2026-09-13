# CODEX_V2_AUTORUN_PROMPT.md

你正在继续开发 `ParcelStationX Digital Twin v2.0`。

## 唯一执行目标

从当前仓库状态开始，严格依据 `TASKS_V2.md` 的 Phase 顺序持续自动开发，**不需要等待用户在阶段之间确认，也不需要用户反复输入“继续”**。

## 启动顺序

1. 切换并确认当前分支为 `codex/visualization-v2`。
2. 读取根目录 `AGENTS.md`。
3. 按 `docs/v2/00_READ_FIRST.md` 指定顺序读取所有 V2 文档。
4. 读取 `TASKS_V2.md`、`CODEX_V2_MASTER_PROMPT.md` 和本文件。
5. 找到编号最小的 `TODO` 或 `IN_PROGRESS` V2 Phase，从该 Phase 开始。

## 连续执行规则

必须按照 Phase 0 → Phase 8 的顺序持续推进。

每一个 Phase 都必须单独完成以下闭环：

1. 重新阅读当前 Phase 的范围、禁止项和验收标准；
2. 只实现当前 Phase 所要求的主体功能；
3. 编译 / type-check；
4. 执行当前 Phase 所要求的测试；
5. 如果失败，自己定位并修复；
6. 反复执行直到当前 Phase 的可执行质量门通过；
7. 更新 `development-log/v2/PHASE_X.md`；
8. 根据真实结果更新 `TASKS_V2.md` 状态；
9. 检查 `git diff`、`git status`，确保没有无关改动；
10. 为当前 Phase 创建独立 commit；
11. push 到 `codex/visualization-v2`；
12. 再次读取仓库状态；
13. **自动进入下一个 Phase，不等待人工确认。**

不得把 Phase 0–8 一次性实现成一个大提交。连续开发指的是“自动连续推进阶段”，不是“取消阶段边界”。

## 不得停止询问用户的情况

以下问题必须自行解决，不得因此停止等待用户：

- Java 编译错误；
- Maven 测试失败；
- TypeScript 类型错误；
- npm/Vite/Vitest 构建或测试失败；
- 普通逻辑 BUG；
- UI 布局问题；
- Three.js 交互 BUG；
- API 返回结构不一致；
- 数据库 migration 代码错误；
- 单元测试/集成测试自身缺陷；
- Git 工作区中由本轮开发产生的可解释修改；
- 需要重构当前 Phase 内部实现才能满足验收。

这些都属于工程工作的一部分，必须自行处理。

## 允许停止的真实硬阻塞

只有遇到下列无法由代码解决的外部依赖时才允许停止：

- 必须的数据库用户名/密码等凭据无法获得；
- Windows/macOS/Linux 管理员权限阻止启动必须的服务；
- 仓库权限阻止 commit/push；
- 必须的外部硬件/真实设备不可获得；
- 必须由人工提供的 Blender/GLB 资产完全缺失且 primitive fallback 也无法满足该阶段验收；
- 浏览器人工视觉验收属于最终必须人工确认的项目，并且所有自动化部分已经完成。

如果硬阻塞只影响某个验收项，但不影响后续代码开发：

- 完成该 Phase 所有可完成内容；
- 将状态保持为 `IN_PROGRESS`；
- 在阶段日志写明具体阻塞、已完成部分、待人工步骤；
- commit + push；
- 继续执行后续不依赖该阻塞的 Phase。

禁止伪造 MySQL、浏览器、GLB 或真实环境验证结果。

## 稳定基线保护

V2 基线来自 `c7bf559`。

必须保留现有稳定的：

- Java 17 model / dao / service / transaction；
- JDBC / MySQL；
- 登录、入库、出库、异常、通知、统计、备份；
- Swing legacy；
- 已有自动测试。

禁止为了方便 Web 开发而整体推倒重写。

## 架构和课程约束

必须保持：

`Vue / Three.js / ECharts -> HTTP JSON -> Java Handler -> Service -> DAO -> JDBC -> MySQL`

禁止生产代码使用：

- Spring / SpringBoot
- Struts
- MyBatis
- Hibernate
- JPA
- ORM
- SSM / SSH
- Lombok

Java Web API 使用 `com.sun.net.httpserver.HttpServer` 为主。

## 前端与数字孪生原则

必须实现真实业务闭环，不允许只做视觉 Demo：

- Dashboard 使用真实 API；
- 2D drag 必须通过 relocate API 持久化；
- 3D Parcel 必须由真实 Shelf/Slot/Parcel 数据生成；
- 点击/搜索 Parcel 必须让 Camera 自动定位到对应货架正面；
- 3D drag 必须 snap 到 Slot，并调用 relocate API；
- API 失败/409 必须 rollback UI；
- 2D、3D、详情、统计使用同一服务器真相；
- outbound 后 Parcel 必须从 2D/3D 中消失或转为正确状态；
- 禁止使用静态假数据、截图或视频冒充功能。

## 测试策略

后端基础：

```bash
mvn clean test
```

真实 MySQL 阶段：

```bash
mvn -Pintegration-test verify
```

前端创建后每个相关 Phase：

```bash
cd frontend
npm run type-check
npm run test
npm run build
```

Three.js 纯计算逻辑尽可能拆成可测试函数，例如：

- slot -> world position；
- shelf transform；
- camera target；
- scene index mapping；
- drag state；
- relocate rollback。

## Git 规则

工作分支只能是：

`codex/visualization-v2`

每个 Phase 独立 commit 并 push。

建议提交：

- `v2-phase-0: audit stable baseline`
- `v2-phase-1: add java http api foundation`
- `v2-phase-2: add slots layouts and relocation transaction`
- `v2-phase-3: add vue shell login and dashboard`
- `v2-phase-4: implement 2d warehouse drag workflow`
- `v2-phase-5: implement digital twin scene and camera focus`
- `v2-phase-6: implement 3d drag snap and state sync`
- `v2-phase-7: integrate warehouse glb and polish analytics performance`
- `v2-phase-8: complete mysql and browser acceptance`

禁止自动合并 `main`，禁止自动创建最终 PR，除非用户明确要求。

## 最终终止条件

持续开发直到出现以下任一情况：

### A. 全部完成

所有 V2 Phase 均真实满足验收并标记 DONE；所有自动测试通过；最终文档完成。此时停止并输出完整最终报告。

### B. 真实硬阻塞

所有不依赖人工的工作都已经完成，剩余事项只能由用户提供权限、凭据、真实 MySQL、浏览器人工确认或外部资产。此时停止，并一次性输出：

- 已完成到哪个 Phase；
- 哪些 Phase 为 DONE / IN_PROGRESS；
- 最后一个 commit SHA；
- 所有自动测试结果；
- 硬阻塞原因；
- 用户只需要执行的最少人工步骤；
- 完成人工步骤后重新运行 Codex 时应从哪个 Phase 继续。

除此之外，不要停下来问“是否继续”。