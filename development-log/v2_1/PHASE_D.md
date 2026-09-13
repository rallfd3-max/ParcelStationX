# V2.1 Phase D — Home Analytics Merge

## 完成内容

- 主导航“驾驶舱”改为“首页”，移除独立“分析”入口。
- `/analytics` 兼容地址重定向到 `/dashboard#analytics`。
- Dashboard 重建为纵向滚动运营首页：核心指标、14 天入库/出库/库存趋势、快递/状态/异常分布、货架/分区利用率、停留时长、最近异常和最近操作。
- 新增 Java `DashboardAnalyticsService`，统一从 DAO 支持的真实 MySQL 业务数据构建统计读模型。
- 新增 `/api/dashboard/trends`、`/api/dashboard/distributions`、`/api/dashboard/activity` 三组鉴权 API。
- 前端 DashboardStore 并行读取摘要、趋势、分布和活动 API。

## 新增/修改文件

- `src/main/java/com/parcelstationx/service/DashboardAnalyticsService.java`
- `src/main/java/com/parcelstationx/api/http/ApiServer.java`
- `src/main/java/com/parcelstationx/app/ParcelStationWebApplication.java`
- `frontend/src/types/api.ts`
- `frontend/src/stores/dashboard.ts`
- `frontend/src/views/DashboardView.vue`
- `frontend/src/router/index.ts`
- `frontend/src/App.vue`
- `frontend/src/styles/main.css`
- `TASKS_V2_1.md`

## 测试结果

- `mvn test`：37 个 Java 测试全部通过。
- `npm run type-check`：通过。
- `npm test -- --run`：10 个测试文件、25 个测试全部通过。
- `npm run build`：通过；仅保留既有 Three.js chunk 体积警告。

## 真实浏览器验收

- 新会话登录后导航仅显示“首页”，不再显示“驾驶舱”或独立“分析”。
- 首页读取真实 MySQL 数据并显示：今日入库 7、今日出库 1、库存 43、异常 9、滞留 17、仓位利用率 21.7%。
- 页面成功渲染 3 个趋势图、6 个分布/利用率图，以及最近异常和最近操作列表。
- `/analytics` 最终地址为 `/dashboard#analytics`，并定位到合并后的趋势区域。

## 发现的问题与修复

- Java `LocalDateTime` 在当前 JSON 配置中以数组返回，首页最初直接调用字符串 `replace` 导致渲染异常；新增兼容数组/字符串的时间格式化。
- 首次组合命令在仓库根目录调用 npm，因脚本位于 `frontend` 而失败；切换到正确工作目录后所有前端验证通过。

## 剩余风险 / 下一阶段影响

- 趋势当前为 14 天实时聚合，数据量增长后可进一步下沉为数据库按日聚合查询；当前演示规模性能充足，UI/API 层没有 SQL。
- Phase E 将进行多分辨率、MySQL、浏览器和重复 Three.js 生命周期的最终回归。
