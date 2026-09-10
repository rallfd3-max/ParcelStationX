# ParcelStationX 分阶段开发任务

> Codex 必须按顺序自动执行。  
> 每一 Phase 完成测试和提交后再进入下一 Phase。

## Phase 0 — 需求确认与环境检查

状态：`DONE`

目标：

- 阅读全部 docs；
- 检查 Java/Maven/MySQL 能力；
- 检查禁止依赖；
- 形成 `development-log/PHASE_0.md`；
- 不写核心业务代码。

验收：

- 输出项目执行理解；
- 没有需求冲突；
- 开发路线与文档一致。

---

## Phase 1 — 项目骨架与数据库脚本

状态：`IN_PROGRESS`

目标：

- Maven 项目；
- 标准目录；
- `.gitignore`；
- 配置加载；
- JDBC ConnectionFactory；
- `schema.sql`；
- `seed.sql`；
- 应用启动入口；
- 基础异常体系。

验收：

- `mvn clean test` 至少能完成空项目构建；
- schema 可初始化；
- 不实现完整业务。

---

## Phase 2 — 领域模型、泛型 DAO 与基础查询

状态：`DONE`

实现：

- User
- Customer
- Shelf
- Parcel
- ParcelEvent
- ExceptionRecord
- NotificationRecord
- OperationLog
- 必要 enum
- `BaseDao<T, ID>`
- `Result<T>`
- `PageResult<T>`
- 主要 DAO

技术点：

- 泛型
- JDBC
- 集合

验收：

- CRUD DAO 测试；
- PreparedStatement；
- try-with-resources。

---

## Phase 3 — 核心业务服务

状态：`DONE`

实现：

- 登录与权限
- 客户管理
- 货架管理
- 快件入库
- 取件码生成
- 快件查询
- 快件出库
- 状态流转校验
- 操作日志

验收主流程：

`登录 -> 新增客户 -> 快件入库 -> 分配货架 -> 查询 -> 验证取件码 -> 出库`

必须完成事务测试。

---

## Phase 4 — Swing 前端骨架

状态：`DONE`

实现：

- LoginFrame
- MainFrame
- 导航栏
- DashboardPanel
- 通用表格
- 通用搜索栏
- 通用分页
- 状态提示
- 权限菜单控制

要求：

- UI 层不写 SQL；
- EDT 安全；
- 页面可切换；
- 视觉整洁。

---

## Phase 5 — 核心业务页面

状态：`DONE`

实现页面：

- 快件入库
- 在库快件
- 快件出库
- 客户管理
- 货架管理
- 员工管理
- 操作日志

验收：

- 可通过 UI 完整走通 Phase 3 主流程；
- 输入错误有提示；
- 重复运单号不能入库；
- 已出库快件不能重复出库。

---

## Phase 6 — 异常件 + 通知 + 多线程

状态：`DONE`

实现：

- 异常件登记
- 异常处理
- 滞留件识别
- 通知任务队列
- 模拟短信/通知发送
- NotificationRecord
- ExecutorService
- SwingWorker

技术点：

- 集合 Queue
- 多线程

要求：

- 任务失败不能使 UI 崩溃；
- 不允许线程泄漏；
- 应用退出时正常 shutdown。

---

## Phase 7 — 统计 + 序列化备份恢复

状态：`DONE`

实现：

- 今日入库数
- 今日出库数
- 当前库存
- 异常件数
- 平均滞留时长
- 快递公司件量
- 货架占用率
- Java2D 简单柱状统计组件
- 备份元数据对象序列化
- 数据导出/备份
- 恢复前确认
- 备份文件校验

技术点：

- Math
- Date/时间
- 集合
- 序列化
- 多线程

---

## Phase 8 — 自动测试、演示数据与稳定性

状态：`IN_PROGRESS`

补齐：

- DAO 测试
- Service 测试
- 状态机测试
- 重复数据测试
- 事务回滚测试
- 并发通知测试
- 序列化测试
- 边界输入
- SQL 初始化测试

演示数据：

- >= 3 名员工
- >= 20 名客户
- >= 50 个快件
- >= 10 个异常/滞留场景
- >= 30 条日志

---

## Phase 9 — 最终回归与答辩材料

状态：`IN_PROGRESS`

完成：

- README 运行教程
- 数据库初始化教程
- 演示账号
- 演示流程
- 系统截图位置说明
- 课程 5 技术点代码定位
- Git 提交说明
- 常见答辩问题
- 已知限制
- 最终测试报告

最终质量门：

```bash
mvn clean test
```

必须通过后才能标记项目完成。
