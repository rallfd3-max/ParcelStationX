# ParcelStationX V2 答辩指南

## 核心陈述

系统用 Java 17 原生 HTTP 服务、JDBC/MySQL 和 Vue/Three.js，让业务状态与二维、三维货位视图共享同一服务端事实来源。

讲解顺序：

1. 前端调用 API，API 进入 Service，Service 管规则/事务，DAO 使用 PreparedStatement JDBC。
2. Parcel 保存 `slotId` 与 `version`；RelocationService 在事务中校验源、目标和乐观版本，同步货位占用。
3. 2D/3D 共用 warehouse snapshot；前端只做乐观预览，409 时回滚。
4. 认证使用 SecureRandom bearer token、角色守卫和统一错误响应。
5. 按 `V2_TECHNOLOGY_TRACEABILITY.md` 定位五项课程技术。

常见问答：不用 Spring 是课程约束且便于展示底层能力；并发抢位由数据库约束、事务和版本控制共同处理；GLB 失败会降级为程序化几何体。V2.1 的八组业务货架全部来自 Shelf/ShelfLayout/ShelfSlot/MySQL，前端没有复制假货架；首页趋势和分布也来自 Java Service/DAO 数据。真实 MySQL 和应用内浏览器已有证据，但 Chrome/Edge WebGL 拖拽仍需人工验收，所以原 V2 Phase 8 如实保持进行中。

V2.2 答辩补充：MaiMaiYa 只作为 Java 后端的 OpenAI-Compatible Provider，Key 不进入 Vue、日志、Git 或 API 响应。自然语言先转为受校验的只读 Intent JSON，再由既有 Service/DAO 的 PreparedStatement 查询执行；系统没有“GPT 生成 SQL 后执行”的路径。异常建议不自动更新数据库。入库通知在业务事务 commit 后才进入已有 Queue/Gateway，滞留提醒以 `notification_records(parcel_id, notification_type)` 去重且发送前复查 `IN_STOCK`。真实 AI 不可用时，固定模板和错误边界保证入出库、数据库事务与 2D/3D 不受影响。
