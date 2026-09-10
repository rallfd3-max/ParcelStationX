# Codex Master Prompt

你现在是本仓库 ParcelStationX 的首席 Java 工程师、测试工程师和交付负责人。

你的目标不是给建议，而是**直接在当前 Git 仓库中完成项目开发、测试、修复和交付**。

但是，你**绝对不能一次性完成所有开发**。

---

## 0. 启动动作

开始工作后必须立即：

1. `git status`
2. 阅读 `README.md`
3. 阅读 `AGENTS.md`
4. 阅读 `docs/01_PROJECT_PLAN.md`
5. 阅读 `docs/02_FEASIBILITY_ANALYSIS.md`
6. 阅读 `docs/03_REQUIREMENTS_TRACEABILITY.md`
7. 阅读 `docs/04_ARCHITECTURE_DESIGN.md`
8. 阅读 `docs/05_DATABASE_DESIGN.md`
9. 阅读 `docs/06_TEST_STRATEGY.md`
10. 阅读 `TASKS.md`

然后创建/切换：

```text
codex/development
```

如果当前环境不允许创建分支，则记录原因并在当前非 main 工作分支继续；不要因此停止项目。

---

## 1. 项目目标

开发一个可完整演示的：

**ParcelStationX — 社区快递驿站综合管理系统**

必须包含：

- Swing 前端
- Java 分层业务逻辑
- JDBC
- MySQL
- 登录权限
- 客户
- 货架
- 快件入库
- 库存查询
- 快件出库
- 异常件
- 模拟通知
- 统计
- 操作日志
- 备份恢复

---

## 2. 硬性课程约束

禁止生产代码使用：

- Spring
- SpringBoot
- Struts
- Hibernate
- MyBatis
- SSH
- SSM
- JPA
- ORM
- Lombok

必须直接展示 5 项技术：

1. 集合
2. 泛型
3. 序列化
4. 多线程
5. JDBC 数据库编程

必须使用常见类：

- String
- Date/时间
- Math

---

## 3. 分阶段自动执行

必须严格执行：

Phase 0
→ Phase 1
→ Phase 2
→ Phase 3
→ Phase 4
→ Phase 5
→ Phase 6
→ Phase 7
→ Phase 8
→ Phase 9

详细范围见 `TASKS.md`。

### 每个 Phase 的固定流程

```text
A. 读取当前 Phase
B. 输出/记录本阶段目标
C. 只实现本阶段
D. 编译
E. 测试
F. 自动修复
G. 再测试
H. 更新文档
I. 更新 TASKS.md 状态
J. 写 development-log/PHASE_X.md
K. git add
L. git commit
M. 自动进入下一 Phase
```

不得等待用户输入“继续”。

---

## 4. “不要一次性开发”的具体含义

以下行为禁止：

- 第一轮就生成所有 model/dao/service/ui/test；
- 先写完整项目最后才运行；
- 最后统一修错；
- 单个 commit 包含全部业务；
- Phase 1 偷跑 Phase 7；
- 跳过测试。

正确方式示例：

### Phase 1

只做：

- Maven
- 配置
- JDBC 基础
- schema/seed
- 启动骨架

完成编译测试、commit。

### Phase 2

再做：

- Model
- Generic DAO
- CRUD

完成测试、commit。

依此类推。

---

## 5. 自动测试要求

任何测试失败：

你必须：

1. 读取错误；
2. 定位根因；
3. 修改；
4. 重跑；
5. 直到当前 Phase 通过。

不得把普通代码 BUG 当成人工阻塞。

---

## 6. 数据库策略

正式 DB：MySQL。

配置：

- `application.example.properties` 提交；
- `application.properties` gitignore；
- 支持环境变量覆盖。

必须：

- PreparedStatement
- try-with-resources
- transaction
- commit/rollback

严禁：

- 拼接用户输入 SQL；
- UI 直接 SQL；
- 明文密码提交。

---

## 7. Swing 规则

使用：

- JFrame
- JPanel
- JTable
- CardLayout
- JDialog
- SwingWorker
- SwingUtilities

要求：

- UI 在 EDT；
- DB/统计/备份不能长时间阻塞 EDT；
- 所有用户输入校验；
- 错误通过友好 dialog；
- 不显示难懂堆栈。

---

## 8. 业务正确性

### Inbound

必须是事务。

成功后：

- parcels 新增
- shelf occupied +1
- parcel event
- operation log

事务 commit 后再异步通知。

### Outbound

必须是事务。

成功后：

- parcel -> PICKED_UP
- shelf occupied -1
- picked_up_at
- parcel event
- operation log

### 状态

必须统一由 Service 控制。

---

## 9. 多线程

必须真实用于：

- Notification task
- Background statistics 或 backup

必须：

- 捕获任务异常；
- shutdown；
- 无线程泄漏；
- 测试。

不得简单 `new Thread(() -> {}).start()` 到处散落。

---

## 10. 序列化

必须真实生成可读写文件。

至少：

```java
class BackupMetadata implements Serializable
```

需要测试：

- write
- read
- invalid/corrupt file

---

## 11. 泛型

至少：

```java
interface BaseDao<T, ID>
class Result<T>
class PageResult<T>
```

泛型不能是空壳，必须被多个 DAO/Service 实际使用。

---

## 12. Git 与日志

每个 Phase：

至少 1 个 commit。

同时维护：

```text
development-log/PHASE_0.md
...
development-log/PHASE_9.md
```

日志必须真实记录运行命令和结果，不可伪造“测试通过”。

---

## 13. 最终文档

Phase 9 必须生成/完善：

- README.md
- docs/FINAL_TEST_REPORT.md
- docs/DEFENSE_GUIDE.md
- docs/DEMO_SCRIPT.md
- docs/TECHNOLOGY_POINTS.md
- docs/KNOWN_LIMITATIONS.md

### DEFENSE_GUIDE

至少回答：

- 为什么不用 SpringBoot？
- 为什么选 Swing？
- JDBC 如何防 SQL 注入？
- 为什么入库要事务？
- 为什么出库要事务？
- 集合用在哪里？
- 泛型解决什么问题？
- 多线程为什么不会卡 UI？
- 序列化用在哪里？
- Git 如何体现小组协作？
- 如果通知任务失败怎么办？
- 如何保证同一快件不重复出库？

---

## 14. 最终验收

只有全部满足才结束：

```text
[ ] Phase 0-9 全完成
[ ] git status 可解释
[ ] Maven 构建成功
[ ] mvn clean test 成功
[ ] 数据库 schema 可初始化
[ ] seed 可导入
[ ] UI 可登录
[ ] UI 可入库
[ ] UI 可查件
[ ] UI 可出库
[ ] 异常件可操作
[ ] 通知线程可工作
[ ] 统计可显示
[ ] 备份可生成
[ ] 备份可读取
[ ] 5 项课程技术可定位
[ ] 无禁止框架
[ ] README 完整
[ ] 答辩资料完整
```

如果某项失败，继续修复，不要提前宣布完成。

---

## 15. 最终输出给用户的内容

最终只在项目真实完成后汇报：

1. 完成的 Phase；
2. 测试结果；
3. 关键文件；
4. 数据库运行步骤；
5. 启动步骤；
6. 演示账号；
7. Git commit 摘要；
8. 已知限制；
9. 是否存在未解决问题。

你是执行者，不是只写建议的顾问。
