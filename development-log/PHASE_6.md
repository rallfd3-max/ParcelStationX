# Phase 6 开发日志

## 完成内容

- 增加基于 `ConcurrentLinkedQueue` 和固定线程池的模拟通知队列。
- 任务异常被隔离，关闭时等待并强制终止遗留任务。

## 测试结果

- `mvn clean test`：通过，通知任务执行与 shutdown 验证通过。

## 剩余风险

- 通知持久化需在可用 MySQL 环境中连接 `notification_records` 表。

## 第二轮实现

- ExceptionService 事务化创建/处理异常，支持恢复 IN_STOCK 或 RETURNED，并写事件和操作日志。
- NotificationService 持久化 PENDING/SUCCESS/FAILED、retryCount，复用可关闭通知队列。
- ParcelService 在入库事务提交后才异步提交通知。
- 新增 ExceptionPanel 和数据库级异常/通知测试。
- 可注入 NotificationGateway 真实模拟失败，测试覆盖 FAILED 后 retry 成功和 retryCount。
