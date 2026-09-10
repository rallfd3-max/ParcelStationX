# AGENTS.md — Codex 自动开发总规则

你正在开发《软件设计与开发 II》课程大作业 **ParcelStationX**。

## 一、最高优先级约束

### 1. 禁止一次性开发完整项目

必须严格按 `TASKS.md` 的 Phase 0 → Phase 9 顺序执行。

每一阶段必须形成以下闭环：

- 读取阶段要求
- 实现本阶段
- 编译
- 测试
- 修复
- 更新 TASKS.md
- 更新必要文档
- Git commit
- 自动进入下一阶段

不得在 Phase 2 时提前批量实现 Phase 6、Phase 7 的完整功能。

### 2. 不需要人工反复说“继续”

只要没有以下阻塞，就自动进入下一 Phase：

- 关键需求矛盾且无法依据仓库文档判断；
- 缺失数据库环境并且无法使用项目提供的配置继续完成不依赖数据库的工作；
- 文件权限/系统权限阻止必要操作；
- 编译器、JDK、Maven 等基础工具完全不存在。

普通 BUG、测试失败、代码设计问题不属于阻塞，必须自行修复。

### 3. 不得使用业务框架

生产代码禁止：

- Spring / SpringBoot
- Struts
- Hibernate
- MyBatis
- SSH / SSM
- JPA
- Lombok
- ORM
- 任何替代 JDBC 的持久化框架

允许：

- Java 17 标准库
- Swing
- JDBC
- MySQL Connector/J
- Maven
- JUnit 5（仅测试）

## 二、课程技术点必须真实可答辩

最终必须明确实现并能定位代码：

1. 集合
2. 泛型
3. 序列化
4. 多线程
5. 数据库编程

禁止为了“凑技术点”写无意义代码。

每个技术点在 `docs/03_REQUIREMENTS_TRACEABILITY.md` 中必须登记：

- 使用位置
- 类名
- 方法
- 业务价值
- 答辩解释

## 三、架构规则

推荐包结构：

```text
com.parcelstationx
├── app
├── config
├── model
├── dao
│   └── impl
├── service
│   └── impl
├── ui
│   ├── frame
│   ├── panel
│   ├── dialog
│   └── component
├── task
├── backup
├── util
└── exception
```

必须保持：

`UI -> Service -> DAO -> JDBC -> MySQL`

UI 层不得直接写 SQL。

DAO 不得包含 Swing UI 代码。

Service 负责状态流转、校验、事务边界和业务规则。

## 四、Swing 规则

- Swing 组件创建在 EDT。
- 耗时数据库查询、备份、统计任务不能阻塞 EDT。
- 使用 SwingWorker 或 ExecutorService。
- 表格使用 TableModel。
- 所有输入必须校验。
- 用户错误使用友好对话框提示，不输出堆栈给用户。
- 日志可记录异常堆栈。

## 五、数据库规则

- 只使用 PreparedStatement。
- 使用 try-with-resources。
- 关键出入库流程需要事务。
- 数据库连接配置不得硬编码密码。
- 默认读取：
  - 环境变量；
  - 或未提交的本地 `application.properties`。
- 仓库只提交 `application.example.properties`。

## 六、Git 规则

建议分支：`codex/development`

阶段提交格式：

```text
phase-1: initialize project skeleton and database
phase-2: implement domain models and generic dao
phase-3: implement core parcel services
...
```

一次 Phase 至少一个 commit。

不得把整个项目压缩为一个最终 commit。

## 七、每阶段结束报告

在 `development-log/PHASE_X.md` 中记录：

- 完成内容
- 新增/修改文件
- 执行命令
- 测试结果
- 发现的问题
- 修复内容
- 剩余风险
- 对下一阶段的影响

## 八、最终完成条件

只有以下全部满足才可宣告完成：

- 所有 Phase 完成；
- `mvn clean test` 成功；
- SQL 初始化成功；
- 核心功能可演示；
- 无 SpringBoot/ORM 等违规依赖；
- 课程 5 个技术点可定位；
- README 已有运行步骤；
- 演示账号、演示数据已准备；
- 答辩文档已生成；
- 最终回归测试已记录。
