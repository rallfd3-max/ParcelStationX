# 已知限制

- 当前 Phase 8 环境未提供三个 `PARCEL_DB_*` 变量，真实 MySQL 集成测试被跳过；H2 不能替代 MySQL 8 最终验收。
- 自动浏览器阻止访问 localhost（`ERR_BLOCKED_BY_CLIENT`），且没有可控 Chrome/Edge，尚未完成真实数据库桌面浏览器全链路。
- 仓库未内置定制 GLB 美术资产；3D 页面使用程序化货架与包裹作为降级显示。
- `/parcels`、`/exceptions`、`/settings` 仍为 V2 路由占位页；遗留 Swing 功能及部分 REST 能力仍保留，但尚未迁移为完整 V2 Web 页面。
- 通知为课程演示模拟，不连接真实短信网关。
- 1000 件场景测试使用确定性 mock 性能工具，不代表特定 GPU 的帧率承诺。

因此 Phase 8 保持 `IN_PROGRESS`，真实 MySQL 与 Chrome/Edge 全链路通过后方可改为 `DONE`。
