# V2 最终功能完整性审计

审计日期：2026-09-13。审计范围包括正式 Vue 路由、Pinia/API 使用、Java Handler → Service → DAO 边界，以及 Phase 0–7 的关键回归面。

## 路由与功能

| 路由 | 页面 | 数据来源 | 状态 |
| --- | --- | --- | --- |
| `/login` | `LoginView` | auth API | 完整 |
| `/dashboard` | `DashboardView` | dashboard API | 完整 |
| `/warehouse` | `WarehouseView` | warehouse/relocate API | 完整 |
| `/digital-twin` | `DigitalTwinView` | warehouse/relocate API | 完整 |
| `/parcels` | `ParcelsView` | parcel-details/events/relocations API | 已补齐 |
| `/exceptions` | `ExceptionsView` | exception create/list/resolve API | 已补齐 |
| `/analytics` | `AnalyticsView` | dashboard API | 完整 |
| `/settings` | `SettingsView` | admin users/layout/slots API | 已补齐，ADMIN only |

正式路由已不再引用 `PlaceholderView.vue`，该文件已删除。STAFF 菜单不显示 Settings，前端路由守卫会重定向，所有 admin API 还会独立返回 403，权限不依赖菜单隐藏。

## 新增功能核对

- 快件中心支持运单/取件码/客户搜索，以及状态、快递、货架、仓位过滤；显示脱敏手机、停留时间、事件和换位历史，并可携带 `parcelId` 定位 2D/3D。
- Digital Twin 读取 route query，设置共享 `selectedParcelId` 并调用 `focusParcel`；Warehouse 同样读取 query 选择快件。
- 异常中心通过 `ExceptionService` 登记和处理。服务规则会同步 Parcel 状态、ExceptionRecord、ParcelEvent 和 OperationLog；它不直接改货位。操作后前端重载异常与 warehouse snapshot，Dashboard/Analytics 在重新进入或刷新时读取最新 API。
- 系统管理支持员工查看/新增/启禁用、布局参数保存、仓位启禁用和系统信息。占用中的 Slot 不能禁用。布局/仓位 mutation 后主动刷新共享 WarehouseStore。
- Web 备份/恢复未暴露：现有实现以服务器本地文件为边界，缺少安全的上传/下载协议，页面明确指向 legacy Swing 能力，没有伪按钮。

## 架构审计

新 Handler 只做 JSON、参数、鉴权与 DTO 转换；业务查询聚合在 `ParcelQueryService`，异常事务在 `ExceptionService`，布局/仓位校验在 `WarehouseLayoutService`，数据访问继续经过 DAO/JDBC。用户 DTO 和快件详情 DTO 均不序列化 `passwordHash`，手机号默认脱敏。

## Phase 0–7 回归审计

- Dashboard/Analytics 继续使用真实 API，无 mock 业务数据。
- 2D 与 3D 继续共用 WarehouseStore；relocate 成功提交、409 回滚逻辑未改。
- 3D query focus 复用既有 CameraController；drag/snap、GLB fallback 与 dispose 路径未被改写。
- 新页面保持现有深色工业主题，并具备 loading/error/empty 状态。

## 仍需外部验收

真实 MySQL migration/schema/seed 与非 skipped integration profile 已通过。应用内浏览器已验证登录、Dashboard 真实指标和 Web 快速入库；Chrome/Edge 的完整交互与视觉验收仍未全部执行，因此 Phase 8 保持 `IN_PROGRESS`。
