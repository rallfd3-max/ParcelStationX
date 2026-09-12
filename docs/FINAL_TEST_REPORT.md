# 最终测试报告（第二轮）

环境：OpenJDK 17.0.18、Maven 3.9.14、Windows 11。

- `mvn clean test`：18 tests，0 failures，0 errors。
- 覆盖 JDBC CRUD、数据库登录、入出库事务、重复运单、满货架、错误/重复取件、rollback、异常流、通知持久化、统计和序列化。
- `mvn -Pintegration-test verify` 可运行；因未配置 `PARCEL_DB_*`，真实 MySQL 探测按条件跳过。

- MySQL integration tests 已扩展为数据规模、货架占用、约束和完整工作流断言；当前因服务停止而未执行。

seed 预期校验：3 employees、20 customers、50 parcels、10 exception records、至少 30 operation logs；货架 occupied 必须为 17/17/16，且与 `IN_STOCK`/`EXCEPTION` 包裹计数一致。

Windows 存在 MySQL 服务，但当前进程无权限启动，3306 未监听，也没有数据库凭据。因此 Phase 1、8、9 保持 `IN_PROGRESS`。
