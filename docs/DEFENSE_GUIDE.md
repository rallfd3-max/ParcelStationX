# 答辩要点

- 不用 SpringBoot：课程要求直接展示 JDBC、事务、线程和泛型。
- 选 Swing：JDK 原生桌面 UI，部署简单，适合课程演示。
- JDBC 防注入：DAO 约定只用 PreparedStatement。
- 入库与出库事务：快件、货架占用、事件和日志必须同时成功或回滚。
- 集合用于通知队列；泛型复用 DAO/结果容器；序列化保存备份元数据；线程避免耗时通知卡住 EDT。
- 同一快件不能重复出库：`ParcelStateMachine` 只允许 `IN_STOCK -> PICKED_UP`。

## 第二轮代码定位

- 入库事务：`ParcelService.inbound`，同一 Connection 写 parcel、shelf、event、operation log，提交后通知。
- 出库事务：`ParcelService.outbound`，校验状态后更新 pickedUpAt、货架、event 和 log。
- SQL 注入防护：八个 `dao.impl` 仅通过 PreparedStatement 绑定数据值。
- 通知失败：`NotificationService` 持久化 FAILED 和 errorMessage，`retry` 增加 retryCount 后重新排队。
- UI 不阻塞：入库、出库、库存、统计和备份页面用 SwingWorker，完成后回 EDT 更新组件。
- 备份恢复：`BackupSnapshot` 序列化七类实体，`JdbcBackupDataStore.restore` 在事务内恢复原主键。
- Git 协作：每个 Phase 独立提交，审计提交 `9ebaa97` 主动纠正上一轮错误 DONE 状态。
