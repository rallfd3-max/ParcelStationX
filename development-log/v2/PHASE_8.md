# V2 Phase 8 — 最终验收（IN_PROGRESS）

## 目标与完成内容

执行真实 MySQL、Java、前端、桌面浏览器全链路并补齐交付文档。本次完成所有当前环境可自动执行工作，但真实 MySQL/浏览器链路受阻，不能标记 DONE。

- 强化 `MySqlConnectionIT`，将 V2 三张表纳入结构断言。
- 强化 `MySqlWorkflowIT`，覆盖待上架、换位、占用数变化和出库。
- 更新 README、已知限制、任务状态；新增测试报告、答辩指南、演示脚本、技术追踪。
- 完成 `/parcels`、`/exceptions`、`/settings`，删除正式路由 Placeholder。
- 新增快件聚合详情、异常事务和 ADMIN 用户/布局/仓位 API，并完成 H2 HTTP 测试。
- 新增最终功能完整性审计，复核 Phase 0–7 的状态同步、3D focus/fallback/dispose 回归面。

## 命令与结果

- `mvn clean test`：通过，37 tests。
- `mvn -Pintegration-test verify`：真实 MySQL 9.6.0 通过，2 tests，0 skipped。
- `npm run type-check`：通过。
- `npm run test`：通过，7 files / 20 tests。
- `npm run build`：通过。
- 浏览器访问 Vite localhost：`ERR_BLOCKED_BY_CLIENT`；当前无可用 Chrome/Edge 控制面。

## 阻塞与恢复

已备份并迁移本机数据库，seed 数量和占位一致性通过；应用内浏览器也已验证登录、Dashboard 与快速入库。剩余阻塞为 Chrome/Edge 完整拖拽、3D 与视觉链路证据，Phase 8 保持 IN_PROGRESS。
