# Phase AI-2 — Operations Insight & Exception Advice

## 完成内容

- 首页新增 MaiMaiYa AI 运营洞察卡片，包含总体总结、风险和运营建议，并具备 loading、错误与 retry；AI 失败不改变原 ECharts/指标数据状态。
- 服务端 `AiOperationsService` 自行读取 `DashboardAnalyticsService.aiContext()`，上下文包含今日入/出库、库存、异常、滞留、趋势、分布、Shelf/Zone 利用率和裁剪后的活动数据。
- 最近活动只传类型、状态、时间等必要字段；不传客户手机号、pickupCode、passwordHash 或 API Key。
- 异常中心新增 AI 处置建议；建议类型、风险、理由、步骤与处理草稿均经 Java validator 校验。
- “采用建议”仅填充前端处理结果草稿，不调用任何 mutation；最终仍需用户点击“人工确认并完成处理”，走原 `ExceptionService`。
- AI 异常统一降级为明确的 502/503 envelope，不泄露上游 body 或敏感配置。

## 验证

- `mvn clean test`：PASS，47 tests，0 failure/error。
- `npm run type-check`：PASS。
- `npm run test -- --run`：PASS，14 files / 37 tests。
- `npm run build`：PASS（既有 bundle size warning）。
- `mvn spotless:check` 在 apply 后通过等价格式校验；新增测试覆盖真实服务端 context 注入、非法 JSON、非法 exception enum、手机号裁剪和仅填草稿。

## 修复

- AI output 对字符串长度、数组条数、异常 enum 与风险 enum 做确定性校验。
- MaiMaiYa 不可用/disabled 时只影响 AI 卡片，不阻塞首页数据请求、异常列表和原有业务提交。

## 未完成项与下一阶段输入

- 当前 MaiMaiYa 管理页要求登录，未执行真实模型 UI 结果验证；FakeAiClient 测试已覆盖本阶段逻辑。
- AI-3 将复用同一 client/validator，增加严格 Intent/Action 白名单与 Service 查询执行器。
