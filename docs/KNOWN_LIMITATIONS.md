# 已知限制

- 本机 MySQL 9.6.0 已完成 V2 migration、seed、一致性检查与非跳过 integration tests。
- Codex 应用内浏览器已完成 localhost 登录、首页、入库、底部仓位持久化、3D 暂存/出库和三档分辨率检查；当前环境仍没有可控 Chrome/Edge，因此原 V2 Phase 8 按门禁规则保持 `IN_PROGRESS`。
- 仓库未内置定制 GLB 美术资产；3D 页面使用程序化货架与包裹作为降级显示。
- Web 备份/恢复仍保留在 legacy Swing：当前实现操作服务器本地文件，尚无适合浏览器的安全上传/下载协议。
- 通知为课程演示模拟，不连接真实短信网关。
- 1000 件场景测试使用确定性 mock 性能工具，不代表特定 GPU 的帧率承诺。

因此 V2.1 Phase A–E 已完成；原 V2 Phase 8 仍保持 `IN_PROGRESS`，待 Chrome/Edge 人工执行完整 WebGL 拖拽链路后方可改为 `DONE`。
