# Phase 1 开发日志

## 完成内容

- 建立 Java 17 Maven 项目及 JUnit 5 测试基线。
- 增加 MySQL Connector/J，未引入任何禁止的业务框架或 ORM。
- 实现环境变量优先的数据库配置加载与 JDBC `ConnectionFactory`。
- 新增应用入口、配置及数据库异常类型。
- 新增 MySQL InnoDB schema、初始员工和货架 seed 脚本。

## 新增/修改文件

- `pom.xml`
- `src/main/java/com/parcelstationx/app/ParcelStationApplication.java`
- `src/main/java/com/parcelstationx/config/*`
- `src/main/java/com/parcelstationx/exception/*`
- `src/main/resources/application.example.properties`
- `src/main/resources/db/schema.sql`
- `src/main/resources/db/seed.sql`
- `src/test/java/com/parcelstationx/config/AppConfigTest.java`
- `TASKS.md`
- `development-log/PHASE_1.md`

## 执行命令

```text
mvn clean test
rg -n "spring|springboot|hibernate|mybatis|lombok|javax.persistence|jakarta.persistence" pom.xml src/main
git diff --check
```

## 测试结果

- `mvn clean test`：通过，1 个测试成功。
- 禁止依赖扫描：未发现禁止框架。
- SQL 静态检查：8 张表和 seed 语句均已存在。

## 发现的问题

- 首次 Maven 构建下载 `junit-platform-commons` 时网络超时。

## 修复内容

- 自动重试 Maven 构建，依赖下载完成后测试通过。

## 剩余风险

- 本机没有 `mysql` CLI，尚不能执行真实 MySQL 导入；脚本遵循 MySQL 8/InnoDB 语法，后续保留 JDBC 集成测试的可配置入口。

## 对下一阶段的影响

- Phase 2 可在既有 JDBC 基础上添加模型、泛型 DAO 和基础查询。

## 第二轮审计修正

- Phase 1 状态改为 `IN_PROGRESS`：schema 存在不等于已成功初始化。
- 检查到 Windows `MySQL` 服务已安装但停止，3306 未监听。
- 当前进程无权限启动该服务，且没有数据库凭据。
- 新增 `docs/MYSQL_VERIFICATION_GUIDE.md`，不再以缺少 CLI 作为跳过 JDBC 验证的理由。
