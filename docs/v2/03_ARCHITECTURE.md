# V2 架构设计

## 1. 总体架构

```text
Browser
  └─ Vue 3 + TypeScript
      ├─ Router
      ├─ Pinia
      ├─ ECharts
      └─ Three.js
            │ HTTP/JSON
            ▼
Java 17 HttpServer
  ├─ Router / Handler
  ├─ Session Auth
  ├─ DTO / JSON
  └─ API Controller
            │
            ▼
Existing Service Layer
  ├─ AuthenticationService
  ├─ ParcelService
  ├─ ExceptionService
  ├─ NotificationService
  ├─ StatisticsService
  └─ New WarehouseLayoutService / RelocationService
            │
            ▼
DAO / JDBC / TransactionRunner
            │
            ▼
MySQL
```

核心原则：Web 层不复制业务规则，只调用 Service；UI 不直接 SQL；3D 不直接持久化坐标。

## 2. 后端建议包结构

```text
com.parcelstationx
├─ app
├─ api
│  ├─ http
│  ├─ handler
│  ├─ dto
│  ├─ json
│  ├─ auth
│  └─ error
├─ config
├─ model
├─ dao
│  └─ impl
├─ service
├─ backup
├─ task
├─ ui          # legacy Swing 保留
└─ exception
```

### API 层职责

- 路由；
- method/path 匹配；
- JSON request/response；
- session 读取；
- role 校验；
- DTO 转换；
- BusinessException -> 4xx；
- DatabaseException/未知异常 -> 5xx；
- CORS（仅开发环境允许限定 localhost）；
- 静态前端资源服务可在最终阶段决定由 Java 提供还是独立 Vite preview。

## 3. 前端目录

```text
frontend/
├─ package.json
├─ vite.config.ts
├─ tsconfig.json
└─ src/
   ├─ api/
   ├─ components/
   ├─ layouts/
   ├─ router/
   ├─ stores/
   ├─ types/
   ├─ views/
   ├─ composables/
   ├─ charts/
   └─ three/
      ├─ WarehouseScene.ts
      ├─ WarehouseRenderer.ts
      ├─ CameraController.ts
      ├─ ShelfBuilder.ts
      ├─ SlotBuilder.ts
      ├─ ParcelRenderer.ts
      ├─ ParcelInteraction.ts
      ├─ DragController.ts
      ├─ SceneIndex.ts
      └─ sceneMath.ts
```

不得把全部 Three.js 代码写进单个 `.vue` 文件。

## 4. 状态设计

### SessionStore
currentUser、role、authenticated、login/logout/me。

### WarehouseStore
shelves、slots、layouts、selectedShelfId、selectedSlotId、loading、lastSyncAt。

### ParcelStore
parcels、waitingParcels、selectedParcelId、events、relocations、filters。

### DashboardStore
summary、trends、utilization、exceptions。

Mutation 规则：

1. 用户操作产生 preview；
2. 调 API；
3. success：以服务器响应更新 store；
4. 409/4xx：rollback preview；
5. 必要时重拉相关 parcel/slot/shelf。

## 5. 会话

不引入 Spring Security。可实现内存 SessionManager：

```text
ConcurrentHashMap<String, Session>
Session = userId + role + createdAt + expiresAt
```

Token 使用 `SecureRandom` 生成，不得用可预测 ID。推荐 HttpOnly SameSite Cookie；如果实现成本过高，可 Bearer token，但必须统一并写明安全边界。

服务端必须在 ADMIN API 上做真实 role 校验，不能只靠前端隐藏菜单。

## 6. HTTP 线程与现有多线程

HttpServer 使用受控 ExecutorService；必须设置线程上限并在应用停止时 shutdown。NotificationQueue 继续独立管理。

不得在 HTTP handler 中创建无限新线程。

## 7. 错误响应

统一格式：

```json
{"success":false,"data":null,"message":"目标仓位已被占用","code":"SLOT_OCCUPIED"}
```

成功：

```json
{"success":true,"data":{},"message":null}
```

建议状态码：

- 200 查询/更新成功；
- 201 创建成功；
- 400 参数错误；
- 401 未登录；
- 403 无权限；
- 404 不存在；
- 409 version/slot 冲突；
- 422 业务状态不允许；
- 500 未知服务端错误。

## 8. 前后端数据更新

V2 第一版不强制 WebSocket。所有用户主动操作立即用 API 返回值同步，Dashboard 可采用 10–30 秒轻量 polling。只有在全部核心功能完成后，如确有必要才考虑 Server-Sent Events；不得因实时推送扩展阻塞主线。

## 9. Legacy Swing

Swing 代码仍可直接运行。V2 新增 HTTP app 入口建议独立，例如：

- `ParcelStationApplication`：legacy Swing；
- `ParcelStationWebApplication`：HttpServer + API。

不得在 V2 初期删除 Swing 或让原有测试失效。

## 10. 构建

根目录继续 Maven 管 Java；`frontend/` 使用 npm。

阶段质量门至少包含：

```bash
mvn clean test
cd frontend && npm run type-check
cd frontend && npm run test
cd frontend && npm run build
```

真实 MySQL 阶段另执行：

```bash
mvn -Pintegration-test verify
```
