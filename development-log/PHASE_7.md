# Phase 7 开发日志

## 完成内容

- 实现可序列化的 `BackupMetadata` 和文件读写服务。
- 对损坏备份文件提供明确业务异常。

## 测试结果

- `mvn clean test`：通过，备份写读和损坏文件测试通过。

## 第二轮实现

- BackupSnapshot 包含 customers、shelves、parcels、events、exceptions、notifications 和 operation logs。
- JdbcBackupDataStore 从数据库采集快照并在单一事务中清理、恢复全部业务表，校验版本和损坏文件。
- StatisticsService 实现全部核心指标、公司件量及货架占用率。
- 增加 DashboardPanel、StatisticsPanel、BackupPanel 和 Java2D BarChartPanel。
- 二次复核修复了清表后带 ID 实体误走 UPDATE 的问题；DAO `restore` 使用 PreparedStatement 显式恢复原主键。
