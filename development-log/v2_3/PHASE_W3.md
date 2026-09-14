# Phase W3 — Web Layout Manager & 2D/3D Sync

## 完成内容

- Settings 升级为深色工业风布局管理器：批量表单、服务器 preview、确认创建、位置/尺寸编辑与停用。
- preview 显示 ghost shelf 编码、坐标和仓位数量，取消无数据库写入。
- WarehouseView 使用 Snapshot 对任意 zone 动态分组；不再依赖 A/B/C/D。
- mutation 成功后通过 WarehouseStore refresh 驱动 2D/3D 同源刷新。
- WarehouseScene 的异步 GLB load 新增 disposed guard 与幂等 dispose，避免旧场景索引与资源复活。
- Vite proxy 可通过 `VITE_API_PROXY` 指向隔离的后端端口。

## 验证

- `npm run type-check` 通过。
- `npm run test`：14 files / 38 tests 通过，新增 arbitrary-zone grouping 覆盖。
- `npm run build` 通过；仅既有 bundle size warning。
- `mvn clean test`：60 tests 通过。
- 浏览器尝试启动隔离 API 18080 和 Vite 15173。模型切换后进程会话失效；重启后 in-app browser 对 loopback 报 `ERR_CONNECTION_REFUSED`，因此不将浏览器验收标记为通过。

## 下一阶段输入

- W4 可复用 server preview/result DTO，在 Settings 中加入 AI command plan/confirm。
