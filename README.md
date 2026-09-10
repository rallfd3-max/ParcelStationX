# ParcelStationX — 社区快递驿站综合管理系统

## 快速运行

1. 安装 JDK 17、Maven 3.9+ 与 MySQL 8。
2. 执行 `src/main/resources/db/schema.sql`，再执行 `seed.sql`。
3. 复制 `application.example.properties` 为 `application.properties`，填写本地密码；也可配置 `PARCEL_DB_URL`、`PARCEL_DB_USERNAME`、`PARCEL_DB_PASSWORD`。
4. 运行 `mvn clean test`，再运行 `com.parcelstationx.app.ParcelStationApplication`。

演示账号：`admin`、`staff01`、`staff02`；统一初始密码：`admin123`。

> 《软件设计与开发 II》Java 大作业项目规划仓库  
> 当前仓库阶段：**规划与开发约束已完成，业务代码尚未开始实现**。

## 1. 项目定位

ParcelStationX 是一个面向社区快递驿站的桌面端综合管理系统，用于完成：

- 快件入库
- 货架分配与库存查询
- 取件码生成
- 模拟取件通知
- 快件出库
- 异常件登记与处理
- 客户与员工管理
- 操作日志
- 业务统计
- 数据备份与恢复

项目以课程答辩为目标，强调：

1. 能完整运行；
2. 业务流程清晰；
3. 课程知识点使用自然；
4. 不依赖 SpringBoot、SSH、SSM 等现有业务框架；
5. Git 提交历史能够体现分阶段开发；
6. 有可演示、可解释、可测试的完整功能。

## 2. 技术路线

### 2.1 生产代码

- Java 17
- Java Swing
- JDBC
- MySQL 8+
- Maven（仅作为构建与依赖管理工具）
- Java 标准库

### 2.2 明确禁止

生产代码禁止使用：

- Spring
- SpringBoot
- Struts
- Hibernate
- MyBatis
- SSH
- SSM
- JPA
- Lombok
- 任何 ORM 框架
- 任何能够掩盖 JDBC、线程、序列化、集合或泛型实现细节的业务框架

### 2.3 本项目覆盖的 5 项课程技术

1. **集合**：List、Map、Queue 等
2. **泛型**：BaseDao<T, ID>、Result<T>、PageResult<T> 等
3. **序列化**：系统备份/恢复及本地配置快照
4. **多线程**：异步通知、后台统计、备份任务
5. **数据库编程**：JDBC + MySQL

不主动使用反射和网络编程，确保总量保持在 3–5 项要求范围内。

同时会使用 String、Date/时间类、Math 等常用类。

## 3. 仓库文档阅读顺序

Codex 或开发者应按以下顺序读取：

1. `AGENTS.md`
2. `docs/01_PROJECT_PLAN.md`
3. `docs/02_FEASIBILITY_ANALYSIS.md`
4. `docs/03_REQUIREMENTS_TRACEABILITY.md`
5. `docs/04_ARCHITECTURE_DESIGN.md`
6. `docs/05_DATABASE_DESIGN.md`
7. `docs/06_TEST_STRATEGY.md`
8. `TASKS.md`
9. `CODEX_MASTER_PROMPT.md`

## 4. 开发原则

本项目**不能一次性写完**。

必须按照 `TASKS.md` 中 Phase 0 → Phase 9 的顺序推进。每个阶段都必须：

1. 只完成本阶段目标；
2. 编译；
3. 执行本阶段测试；
4. 修复失败；
5. 更新文档和任务状态；
6. 形成独立 Git commit；
7. 再进入下一阶段。

Codex 在没有真正阻塞时，不需要等待人工回复“继续”。

## 5. 目标质量

最终质量门槛：

- `mvn clean test` 通过；
- 数据库脚本可重复初始化；
- 主流程可从登录一路演示到快件出库；
- 异常输入不会导致程序崩溃；
- Swing UI 不出现明显卡死；
- 数据库连接、Statement、ResultSet 均正确关闭；
- 不出现明文数据库密码提交到仓库；
- 至少准备 20 名客户、50 个快件、10 个异常件的演示数据；
- 提供答辩演示流程与技术点说明。
