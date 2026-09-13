# ParcelStationX Digital Twin v2.0 — 项目计划书

## 1. 项目定位

V2 将现有 ParcelStationX 从传统 Swing/JDBC 管理系统升级为智慧快递驿站数字孪生可视化平台。现有 Java 业务层、DAO、事务、MySQL 和 Swing fallback 必须保留；V2 主要新增浏览器端驾驶舱、二维拖拽仓位、三维仓库数字孪生和 Java HTTP API。

## 2. 核心目标

- 深色工业风驾驶舱，中央空间可视化，左右 KPI/详情区。
- Vue 3 + TypeScript + Vite + Pinia + Vue Router。
- Three.js 展示真实比例驿站、货架、仓位和快件。
- ECharts 展示入库、出库、库存、异常、滞留和货架利用率。
- 待上架快件可拖到具体 Slot；已上架快件可换位。
- 点击快件后相机自动移动到对应货架正面并高亮。
- 3D 中支持 Parcel 拖拽，目标仓位可用时绿色，不可用时红色，松开后吸附 Slot。
- 所有移动、上架、出库最终都必须经过 Java Service + JDBC Transaction。

## 3. 必须保留

Java 17、现有 model、JDBC DAO、TransactionRunner、AuthenticationService、ParcelService、ExceptionService、NotificationService、StatisticsService、BackupService、MySQL schema/seed、Swing UI 和原有测试。

## 4. V2 新增

- Java HttpServer REST API；
- 会话认证与角色校验；
- Vue Web 前端；
- Dashboard；
- 2D Warehouse Layout；
- ShelfLayout / ShelfSlot / ParcelRelocation；
- Three.js Digital Twin；
- Blender GLB 环境模型加载；
- Camera focus / Raycaster / 3D drag / slot snap；
- optimistic locking；
- API、前端、3D 与 MySQL 集成测试。

## 5. 页面

### `/login`
数据库登录，进入系统后加载当前用户与权限。

### `/dashboard`
今日入库、今日出库、当前库存、异常件、滞留件、仓位使用率、快递公司占比、趋势、货架利用率、异常类型。

### `/warehouse`
左侧待上架快件，中间二维货架/仓位矩阵，右侧当前快件详情。支持搜索、拖拽上架、换位、失败回滚和切换 3D 定位。

### `/digital-twin`
三维驿站，自由旋转/缩放/平移，点击 Parcel/Shelf/Slot，搜索定位，自动镜头，详情侧栏，3D 拖拽与吸附。

### `/parcels`
表格辅助查询、筛选、事件历史、移动历史、3D 定位。

### `/exceptions`
异常件和滞留件管理。

### `/analytics`
ECharts 数据分析。

### `/settings`
管理员的用户、布局、备份恢复和系统信息。

## 6. 核心流程

### 入库上架
入库后快件进入待分配仓位状态（实现时在新增状态与 `IN_STOCK + slot_id IS NULL` 两种方案中选择一种并保持唯一语义）→ 2D 拖拽到 Slot → 服务端校验目标 → transaction → 更新 parcel/slot/version → Event + Relocation + Log → 2D/3D/统计同步。

### 换位
Parcel 拖到目标 Slot → `POST /api/parcels/{id}/relocate` → version/状态/目标空闲校验 → transaction → 记录 relocation/event/log → 返回新位置和新版本。

### 出库
取件校验 → 出库 transaction → Slot 清空 → 快件从 2D/3D 消失 → 利用率与 Dashboard 更新。

### 3D 定位
任意列表、搜索、2D 或 3D 点击 Parcel → `focusParcel(parcelId)` → SceneIndex 获取 Slot/Shelf → CameraController 计算正面视角 → 600–1000ms 平滑移动 → 高亮 → 展开详情。

## 7. 开发原则

- 先数据库/API，再 Vue，再 2D，再 3D。
- 先保证空间映射和业务一致性，再提高视觉效果。
- Parcel 不直接保存 world x/y/z；位置由 `parcel.slot_id -> shelf_slot -> shelf_layout` 计算。
- Blender 只负责地面、墙、门、服务台等环境；业务货架、Slot、Parcel 由 Three.js 程序化生成。
- 前端拖拽只是预览，服务器成功才确认最终位置；失败必须动画回滚。
- 不得为视觉效果绕开 Service/DAO/事务。

## 8. 最终演示

登录 → Dashboard → 入库 → 待上架区 → 2D 拖到 A01-02-03 → 数据库更新 → 3D 出现 → 点击后相机定位并高亮 → 查看详情 → 3D 拖到 B02-03-01 → relocation/event/log 更新 → 出库 → 3D 消失 → Dashboard 与利用率变化 → 异常与统计展示。

## 9. 非目标

不做大型物流园 GIS、Unity/Unreal 游戏级项目、分布式微服务、复杂 WMS 波次算法、AR/VR 和手机端完整 3D 拖拽。