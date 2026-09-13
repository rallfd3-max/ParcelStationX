# ParcelStationX V2 演示脚本

## 准备

1. 启动 MySQL 8，执行 schema 与 seed，设置三个 `PARCEL_DB_*` 变量。
2. 执行 `mvn -Pintegration-test verify`，确认 MySQL IT 实际执行并通过，而非 skipped。
3. 启动 Web API 与 Vite，在 Chrome/Edge 以至少 1366×768 打开页面。

## 主链路

1. 用 `admin/admin123` 登录，确认 Dashboard 数据来自 API。
2. 新增或选择客户并入库，记下快件编号。
3. 在 2D 仓库确认快件位于待上架区，拖到空 Slot A；刷新后位置应保持。
4. 拖到占用货位，确认 409 提示且对象回原位。
5. 进入 3D，搜索该快件并自动定位，核对详情、货架和 Slot A。
6. 拖到空 Slot B，确认吸附成功，并核对 relocation、event、operation log。
7. 出库后确认快件从 2D/3D 消失、货位释放、Dashboard 利用率更新。
8. 演示异常与统计；若 V2 Web 仍是占位页，应明确使用 Swing 或 API，不得声称已有完整 Web 页面。
9. 退出登录，确认受保护页面不可访问。

补充检查：过期 `version` 返回 409；GLB 缺失时 fallback 可用；反复进入退出 3D 无叠加 RAF/listener。通过后记录浏览器/MySQL 版本、时间、操作者和截图，再将 Phase 8 改为 `DONE`。
