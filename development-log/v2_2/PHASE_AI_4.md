# Phase AI-4 — Intelligent Notifications

## 完成内容

- `ParcelStationWebApplication` 完整 wiring `NotificationRecordDaoImpl`、`NotificationQueue`、`MockSmsGateway`、`NotificationService` 和 `OverdueNotificationScheduler`，并通过 `ParcelService.withNotifications` 启用 Web 入库通知。
- 保持 `ParcelService.inbound` 的事务返回后才调用通知：rollback 不创建通知记录；成功路径为 PENDING → queue → SUCCESS/FAILED。
- `MockSmsGateway` 明确输出 `[SMS MOCK] simulated delivery`，手机号仅显示前三后四位，不宣称真实外部短信送达。
- 新增占位符文案服务。MaiMaiYa 只接收 `{{COURIER}}`、`{{PICKUP_CODE}}`、`{{DAYS}}`、`{{LOCATION}}` 等模板要求；真实手机号和 pickupCode 不进入 prompt。非法/超长/失败输出自动使用固定模板。
- 新增可配置 7/10/15 天阶段、扫描周期、最大重试与 mock SMS mode；非 mock 模式在未实现真实供应商合同时明确拒绝，不伪造 Real gateway。
- `OverdueNotificationScheduler` 提供 `runOnce/start/close`、注入 `Clock`、受控 daemon `ScheduledExecutorService` 和关闭状态。
- `notification_records` 按 parcel + stage 持久去重；FAILED retry 更新原记录并增加 retry count，不无限新增。
- gateway 发送前重新读取 Parcel，非 `IN_STOCK` 的 overdue 记录取消为 FAILED/CANCELED，不调用 gateway。
- 增加 V2.2 非破坏索引 migration。

## 验证

- `mvn clean test`：PASS，56 tests，0 failure/error。
- `npm run type-check`：PASS。
- `npm run test -- --run`：PASS，15 files / 39 tests。
- `npm run build`：PASS（既有 bundle size warning）。
- 自动测试覆盖 Day 6、7、8/9、10、15、重复 run、scheduler restart、picked-up、gateway failure/retry、AI 文案 fallback、Key/手机号不进 prompt、rollback 0 notification 与 executor close。

## 修复

- Gateway 异常只持久化异常类型，不保存可能含手机号/凭据的原始 message。
- Scheduler 单轮异常不会终止后续调度，也不会影响 Web 核心服务。

## 下一阶段输入

- AI-5 执行真实 MySQL migration/integration、全量回归、MaiMaiYa API/API 页面 smoke、浏览器全链路、安全 grep 与最终文档审计。
