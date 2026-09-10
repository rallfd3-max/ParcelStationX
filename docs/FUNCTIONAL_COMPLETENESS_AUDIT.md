# 功能完整性审计

审计日期：2026-09-10。审计基线：`codex/issue-1` 的 `f7a8b98`。

## 结论

上一轮将代码骨架误判为完整功能。Phase 0 保持完成；Phase 1 因数据库脚本尚未真实执行改为进行中；Phase 2 至 Phase 9 均恢复为待办。

## 逐项证据

1. DAO：只有 `CustomerDao`、`ShelfDao`、`ParcelDao` 接口，没有任何 `dao.impl` 或 JDBC CRUD。
2. 缩列 DAO：不存在 User、事件、异常、通知、日志 DAO 契约与实现。
3. JDBC：生产代码只有连接工厂和通用 `TransactionRunner`，没有 PreparedStatement 或 ResultSet。
4. 入库：`ParcelService` 仅校验格式和取件码，不创建快件、不查重、不分配货架、不写事件或日志，也未使用事务执行器。
5. 出库：未查询或更新数据库，未更新 `pickedUpAt`、货架占用、事件和日志。
6. 登录：`LoginFrame` 点击后无条件打开主窗口，没有读取 users 表或验证密码和权限。
7. UI：不存在要求的 11 个业务 Panel；MainFrame 使用 JLabel 和空 JTable 占位。
8. 分层调用：UI 没有注入 Service/DAO，完整调用链不存在。
9. 异常件：只有数据 record，没有创建、查询、处理、恢复、退回或日志服务。
10. 通知：内存队列会吞掉异常，不持久化状态，没有 retry 或入库成功后的提交。
11. 统计：所有指标、统计服务和 Java2D 图表均不存在。
12. 备份：仅序列化 `BackupMetadata`，没有 `BackupSnapshot`、业务实体数据或事务恢复。
13. 测试：仅 7 个浅层单元测试，没有 DAO CRUD、入出库事务、rollback、异常流、持久通知、统计或完整备份恢复测试。

## 数据库环境

- Windows 安装了名为 `MySQL` 的服务，但审计时状态为 Stopped。
- 3306 没有监听。
- 没有 `PARCEL_DB_*` / `MYSQL*` 环境变量，也没有本地 `application.properties`。
- `mysql.exe` 不在 PATH；这不等同于服务器未安装，因此下一步继续启动服务并使用 JDBC 探测。

## 基线测试

`mvn clean test` 成功：7 tests，0 failures，0 errors。该结果只能证明现有骨架可编译，不能证明业务验收完成。
