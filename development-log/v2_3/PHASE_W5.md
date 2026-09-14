# Phase W5 — Sync V2.2, MaiMaiYa Integration & Full Regression

## 完成内容

- 在 W4 commit `7b0049b` 后执行 `git fetch origin`，审计并以 merge commit `1fef899` 安全合入 `origin/codex/visualization-v2 @ 269a504`；没有使用 reset，也没有丢弃 V2.3 提交。
- 保留 V2.2 的正式 `AiClient`、`OpenAiCompatibleAiClient`、`AiResponseValidator`、全局助手、通知生命周期和路由；唯一启动装配冲突已合并为同一个 API server。
- 新增运行时 `AiWarehouseAgentParser`，注入现有 V2.2 `AiClient`，限定 JSON schema 和 warehouse enum；危险请求在调用模型前拒绝。模型输出随后仍由 Java 参数检查、`ShelfManagementService.preview`、ADMIN confirm 和事务边界控制。
- 保留 `FakeWarehouseAgentParser` 与 FakeAiClient 仅用于离线测试；生产 `ParcelStationWebApplication` 不再装配 Fake parser。
- README 增加 V2.3 安全执行链路说明。

## 验证

- `mvn -q test`：通过，含 V2.2 AI 安全回归、W4 API 两阶段协议与新增 shared-AiClient warehouse parser 测试。
- `npm run type-check`：通过。
- `npm run test -- --run`：15 files / 40 tests 通过。
- `npm run build`：通过；既有大 chunk 警告未作为失败处理。
- 浏览器已确认本地 Vite 登录页可加载。真实 MySQL / 管理员 Agent mutation 和真实 MaiMaiYa warehouse structured response 尚需在保留的本机运行配置中复验；绝不以 Fake 结果冒充真实 relay。

## 阻塞与下一步

- V2.2 已有短 MaiMaiYa smoke 成功，但长结构化运营洞察超过 30 秒，按 V2.2 AI-5 记录保持 `IN_PROGRESS`；本阶段没有重构或尝试规避该 timeout。
- 需要在可用的真实 MySQL 与 MaiMaiYa 配置下复验：`帮我在 E 区增加 4 个货架` → plan → preview → 管理员确认 → transaction → WarehouseSnapshot/2D/3D 更新；同时验证危险指令与 occupied shelf 业务拒绝。
