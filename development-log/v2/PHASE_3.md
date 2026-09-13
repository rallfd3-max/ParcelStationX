# V2 Phase 3 — Vue 基础、登录与 Dashboard

## 完成内容

创建 Vue 3 + TypeScript + Vite 前端，完成真实 Java API 登录、Pinia 会话、Router 守卫、深色工业驾驶舱、ECharts、ADMIN/STAFF 菜单差异及 loading/error/empty 状态。未实现 Phase 4 的拖拽与 Phase 5 的 Three.js。

## 后端支持

- 新增 `GET /api/dashboard/summary`，通过 WarehouseLayoutService 的真实 DAO snapshot 聚合今日入/出库、库存、异常、滞留、启用仓位利用率、快递公司件量和货架占用率。
- DashboardDto 显式输出前端所需字段，不返回 pickupCode 等敏感数据。
- Phase 2 HTTP 集成测试增加 dashboard endpoint 真实数据库断言。

## 前端结构

- `src/api/client.ts`：统一 envelope、Bearer、错误映射。
- `src/stores/session.ts`：login/me/logout/token 恢复与角色 getters。
- `src/stores/dashboard.ts`：真实 summary 加载、错误和刷新状态。
- `src/router/index.ts`：登录守卫、恢复会话、ADMIN settings 服务端/前端边界。
- `App.vue`：全局 TopBar 与角色菜单；STAFF 不显示 Settings。
- `LoginView.vue`：输入校验、真实登录、错误反馈。
- `DashboardView.vue`：六项首屏 KPI 与两个 ECharts 数据图。
- Placeholder 页面明确标注后续 Phase，不伪造业务功能。
- 响应式覆盖 1366 桌面宽度，并支持 prefers-reduced-motion。

## 依赖

Vue、Vue Router、Pinia、ECharts、Vite、TypeScript、Vitest、vue-tsc。`package-lock.json` 已提交以固定解析版本。npm 报告 3 个 moderate development dependency 审计项；当前构建可用，未运行可能引入 breaking change 的 `npm audit fix --force`。

## 测试结果

- `npm run type-check`：成功。
- `npm run test`：2 files / 4 tests 全部通过；覆盖 API Bearer/envelope/error 与 Session login/恢复失败清理。
- `npm run build`：成功，602 modules；主 bundle 1,141.75 kB（gzip 385.18 kB），存在 >500 kB warning，留给 Phase 7 code splitting/performance polish。
- `mvn clean test`：33 tests 全部通过。
- Dashboard endpoint 的真实 H2 API 断言补充后再次纳入最终 Java 回归。

## 关键文件

- `frontend/package.json`, `frontend/package-lock.json`
- `frontend/src/App.vue`
- `frontend/src/api/client.ts`
- `frontend/src/stores/{session,dashboard}.ts`
- `frontend/src/router/index.ts`
- `frontend/src/views/{LoginView,DashboardView}.vue`
- `frontend/src/components/{MetricCard,DataChart}.vue`
- `frontend/src/styles/main.css`
- `src/main/java/com/parcelstationx/api/dto/DashboardDto.java`
- `src/main/java/com/parcelstationx/api/http/ApiServer.java`

## 问题、修复与剩余风险

- 前端质量门首次即通过。
- ECharts 使当前入口 chunk 较大，但不影响 Phase 3 功能；Phase 7 应采用路由/图表异步分块。
- 当前本机没有运行真实 MySQL，因此未进行浏览器登录人工链路；API 数据源与 H2 HTTP 集成测试均为真实 DAO，不是静态 mock。
- Phase 4 可在现有 Warehouse/Parcel stores 约定上实现 2D preview -> API -> commit/rollback，并替换 warehouse/parcels placeholder。
