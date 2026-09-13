# V2 Phase 0 — 基线冻结与详细审计

## 目标与结论

Phase 0 已完成。当前分支为 `codex/visualization-v2`，提交 `c7bf559` 是当前 HEAD 的祖先，稳定 Java 17 / Swing / JDBC 基线完整保留。未创建 Vue、HTTP API、migration 或 Three.js 业务代码。

## 环境与基线

- OS：Windows 11 amd64。
- Java：OpenJDK 17.0.18。
- Maven：3.9.14。
- Node.js：22.17.0；npm：10.9.2，可支持 Phase 3 以后前端工作。
- MySQL CLI：当前 PATH 中不可用；不阻塞 Phase 0–7，Phase 8 的真实 MySQL 验收仍须按真实环境执行。
- 基线关系：`git merge-base --is-ancestor c7bf559 HEAD` 返回 0。
- 当前生产代码 84 个 Java 源文件；测试代码 12 个 Java 源文件。

## 可复用资产审计

### Model / DAO

- 现有 User、Customer、Shelf、Parcel、ParcelEvent、ExceptionRecord、NotificationRecord、OperationLog 及状态枚举可直接保留。
- `BaseDao<T, ID>`、`AbstractJdbcDao<T>`、RowMapper、SqlBinder 已提供泛型 JDBC CRUD 基础。
- ParcelDao 已有 trackingNo / pickupCode 查询；ParcelEventDao 可按 parcelId 查询；其余主要实体 DAO 已覆盖。
- 所有 JDBC 实现沿用 PreparedStatement、try-with-resources 和 ConnectionProvider，不在 Web handler 内写 SQL。

### Service / transaction

- AuthenticationService 可作为 login API 的认证入口。
- ParcelService 已实现 inbound/outbound、状态校验、事件和操作日志事务，可在 Phase 1 暴露只读查询、Phase 2 扩展 slot 语义。
- TransactionRunner 是 relocate 原子事务的复用边界。
- CustomerService、ShelfService、ExceptionService、NotificationService、StatisticsService、BackupService 均保留并供后续 API 适配。
- NotificationQueue 使用受控 ExecutorService；Swing 仍作为 legacy fallback。

### 配置与启动

- AppConfig 从 `PARCEL_DB_*` 或未提交的 `application.properties` 加载数据库配置；密码未硬编码进版本库。
- Phase 0 修复了 AppConfigTest 对本机私有 `application.properties` 的依赖：新增包级纯配置入口，使“缺少配置”测试使用空 Properties/Map，测试结果不再受开发机环境影响。
- Phase 1 新增独立 `ParcelStationWebApplication`，不修改或删除现有 `ParcelStationApplication`。

## 技术限制审计

`pom.xml` 仅包含 MySQL Connector/J、JUnit 5、测试作用域 H2 和构建插件。未发现 Spring、SpringBoot、Struts、MyBatis、Hibernate、JPA、ORM、SSM、SSH 或 Lombok 生产依赖。后续保持 `Vue -> HttpServer handler -> Service -> DAO -> JDBC -> MySQL`。

## V2 实施清单

1. Phase 1：新增轻量 HttpServer、Router、JSON、Session/Auth、统一错误响应和基础只读 Parcel API。
2. Phase 2：先执行数据 migration，再增加 ShelfLayout/ShelfSlot/ParcelRelocation DAO 与 relocate transaction；唯一待上架语义在该阶段定稿。
3. Phase 3：创建 frontend，接真实登录与 Dashboard API。
4. Phase 4：实现 2D warehouse preview/API/commit-or-rollback 流程。
5. Phase 5：按独立 Three.js 模块实现业务坐标映射、选择和镜头定位。
6. Phase 6：增加 3D drag/snap，通过 store 调 relocate API，并完成资源释放。
7. Phase 7：GLB fallback、Analytics、性能、响应式和可访问性完善。
8. Phase 8：真实 MySQL、浏览器全链路和最终文档验收。

## Migration 编号策略

- 现有 `db/schema.sql` 和 `db/seed.sql` 继续作为全新安装基线，不在 Phase 0 修改。
- V2 增量脚本从 `src/main/resources/db/migration/V2_0_1__digital_twin_layout.sql` 开始，采用 `V<major>_<minor>_<sequence>__<description>.sql`，按文件名字典序执行。
- Phase 2 同时维护全新安装 schema/seed 与升级 migration，并分别测试；migration 只做向前兼容的 ADD/CREATE，不删除旧表或旧列。
- 后续结构变化递增 sequence，已发布 migration 不原地改写。

## 执行命令与结果

- `git fetch origin --prune`：成功。
- `git switch --track origin/codex/visualization-v2`：成功。
- `git merge-base --is-ancestor c7bf559 HEAD`：成功。
- Java/Maven/Node/npm 版本检查：成功；MySQL CLI 不可用。
- 禁用依赖全文审计：未发现违规依赖。
- 首次 `mvn clean test`：18 tests，1 failure；原因为本机存在被 Git 忽略的数据库配置，测试错误地假定配置不存在。
- 修复后 `mvn clean test`：18 tests，0 failures，0 errors，BUILD SUCCESS。

## 修改文件

- `src/main/java/com/parcelstationx/config/AppConfig.java`
- `src/test/java/com/parcelstationx/config/AppConfigTest.java`
- `TASKS_V2.md`
- `development-log/v2/PHASE_0.md`

## 发现的问题与修复

- 问题：AppConfigTest 读取真实 classpath 配置，安装过本地配置的机器会失败。
- 修复：把配置解析拆为可注入 Properties/环境 Map 的确定性入口；生产入口行为不变。

## 剩余风险与下一阶段输入

- MySQL CLI 未在 PATH，真实 MySQL 验收留到 Phase 8；H2 MySQL mode 可覆盖前期 DAO/事务测试。
- Phase 1 应避免直接序列化含 passwordHash 的 User，Session token 仅通过认证响应/请求头传输且不得写日志。
- Phase 1 只建设 Web 基础和只读 Parcel 查询，不提前创建 slot migration 或 frontend。
