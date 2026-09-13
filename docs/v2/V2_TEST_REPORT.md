# ParcelStationX V2 测试报告

## 结论

截至 2026-09-13，Phase 0–7 已完成，Phase 8 为 `IN_PROGRESS`。Java 与前端自动质量门通过；真实 MySQL 和 Chrome/Edge 全链路缺少执行条件，不能宣布最终验收完成。

| 检查项 | 结果 | 证据 |
| --- | --- | --- |
| `mvn clean test` | 通过 | 37 tests，0 failure/error |
| `mvn -Pintegration-test verify` | 构建通过但未完成真实验收 | 2 个 MySQL IT 因环境变量缺失跳过 |
| `npm run type-check` | 通过 | Vue TSC 无错误 |
| `npm run test` | 通过 | 7 个文件、20 个测试 |
| `npm run build` | 通过 | Vite production build 成功 |

覆盖认证权限、HTTP 错误、仓储快照、乐观换位与 409 回滚、快件详情/事件/换位、异常创建/处理、ADMIN 用户与布局 API、场景映射、相机定位、拖拽状态及 500/1000 件性能基线。MySQL IT 已包含 V2 表结构和“入库待上架 → 换位 → 占用数变化 → 出库”，但本次未实际连接 MySQL。

## 未完成项

1. MySQL 8 上执行 schema/seed 或 V1 → V2 migration。
2. 非 skipped 状态运行两个 MySQL IT。
3. Chrome/Edge 完成 `V2_DEMO_SCRIPT.md` 全链路。
4. 核对 relocation、parcel event、operation log 和利用率数据库结果。

自动浏览器访问 Vite localhost 返回 `ERR_BLOCKED_BY_CLIENT`，当前也没有可用 Chrome 控制端，故未以页面构建成功冒充浏览器验收。
