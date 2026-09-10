# 课程技术定位

| 技术 | 类与方法 | 业务价值 |
|---|---|---|
| 集合 | `NotificationQueue.pending`、`StatisticsService.calculate` | 通知排队和统计聚合 |
| 泛型 | `BaseDao<T,ID>`、`AbstractJdbcDao<T>`、`Result<T>`、`PageResult<T>` | 八类 DAO 复用 CRUD |
| 序列化 | `BackupSnapshot`、`BackupService.backup/restore` | 保存七类业务数据并恢复数据库 |
| 多线程 | `NotificationQueue.submit/close`、各 Panel 的 `SwingWorker` | 后台执行并安全关闭 |
| JDBC | `AbstractJdbcDao`、各 `*DaoImpl`、`TransactionRunner.run` | PreparedStatement、ResultSet、commit/rollback |
