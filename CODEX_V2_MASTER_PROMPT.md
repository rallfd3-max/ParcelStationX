# CODEX_V2_MASTER_PROMPT.md

你正在开发 `ParcelStationX Digital Twin v2.0`。

## 0. 执行方式

**一次 Codex 运行只能完成一个 V2 Phase。**

启动后必须：

1. 确认当前分支是 `codex/visualization-v2`；
2. 按 `docs/v2/00_READ_FIRST.md` 的顺序读取全部 V2 文档；
3. 读取 `TASKS_V2.md`；
4. 找到编号最小的 `TODO` 或 `IN_PROGRESS` Phase；
5. 只实现该 Phase；
6. 完成编译/类型检查/测试/修复；
7. 更新该 Phase 状态和日志；
8. 独立 commit + push；
9. 停止，并汇报本 Phase 结果、测试结果、阻塞和下一 Phase 名称。

**禁止同一次运行继续下一 Phase。**

如果当前 Phase 由于真实环境/权限/凭据无法完成，可以把状态保留 `IN_PROGRESS`，完成所有不依赖阻塞项的工作并记录真实限制，然后停止。不得为了进入下一 Phase 而伪造验收。

## 1. 项目基线

基线来自稳定 commit `c7bf559`。现有 Java 17/JDBC/Service/DAO/Transaction/MySQL/Swing 功能是可复用资产，不是要丢弃的旧代码。

禁止：

- 删除 Swing legacy；
- 重写所有 Service/DAO；
- 使用 Spring/SpringBoot/MyBatis/Hibernate/JPA/ORM/SSM/SSH/Lombok；
- 把业务规则复制到 HTTP handler；
- 让前端直接决定数据库最终状态；
- 使用静态假数据冒充真实 API；
- 通过删除测试或跳过失败测试来“完成” Phase。

## 2. 技术栈

后端：Java 17、`com.sun.net.httpserver.HttpServer`、JDBC、MySQL、现有 Service/DAO/TransactionRunner、JUnit。

前端：Vue 3、TypeScript、Vite、Vue Router、Pinia、Three.js、ECharts、Vitest。

允许引入轻量 JSON 库。如果引入任何依赖，必须说明用途，并检查是否违反课程限制。

## 3. 架构约束

必须保持：

```text
Vue/Three/ECharts
      ↓ HTTP JSON
Java API handler
      ↓
Service
      ↓
DAO
      ↓
JDBC/MySQL
```

HTTP handler 只负责协议层。事务和业务校验在 Service。

3D 位置必须来自：

```text
Parcel.slotId -> ShelfSlot -> ShelfLayout -> world transform
```

不得在 Parcel 上保存任意 world XYZ 作为真实业务位置。

## 4. 可视化目标

最终风格为深色工业数字孪生驾驶舱，不是传统白底 CRUD 后台。主色语义：正常蓝青、选中橙、可放绿、滞留黄、异常/冲突红、禁用灰。

必须实现：

- Dashboard；
- Warehouse 2D 拖拽；
- Digital Twin 3D；
- 点击/搜索 Parcel 后相机自动定位到货架正面；
- 右侧 Parcel 详情；
- 3D drag + slot snap；
- API 失败 rollback；
- 2D/3D/统计同步。

## 5. Java HTTP 要求

优先使用 `HttpServer`。设计 Router、RequestContext、ApiResponse、SessionManager、AuthGuard、ExceptionMapper 等轻量组件。

身份权限必须服务端验证。ADMIN 和 STAFF 权限参考 `docs/v2/06_DATABASE_API_SPEC.md`。

Session token 必须使用不可预测随机值，不得记录到日志。

API 错误不能返回数据库凭据或完整 stack trace。

## 6. 数据库与 relocate

按 migration 扩展 ShelfLayout、ShelfSlot、ParcelRelocation、Parcel.slotId/version。

relocate 必须是事务：

- parcel/version/status；
- target slot enabled/empty；
- from/to shelf；
- parcel slot/version update；
- shelf occupied（若保留缓存）；
- relocation；
- event；
- operation log；
- commit/rollback。

版本或占用冲突返回 HTTP 409。

## 7. Vue 规则

Vue 页面不得直接包含数据库逻辑。API client 统一处理 JSON、401/403/409 和错误结构。

Pinia 负责 Session/Warehouse/Parcel/Dashboard 状态。

拖拽交互必须提供非拖拽替代操作。

任何 mutation：preview → API → success commit / failure rollback。

## 8. Three.js 规则

Three.js 代码拆分到 `frontend/src/three/`，不能全部塞进一个 Vue 文件。

必须有 SceneIndex、CameraController、ShelfBuilder、SlotBuilder、ParcelRenderer、Interaction/DragController。

Blender GLB 仅环境；Shelf/Slot/Parcel 动态生成。

GLB 缺失时 primitive fallback 仍必须可用。

页面卸载必须 cancel RAF、dispose controls/renderer/资源、移除 listener。

## 9. 测试规则

每个 Phase 先运行基线测试，再实现，再测试。

Java：

```bash
mvn clean test
```

前端创建后：

```bash
cd frontend
npm run type-check
npm run test
npm run build
```

真实 MySQL 阶段：

```bash
mvn -Pintegration-test verify
```

不得因为环境没有 MySQL 就把真实 MySQL Phase 标 DONE。

## 10. Git 规则

只在 `codex/visualization-v2` 工作。

每 Phase 至少一个独立 commit，建议格式：

```text
v2-phase-0: audit stable baseline
v2-phase-1: add java http api foundation
v2-phase-2: add slots layouts and relocation transaction
v2-phase-3: add vue shell login and dashboard
v2-phase-4: implement 2d warehouse drag workflow
v2-phase-5: implement digital twin scene and camera focus
v2-phase-6: implement 3d drag snap and state sync
v2-phase-7: integrate warehouse glb and polish analytics performance
v2-phase-8: complete mysql and browser acceptance
```

不要合并 main，不要创建最终 PR，除非用户明确要求。

## 11. 阶段日志

每次完成/停止时写：

`development-log/v2/PHASE_X.md`

包含：目标、改动、命令、测试数量/结果、修复、未完成项、真实阻塞、下一 Phase 输入。

## 12. 判定 DONE 的规则

“文件存在”“页面能打开”“测试能编译”都不足以判 DONE。必须满足 `TASKS_V2.md` 当前 Phase 的业务验收。

尤其：

- Vue 不得用 mock 作为最终数据；
- 2D drag 必须持久化；
- Camera focus 必须定位正确 Parcel；
- 3D drag 必须调用 relocate API；
- API 失败必须 rollback；
- 真实 MySQL 未验证时最终 Phase 不得 DONE。

## 13. 每次运行最后输出

只输出本 Phase 的：

- Phase 名称；
- 实现摘要；
- 关键文件；
- 测试命令与结果；
- commit SHA；
- 是否 push 成功；
- 当前阻塞；
- 下一 Phase 名称。

然后停止，等待下一次 Codex 运行自行重新读取仓库。