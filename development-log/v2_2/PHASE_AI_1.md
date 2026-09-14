# Phase AI-1 — AI Infrastructure

## 完成内容

- 增加服务端 `AiClientConfig`，统一读取 MaiMaiYa Provider、portal、真实 API base URL、Key、模型、chat path、timeout 和 max tokens；默认关闭。
- 明确阻止把 MaiMaiYa profile portal 当成猜测出的 API endpoint，并处理 base URL 已含 `/v1` 时的路径去重。
- 增加可注入 `AiClient`、Java 17 `HttpClient` 的 OpenAI-Compatible 实现、稳定错误码、Prompt 基础规则和结构化 JSON validator。
- 增加登录保护的 `GET /api/ai/status`；响应不含 API Key、Authorization 或账号资料。
- Web 启动入口加载 AI 配置。AI 未启用时不访问外部网络，也不影响原有功能。
- MaiMaiYa profile 在当前 Computer Use 会话中重定向至登录页，因此未读取账号资料、未猜测 API endpoint；真实 smoke 留待 AI-5。

## 主要文件

- `src/main/java/com/parcelstationx/ai/*`
- `src/main/java/com/parcelstationx/config/AppConfig.java`
- `src/main/java/com/parcelstationx/api/http/ApiServer.java`
- `src/main/java/com/parcelstationx/app/ParcelStationWebApplication.java`
- `src/main/resources/application.example.properties`
- `src/test/java/com/parcelstationx/ai/*`

## 验证

- `mvn clean test`：PASS，44 tests，0 failure/error。
- `npm run type-check`：PASS。
- `npm run test -- --run`：PASS，13 files / 36 tests。
- `npm run build`：PASS（仅保留既有 bundle size warning）。
- 本地 stub 覆盖 success、401、403、429、500、invalid JSON、empty content、timeout、disabled 和 secret-not-in-error。

## 修复与审计

- 使用明确 JSON tree 解析，不使用字符串切割响应。
- 上游 body 和 Key 不进入异常消息；status DTO 不含 secret 字段。
- 未新增 Spring、ORM 或前端 Key 配置。

## 未完成项与下一阶段输入

- 真实 MaiMaiYa endpoint/model/Key 需要本机已授权会话或环境变量；此项不阻塞 Fake/stub 开发，在 AI-5 如实 smoke。
- AI-2 复用本阶段 client、validator 和 status/config，增加真实 Dashboard context 与异常建议，保持人工作业确认边界。
