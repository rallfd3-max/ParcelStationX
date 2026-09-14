# ParcelStationX V2 测试报告

## 结论

截至 2026-09-14，V2.2 AI-1 至 AI-5 已完成；原 V2 Phase 8 仍为 `IN_PROGRESS`。Java、前端与真实 MySQL 自动质量门通过，原 V2 的其他最终验收仍按记录跟进。

| 检查项 | 结果 | 证据 |
| --- | --- | --- |
| `mvn clean test` | 通过 | 78 tests，0 failure/error |
| `mvn -Pintegration-test verify` | 通过 | MySQL，2 个 IT 实际执行，0 skipped |
| `npm run type-check` | 通过 | Vue TSC 无错误 |
| `npm run test` | 通过 | 16 个文件、46 个测试 |
| `npm run build` | 通过 | Vite production build 成功 |

覆盖认证权限、HTTP 错误、仓储快照、乐观换位与 409 回滚、拖拽边缘滚动、快件详情/事件/换位、异常创建/处理、ADMIN 用户与布局 API、场景映射、动态相机构图、tooltip 内容、拖拽状态及 500/1000 件性能基线。MySQL IT 已在本机真实数据库完成八架/240 仓位结构和“入库待上架 → 换位 → 占用数变化 → 出库”验证。

## 未完成项

1. Chrome/Edge 完成 `V2_DEMO_SCRIPT.md` 全链路。
2. 核对完整 UI 链路中的 relocation、parcel event、operation log 和利用率变化。

Codex 应用内浏览器已能访问 Vite localhost，并完成 V2.1 三档分辨率、底部仓位持久化、3D 快速入库/暂存详情/出库、长首页和十次 3D 进出回归。当前仍没有可用 Chrome/Edge 控制端，故原 V2 Phase 8 继续如实保持进行中。

V2.1.1 热修复回归进一步确认：首页实际渲染 1 个折线图、1 个环形图、2 个饼图和 3 个柱状图；六个代表性快件覆盖前后排、左右端与首末列，定位后的运单号、货架和仓位均与详情面板一致；直接点击 3D 快件也能切换到对应详情。全新浏览器会话的首页与数字孪生页均无控制台错误。

ECharts 路由生命周期修复后，Codex 应用内浏览器连续执行 5 轮“首页 → 仓库作业 → 首页 → 数字孪生 → 首页 → 快件 → 首页”，共 15 次返回首页均确认 line、donut、pie、bar 的 canvas 存在且尺寸非零，无需 F5。浏览器 Console 无 ECharts 重复初始化、零尺寸、已销毁实例、ResizeObserver 或 Vue 警告。

## V2.2 AI 补充验收

- 真实 MySQL 已执行并核验 `V2_2_0__ai_notification_indexes.sql` 的三个索引。
- 已完成真实 MaiMaiYa OpenAI-Compatible 短响应 smoke；报告不包含端点凭据或 Key。
- 应用内浏览器已验证本地登录、真实 Dashboard 聚合、`/ai` 页面，以及“删除所有快递”被只读白名单拒绝，未触发变更。
- 运营洞察改为发送 Java 本地聚合后的小 context、320-token 结构化输出和 20-second 请求上限。真实 MaiMaiYa Dashboard 返回约 9.6 秒并正常展示总结/风险/建议；暂时性上游故障会返回本地聚合 fallback。
