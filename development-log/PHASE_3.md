# Phase 3 开发日志

## 完成内容

- 实现快件状态机、取件码生成和入库基础输入校验。
- 实现 JDBC `TransactionRunner`：操作失败时 rollback，成功时 commit。
- 实现取件码校验和重复出库状态阻止规则。

## 测试结果

- `mvn clean test`：通过。
- 覆盖错误取件码、已取件状态不能再次转换，以及取件码格式和冲突回避。

## 发现的问题与修复

- 无代码错误。

## 剩余风险

- 多 DAO 的生产 SQL 写入将在后续 DAO 实现中接入现有事务执行器。

## 对下一阶段的影响

- Phase 4 可安全调用服务层，构建不含 SQL 的 Swing 应用外壳。

## 第二轮实现

- `AuthenticationService` 从 users 表读取账号并校验 SHA-256 密码及 enabled 状态。
- `CustomerService`、`ShelfService` 提供校验后的管理入口。
- `ParcelService.inbound/outbound` 在单一 JDBC transaction 中完成查重、匹配、货架容量、快件、占用、事件和操作日志更新。
- H2 MySQL 模式测试覆盖主流程、重复运单、满货架、错误取件码和重复出库。
