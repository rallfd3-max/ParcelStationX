# ParcelStationX Digital Twin v2.0

ParcelStationX 是《软件设计与开发 II》课程项目。V2 在原 Swing/JDBC 系统上增加 Java 17 `HttpServer` REST API，以及 Vue 3、TypeScript、ECharts、Three.js 数字孪生前端。生产代码不使用 Spring、ORM 或 Lombok。

## 环境与数据库

需要 JDK 17、Maven 3.9+、Node.js 20+、npm 和 MySQL 8。全新安装依次执行：

```text
src/main/resources/db/schema.sql
src/main/resources/db/seed.sql
```

从 V1 升级时，先备份数据库，再依次执行原 V1 schema、`src/main/resources/db/migration/V2_0_1__digital_twin_layout.sql` 和 `seed.sql`。

复制 `application.example.properties` 为不提交的 `application.properties`，或设置 `PARCEL_DB_URL`、`PARCEL_DB_USERNAME`、`PARCEL_DB_PASSWORD`。

## 启动

1. 在 IDE 运行 `com.parcelstationx.app.ParcelStationWebApplication`，API 默认位于 `http://localhost:8080`。
2. 在 `frontend` 目录执行 `npm install`、`npm run dev`。
3. 浏览器打开 Vite 输出的地址（通常为 `http://localhost:5173`）。

演示账号：`admin`、`staff01`、`staff02`；初始密码：`admin123`。仅限本地课程演示。原 Swing 客户端可运行 `com.parcelstationx.app.ParcelStationApplication`。

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

核心调用方向：`UI/Web -> Service -> DAO -> JDBC -> MySQL`。
