# 已知限制

- 本机 MySQL 9.6.0 已完成 V2 migration、seed、一致性检查与非跳过 integration tests。
- 自动浏览器阻止访问 localhost（`ERR_BLOCKED_BY_CLIENT`），且没有可控 Chrome/Edge，尚未完成真实数据库桌面浏览器全链路。
- 仓库未内置定制 GLB 美术资产；3D 页面使用程序化货架与包裹作为降级显示。
- Web 备份/恢复仍保留在 legacy Swing：当前实现操作服务器本地文件，尚无适合浏览器的安全上传/下载协议。
- 通知为课程演示模拟，不连接真实短信网关。
- 1000 件场景测试使用确定性 mock 性能工具，不代表特定 GPU 的帧率承诺。

因此 Phase 8 保持 `IN_PROGRESS`，真实 MySQL 与 Chrome/Edge 全链路通过后方可改为 `DONE`。
