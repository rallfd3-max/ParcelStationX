# Phase W4 — Warehouse Agent Contract & Two-Phase Execution

# Phase W4 — Warehouse Agent Contract & Two-Phase Execution

## 完成内容

- 新增严格的 `WarehouseAgentIntent` 枚举、typed action/parameters、计划模型与五分钟内存计划存储。
- 仅 `CREATE_SHELF` / `CREATE_SHELVES` 可以通过临时 `FakeWarehouseAgentParser`；危险、越权和注入式语言统一归为 `UNSUPPORTED`，不会产生 DAO、SQL、文件、Shell 或 HTTP 路径。
- 新增 ADMIN-only `plan`、`confirm`、`cancel` API。plan 只调用 `ShelfManagementService.preview`；confirm 先保留计划，调用 Java Service 事务，并仅在成功后标记 consumed；失败会恢复 pending，避免伪成功或重放。
- 新增 Settings 的 AI Warehouse Agent 输入、归一化 intent、货架/仓位 ghost preview、确认、修改指令和取消按钮；确认后复用 WarehouseStore 刷新 2D/3D 事实源。
- 没有新增、复制或重构 V2.2 `AiClient` / MaiMaiYa client；Fake parser 仅为 W4 合约与确定性测试，W5 才替换为 V2.2 正式基础设施。

## 验证

- `mvn -q test`：通过（含 API 两阶段确认、STAFF 403、重复确认、非法 UUID、注入拒绝，以及 parser/store 单测）。
- `npm run type-check`：通过。
- `npm run test -- --run`：14 files / 38 tests 通过。
- `npm run build`：通过；现有 Dashboard / DigitalTwin 产物大于 500 kB 的 Vite 提示仍存在，非本阶段失败。
- 浏览器：`http://127.0.0.1:15173/login?redirect=/settings` 可加载到登录页。没有提交数据库账号，故完整管理员登录、真实 MySQL 交互留待 W5 的集成环境验收；H2 API 测试已覆盖计划到确认的服务链路。

## 下一阶段输入

- W5 先确认 W4 已提交且工作树干净，然后 `git fetch origin`，审计并安全合入 `origin/codex/visualization-v2 @ 269a504`。
- 以合入后的 V2.2 正式 MaiMaiYa/OpenAI-Compatible `AiClient` 取代临时 Fake parser，保留本阶段的白名单、preview、ADMIN confirm 与 Java 事务边界。
