# ParcelStationX V2 测试报告

## 结论

截至 2026-09-13，Phase 0–7 已完成，Phase 8 为 `IN_PROGRESS`。Java、前端与真实 MySQL 自动质量门通过；Chrome/Edge 全链路仍未完成，不能宣布最终验收完成。

| 检查项 | 结果 | 证据 |
| --- | --- | --- |
| `mvn clean test` | 通过 | 37 tests，0 failure/error |
| `mvn -Pintegration-test verify` | 通过 | MySQL 9.6.0，2 个 IT 实际执行，0 skipped |
| `npm run type-check` | 通过 | Vue TSC 无错误 |
| `npm run test` | 通过 | 10 个文件、25 个测试 |
| `npm run build` | 通过 | Vite production build 成功 |

覆盖认证权限、HTTP 错误、仓储快照、乐观换位与 409 回滚、拖拽边缘滚动、快件详情/事件/换位、异常创建/处理、ADMIN 用户与布局 API、场景映射、动态相机构图、tooltip 内容、拖拽状态及 500/1000 件性能基线。MySQL IT 已在本机真实数据库完成八架/240 仓位结构和“入库待上架 → 换位 → 占用数变化 → 出库”验证。

## 未完成项

1. Chrome/Edge 完成 `V2_DEMO_SCRIPT.md` 全链路。
2. 核对完整 UI 链路中的 relocation、parcel event、operation log 和利用率变化。

Codex 应用内浏览器已能访问 Vite localhost，并完成 V2.1 三档分辨率、底部仓位持久化、3D 快速入库/暂存详情/出库、长首页和十次 3D 进出回归。当前仍没有可用 Chrome/Edge 控制端，故原 V2 Phase 8 继续如实保持进行中。
