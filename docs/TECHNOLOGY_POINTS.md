# 课程技术定位

| 技术 | 代码定位 | 业务价值 |
|---|---|---|
| 集合 | `task/NotificationQueue` | 并发队列保存通知任务 |
| 泛型 | `dao/BaseDao`、`Result`、`PageResult` | 统一 DAO 与查询结果类型 |
| 序列化 | `backup/BackupMetadata`、`BackupService` | 备份元数据读写与校验 |
| 多线程 | `task/NotificationQueue` | 后台通知不阻塞 UI，支持 shutdown |
| JDBC | `config/ConnectionFactory`、`service/TransactionRunner` | MySQL 连接和 commit/rollback |
