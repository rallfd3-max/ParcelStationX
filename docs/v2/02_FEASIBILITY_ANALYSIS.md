# V2 可行性分析

## 1. 总体结论

V2 可行性为高。现有系统已经具备 Java 17、JDBC DAO、事务、MySQL、登录、入出库、异常、通知、统计与备份，升级重点集中在“Web API + Vue + 2D/3D 可视化”，不需要重做核心业务。

## 2. 技术可行性

### Java 后端

使用 Java 17 `com.sun.net.httpserver.HttpServer` 增加轻量 REST 层，可继续复用现有 Service/DAO。这样既满足 Web 前后端通信，也避免引入 SpringBoot 等课程限制框架。

建议 API 层只负责：路由、JSON 解析、身份鉴权、参数校验、HTTP 状态映射和调用 Service。业务规则仍放在 Service。

### Vue 前端

Vue 3 + TypeScript + Vite 适合快速构建桌面优先的管理与大屏页面。Pinia 用于共享 warehouse/parcel/session 状态，Router 管理页面，ECharts 用于统计图表。

### 3D

Three.js 足够完成低多边形驿站、程序化货架、Parcel 实例、Raycaster 选择、OrbitControls、镜头动画和拖拽吸附。场景规模从几十到上千个简化箱体均可优化。

### Blender

Blender 只提供环境 GLB，不承担动态业务对象。即使开发环境没有 Blender，也可先用 Three.js primitive 完成全部逻辑，最后替换 `warehouse.glb`，不会阻塞业务开发。

## 3. 数据可行性

现有 Parcel/Shelf 模型不足以表达具体仓位，因此 V2 新增 ShelfLayout、ShelfSlot、ParcelRelocation，并给 Parcel 增加 `slot_id` 和 `version`。使用 migration 扩展，不删除原字段。

位置不直接存世界坐标，而由 ShelfLayout 的 transform + Slot level/column 计算，数据库与 3D 解耦且易维护。

## 4. 操作可行性

2D 拖拽是高频业务操作的主入口；3D 拖拽作为增强操作。即使某台电脑 3D 性能较弱，仍可通过 2D 和列表完成所有业务，因此系统不存在“必须依赖 3D 才能工作”的单点失败。

## 5. 性能可行性

- Parcel 使用 InstancedMesh 或低复杂度 Mesh；
- 货架程序化生成；
- 不在 render loop 中反复创建对象；
- Vue 页面卸载时释放 renderer/controls/listener/RAF；
- 目标首先保证 50 个演示快件稳定，再测试 500/1000 场景。

## 6. 课程可行性

V2 新增 HTTP 通信后，系统会真实使用网络编程。课程要求最多使用 3–5 项指定技术时，需要在最终答辩前重新核对实际代码并选择最终强调的 5 项，避免将所有出现过的技术都作为“选用项”陈述。

不得为了规避技术项而破坏已经稳定的泛型 DAO；是否需要调整，放在最终验收阶段根据教师口径判断，不在 V2 初期重构。

## 7. 风险与控制

### 风险 A：3D 做得很漂亮，但业务未接数据库
控制：所有 drag/drop 最终必须走 relocate API；服务端失败强制 rollback UI。

### 风险 B：过早追求高精模型导致进度失控
控制：Phase 5 先 primitive 环境 + 程序化货架，逻辑通过后才接 GLB。

### 风险 C：前后端重写破坏现有稳定代码
控制：Swing 保留；V2 从稳定 commit 分支开发；API 复用 Service，不复制业务逻辑。

### 风险 D：多人/多页面同时占用同一 Slot
控制：Parcel version optimistic locking + 服务端 Slot 空闲检查 + transaction + 409 冲突响应。

### 风险 E：前端状态与数据库不同步
控制：所有 mutation 以服务端响应为准；Pinia 统一更新；失败刷新对应 Parcel/Shelf/Slot。

### 风险 F：Three.js 内存泄漏
控制：每次页面 unmount 必须停止 RAF、dispose controls/renderer/material/geometry/texture、移除事件监听，并加入重复进入页面的 smoke test。

### 风险 G：真实 MySQL 环境不可用
控制：继续 H2 MySQL mode 单元/事务测试，但不得把 Phase 的真实 MySQL 验收标 DONE；保留明确人工验证步骤。

## 8. 时间与复杂度判断

复杂度从低到高：

- Dashboard / Vue 基础：中低；
- Java REST API：中；
- ShelfSlot 数据迁移：中；
- 2D 拖拽：中；
- Three.js 基础场景：中；
- 自动相机定位：中；
- 3D 拖拽 + slot snap + rollback：中高；
- Blender 美术优化：中。

最大技术风险在 3D 交互而非 Java/MySQL，因此必须放在数据/API/2D 稳定之后。

## 9. 可行性结论

方案适合作为现有 ParcelStationX 的增强版课程项目。只要严格分阶段，先保证业务一致性和测试，再逐步提升 3D 视觉，整体可控，并能显著提高演示与答辩辨识度。