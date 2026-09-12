# Phase 8 开发日志

## 完成内容

- 完成状态机、取件码、泛型容器、异步队列及序列化的自动回归测试。
- 现有 `seed.sql` 包含三个演示账号和三个货架；完整客户与快件批量演示数据待 MySQL 环境可用时导入生成。

## 测试结果

- `mvn clean test`：通过。
- 当前单元测试不依赖本机缺失的 MySQL 服务，因此可重复执行。

## 发现的问题与修复

- 本机未发现 MySQL CLI，无法执行数据库集成和 seed 导入；代码层测试保持独立可运行。

## 剩余风险

- 完整的 MySQL DAO 集成测试及 >=20 客户、>=50 快件演示数据需在有 MySQL 的答辩机执行。

## 第二轮实现

- 增加入库末端写日志失败时的 transaction rollback 测试，验证 parcels 与 shelf occupied 均回滚。
- 增加 `integration-test` Maven profile 和 MySQL JDBC schema 探测测试。
- 本机 MySQL 服务因权限无法启动，本阶段保持 `IN_PROGRESS`，不声称真实 MySQL 已验证。

## 第三轮验收

- 扩展 MySQL integration tests：演示数据规模、货架占用一致性、约束、登录、客户、入库、出库、事件和日志工作流。
- 真实 MySQL 未启动，阶段继续保持 `IN_PROGRESS`。

## 第三轮最终验收

- seed 货架占用修正为 17 / 17 / 16；MySQL integration test 增加占用与当前在库包裹一致性断言。
- integration workflow 使用独立手机号/运单号并在 `@AfterEach` 删除测试数据，不破坏 seed 演示数据。
- 真实 MySQL 因服务停止和凭据缺失未执行，Phase 8 保持 `IN_PROGRESS`。
