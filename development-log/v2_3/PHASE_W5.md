# Phase W5 — Sync V2.2, MaiMaiYa Integration & Full Regression

## 完成内容

- 在 W4 commit `7b0049b` 后执行 `git fetch origin`，审计并以 merge commit `1fef899` 安全合入 `origin/codex/visualization-v2 @ 269a504`；没有使用 reset，也没有丢弃 V2.3 提交。
- 保留 V2.2 的正式 `AiClient`、`OpenAiCompatibleAiClient`、`AiResponseValidator`、全局助手、通知生命周期和路由；唯一启动装配冲突已合并为同一个 API server。
- 新增运行时 `AiWarehouseAgentParser`，注入现有 V2.2 `AiClient`，限定 JSON schema 和 warehouse enum；危险请求在调用模型前拒绝。模型输出随后仍由 Java 参数检查、`ShelfManagementService.preview`、ADMIN confirm 和事务边界控制。
- 保留 `FakeWarehouseAgentParser` 与 FakeAiClient 仅用于离线测试；生产 `ParcelStationWebApplication` 不再装配 Fake parser。
- README 增加 V2.3 安全执行链路说明。

## 真实本机验收（2026-09-14）

- 真实 MySQL 3306、真实 `ParcelStationWebApplication`、Vite 与 MaiMaiYa 配置启动成功；`/api/ai/status` 显示 provider `maimaiya` 已启用且已配置。
- 管理员输入“帮我在 E 区增加 4 个货架，每个 5 层 6 列”：MaiMaiYa 生成 `CREATE_SHELVES` plan，Preview 展示 E-01~E-04、4 Shelf、120 ShelfSlot 及自动坐标。Preview 期间数据库仍为 8 Shelf / 240 Slot。
- 管理员确认后 Java 重新校验并在事务中写入 Shelf/ShelfLayout/ShelfSlot/OperationLog；页面刷新为 12 Shelf / 360 Slot，2D 和 3D 同时显示 E 区。浏览器刷新及后端重启后数据仍存在。
- 重放同一 confirmation 返回 400；过期 plan 返回 400；两者的前后 snapshot 计数不变。危险指令、越权 action、count/levels/columns 越界均被拒绝且无 mutation。
- 真实业务验证包含单架、批量、自动编号、移动、扩容、缩容和停用。占用货架缩容/停用均返回 400，冲突布局返回 400，未产生部分写入。
- Edge 153 headless/CDP 补充回归：真实管理员登录，Settings 显示 E-01~E-04，Warehouse 2D 显示 E 区，Digital Twin 创建唯一 708x538 WebGL canvas，强制刷新后仍为唯一 canvas。可视化 Chromium 流程同时完成 Preview/Confirm/2D/3D 点击验证。
- `mvn -q clean test`：通过。`MySqlConnectionIT` 和 `MySqlWorkflowIT`：各 1 test，0 failure，0 skipped。
- `npm run type-check`：通过。`npm run test -- --run`：15 files / 40 tests 通过。`npm run build`：通过，仅有既有大 chunk 警告。

## 修复

- 动态货架会使货架总数大于初始 8 个；修正 `MySqlConnectionIT` 的脆弱精确计数断言，改为验证 8 个基线货架始终存在，同时允许真实动态数据。

## 已知限制与下一步

- W5 完成时验收库保留 E/F/G 区动态货架作为真实持久化证据；不是 frontend mock。
- V2.2 长结构化运营洞察 >30s 是既知限制，本 W5 未通过删除功能或伪造 AI 结果规避；在 V2.1.2 3D 交互修复后统一优化。
