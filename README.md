# ParcelStationX Digital Twin v2.1 / V2.2 AI

ParcelStationX 是《软件设计与开发 II》课程项目。V2 在原 Swing/JDBC 系统上增加 Java 17 `HttpServer` REST API，以及 Vue 3、TypeScript、ECharts、Three.js 数字孪生前端。生产代码不使用 Spring、ORM 或 Lombok。

## 环境与数据库

需要 JDK 17、Maven 3.9+、Node.js 20+、npm 和 MySQL 8。全新安装依次执行：

```text
src/main/resources/db/schema.sql
src/main/resources/db/seed.sql
```

从 V1 升级时，先备份数据库，再依次执行原 V1 schema、`src/main/resources/db/migration/V2_0_1__digital_twin_layout.sql`、`seed.sql`、`src/main/resources/db/migration/V2_1_0__expand_demo_warehouse.sql` 和 `src/main/resources/db/migration/V2_2_0__ai_notification_indexes.sql`。最后一个迁移仅添加通知去重/扫描索引，不删除已有数据。

复制 `application.example.properties` 为不提交的 `application.properties`，或设置 `PARCEL_DB_URL`、`PARCEL_DB_USERNAME`、`PARCEL_DB_PASSWORD`。

## 启动

1. 在 IDE 运行 `com.parcelstationx.app.ParcelStationWebApplication`，API 默认位于 `http://localhost:8080`。
2. 在 `frontend` 目录执行 `npm install`、`npm run dev`。
3. 浏览器打开 Vite 输出的地址（通常为 `http://localhost:5173`）。

Windows PowerShell 建议从当前工作树启动，避免 8080 还在运行另一个项目的旧后端：

```powershell
cd C:\Users\19707\Documents\ChatGPT\ParcelStationX-dynamic-shelf-agent
mvn clean package -DskipTests
mvn exec:java "-Dexec.mainClass=com.parcelstationx.app.ParcelStationWebApplication"
```

前端另开一个 PowerShell：

```powershell
cd C:\Users\19707\Documents\ChatGPT\ParcelStationX-dynamic-shelf-agent\frontend
npm install
npm run dev
```

如果前端显示“接口不存在”，检查 8080 指向的 JVM 工作树：

```powershell
netstat -ano | findstr :8080
Get-CimInstance Win32_Process -Filter "ProcessId=<PID>" | Select-Object ProcessId,CommandLine,ExecutablePath
```

`GET /api/health` 会返回 `warehouseAgent` 和 `aiEnabled` 布尔值，且不包含任何数据库或 AI 凭据。`/api/ai/warehouse/plan` 必须使用无尾斜杠的精确路径。

演示账号：`admin`、`staff01`、`staff02`；初始密码：`admin123`。仅限本地课程演示。原 Swing 客户端可运行 `com.parcelstationx.app.ParcelStationApplication`。

## V2.2 AI Provider：MaiMaiYa

V2.2 的 GPT 能力使用 **MaiMaiYa** 作为 OpenAI-Compatible 中转站 Provider。

账号/API 管理入口：

```text
https://maimaiya.click/profile
```

注意：该地址是供应商管理/资料入口，**不能直接假定为实际 API Base URL**。Java 后端真正请求的 Base URL、chat path 和模型必须以 MaiMaiYa 当前账号/API 配置提供的信息为准，并通过环境变量配置。

建议运行时配置：

```text
PARCEL_AI_ENABLED=true
PARCEL_AI_PROVIDER=maimaiya
PARCEL_AI_PORTAL_URL=https://maimaiya.click/profile
PARCEL_AI_BASE_URL=<MaiMaiYa 提供的实际 OpenAI-Compatible API Base URL>
PARCEL_AI_API_KEY=<仅本机环境变量>
PARCEL_AI_MODEL=<MaiMaiYa 当前可用模型>
PARCEL_AI_CHAT_PATH=<MaiMaiYa 实际兼容路径>
PARCEL_AI_TIMEOUT_SECONDS=15
PARCEL_AI_MAX_OUTPUT_TOKENS=1200
```

AI Key 只能存在于 Java 后端环境变量或未提交的本地配置，禁止写入 Vue、Git、日志、浏览器响应和截图。
AI 默认关闭；未配置 MaiMaiYa endpoint、Key 或模型时，全部原有业务仍可正常运行。登录后的 `GET /api/ai/status` 只报告启用状态、Provider、模型和配置完整性，不返回 Key 或 Authorization 信息。

Provider 详细规则见：

```text
docs/v2/V2_2_AI_PROVIDER_MAIMAIYA.md
```

V2.2 项目计划、可行性和 Codex 自动开发规范见：

```text
docs/v2/V2_2_AI_INTELLIGENT_OPERATIONS_PLAN.md
docs/v2/V2_2_AI_FEASIBILITY_ANALYSIS.md
TASKS_V2_2.md
CODEX_V2_2_AI_PROMPT.md
```

## 质量检查

```bash
mvn clean test
mvn -Pintegration-test verify
cd frontend
npm run type-check
npm run test
npm run build
```

MySQL 集成测试仅在三个 `PARCEL_DB_*` 变量齐全时执行；缺失时会跳过，不能视为真实 MySQL 验收通过。最终链路见 `docs/v2/V2_DEMO_SCRIPT.md`，状态见 `docs/v2/V2_TEST_REPORT.md`，课程技术点见 `docs/v2/V2_TECHNOLOGY_TRACEABILITY.md`。

V2.1 首页已合并运营分析，数字孪生支持暂存区快速入库、hover 详情、3D 换位和取件码确认出库；二维仓库支持独立滚动区和拖拽边缘自动滚动。

V2.2 已实现：首页基于 `DashboardAnalyticsService` 真实聚合数据的 AI 运营洞察、只生成草稿的异常处置建议、`/ai` 白名单只读查询与 3D 定位、入库后通知，以及 7/10/15 天持久化去重的滞留提醒。模型不能生成或执行 SQL；所有数据读取仍经 Java Service/DAO/JDBC。AI 不可用时，核心入出库、2D/3D、事务和固定通知模板继续可用。

V2.3 已增加管理员专用的 AI Warehouse Agent：MaiMaiYa 仅返回受限的 `WarehouseAgentAction` JSON；Java 再校验 enum 和参数、生成零写入 preview，并且只有管理员显式确认后才会调用 `ShelfManagementService` 的事务创建货架、布局和仓位。模型没有 SQL、DAO、Shell、文件或 HTTP 执行权；确认后刷新统一的 WarehouseSnapshot，因此二维仓库和 Three.js 数字孪生同步更新。

通知默认使用课程版 `MockSmsGateway`，会完整记录 `PENDING → queue → SUCCESS/FAILED`，并在日志中脱敏手机号；没有供应商合同不会伪造真实 SMS Gateway。可用 `PARCEL_OVERDUE_REMINDER_DAYS=7,10,15`、`PARCEL_NOTIFICATION_SCAN_MINUTES`、`PARCEL_NOTIFICATION_MAX_RETRIES` 与 `PARCEL_SMS_MODE=mock` 配置。通知服务只在入库事务提交后运行；滞留扫描在发送前再次读取快件状态，已出库快件不会再提醒。

核心调用方向：`UI/Web -> Service -> DAO -> JDBC -> MySQL`；AI 外部调用方向：`Vue -> ParcelStationX Java API -> AiClient -> MaiMaiYa OpenAI-Compatible API`。
