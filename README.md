# ParcelStationX Digital Twin v2.1 / V2.2 AI

ParcelStationX 是《软件设计与开发 II》课程项目。V2 在原 Swing/JDBC 系统上增加 Java 17 `HttpServer` REST API，以及 Vue 3、TypeScript、ECharts、Three.js 数字孪生前端。生产代码不使用 Spring、ORM 或 Lombok。

## 环境与数据库

需要 JDK 17、Maven 3.9+、Node.js 20+、npm 和 MySQL 8。全新安装依次执行：

```text
src/main/resources/db/schema.sql
src/main/resources/db/seed.sql
```

从 V1 升级时，先备份数据库，再依次执行原 V1 schema、`src/main/resources/db/migration/V2_0_1__digital_twin_layout.sql`、`seed.sql` 和 `src/main/resources/db/migration/V2_1_0__expand_demo_warehouse.sql`。V2.1 迁移可重复执行，不删除已有快件，并扩展为八组货架、240 个真实仓位。

复制 `application.example.properties` 为不提交的 `application.properties`，或设置 `PARCEL_DB_URL`、`PARCEL_DB_USERNAME`、`PARCEL_DB_PASSWORD`。

## 启动

1. 在 IDE 运行 `com.parcelstationx.app.ParcelStationWebApplication`，API 默认位于 `http://localhost:8080`。
2. 在 `frontend` 目录执行 `npm install`、`npm run dev`。
3. 浏览器打开 Vite 输出的地址（通常为 `http://localhost:5173`）。

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

V2.2 计划增加：首页 AI 运营洞察、异常 AI 处置建议、全局自然语言查询、AI 结果联动 3D、入库自动通知与 7/10/15 天滞留提醒。

核心调用方向：`UI/Web -> Service -> DAO -> JDBC -> MySQL`；AI 外部调用方向：`Vue -> ParcelStationX Java API -> AiClient -> MaiMaiYa OpenAI-Compatible API`。
