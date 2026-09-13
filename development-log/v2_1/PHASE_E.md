# V2.1 Phase E — Full Regression

## 最终结论

V2.1 Phase A–E 已完成。Java、前端、真实 MySQL 和 Codex 应用内浏览器回归通过；由于当前没有可控 Chrome/Edge，原 `TASKS_V2.md` Phase 8 仍按其严格门禁保持 `IN_PROGRESS`。

## 自动质量门

- `mvn clean test`：37 tests，0 failure/error。
- 配置真实 `PARCEL_DB_*` 后执行 `mvn -Pintegration-test verify`：2 IT 实际执行，0 skipped/failure/error。
- `npm run type-check`：通过。
- `npm test -- --run`：10 files、25 tests 全部通过。
- `npm run build`：通过；仅有既有 Three.js chunk 大小提示。
- 数据库：8 个业务货架、240 个真实仓位、无失效 slot 引用；V2.1 migration 连续执行两次成功。

## 浏览器回归

- 1366×768、1440×900、1920×1080：每档首页均显示 9 个 ECharts 图表和 2 个活动列表。
- 1366×768 二维仓库：独立画布 `clientHeight=416`、`scrollHeight=3779`，确认可独立滚动。
- 底部仓位：`ACCEPT20260914` 刷新后仍位于 `C-01-04-05`。
- 3D 快速入库/暂存详情/客户脱敏/取件码/两步确认出库：通过。
- `/analytics`：重定向为 `/dashboard#analytics`。
- 在全新浏览器页连续 10 次进入/退出数字孪生：控制台新增错误为 0。

## 回滚与冲突

- WarehouseStore 单测继续覆盖 409 后恢复原对象并刷新服务端状态。
- RelocationService 测试覆盖占用目标、旧版本和事务一致性；真实 MySQL workflow IT 覆盖入库、换位、occupied 变化和出库。

## 文档更新

- 更新 README 的 V2.1 迁移、启动和功能说明。
- 更新已知限制、测试报告、最终功能审计和答辩指南。
- 保留原 V2 Phase 8 `IN_PROGRESS`，没有把应用内浏览器证据虚报成 Chrome/Edge 验收。

## 剩余人工项

- 在 Chrome 或 Edge 执行长时间 HTML5/WebGL 拖拽手感与 hover 像素命中检查；通过后再关闭原 V2 Phase 8。
