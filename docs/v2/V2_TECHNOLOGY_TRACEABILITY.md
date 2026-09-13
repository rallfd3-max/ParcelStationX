# V2 课程技术点追踪

| 技术点 | 代码位置/代表类 | 业务价值 | 答辩说明 |
| --- | --- | --- | --- |
| 集合 | `WarehouseLayoutService`、`SessionManager` | 组织仓储快照与在线会话 | 集合承载真实聚合和索引 |
| 泛型 | `BaseDao<T, ID>`、`AbstractJdbcDao<T>` | 统一 CRUD 合同并保证类型安全 | 减少重复而不丢失实体类型 |
| 序列化 | `BackupService.createBackup/restoreBackup` | 保存、恢复业务快照 | 对象流用 try-with-resources 管理 |
| 多线程 | `ApiServer`、`NotificationQueue`、Swing `SwingWorker` | HTTP 并发、后台通知、避免阻塞 EDT | I/O 在工作线程，UI 更新回 EDT |
| 数据库编程 | `*DaoImpl`、`RelocationService` | 持久化并保证换位原子性 | DAO 关闭资源，Service 控制事务回滚 |

V2 关键定位：`ShelfLayout`、`ShelfSlot`、`ParcelRelocation` 为领域模型；`RelocationService` 负责占用/版本/事务；`WarehouseLayoutService` 提供统一快照；`ApiServer` 是 REST 边界；`frontend/src/features/warehouse` 与 `digital-twin` 分别实现 2D 和 3D。V1 的详细登记仍见 `docs/03_REQUIREMENTS_TRACEABILITY.md`。
