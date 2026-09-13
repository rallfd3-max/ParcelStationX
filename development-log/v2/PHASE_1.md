# V2 Phase 1 — Java HTTP / JSON / Session 基础

## 目标与完成情况

Phase 1 已完成。新增基于 Java 17 `com.sun.net.httpserver.HttpServer` 的轻量 Web 层，复用 AuthenticationService 和 ParcelDao；未创建 frontend，未修改数据库结构，也未提前实现 ShelfSlot/relocation。

## 实际改动

- 新增独立启动入口 `ParcelStationWebApplication`，默认监听 `127.0.0.1:8080`，可用 `PARCEL_HTTP_PORT` 调整。
- 新增 `ApiServer`、`Router`、`Route`、`RequestContext`、`ApiHandler`、`ApiResponse`。
- 新增 Jackson `JsonCodec`；仅用于 JSON 编解码，不是 Web/业务框架。
- 新增线程安全 `SessionManager`：SecureRandom 256-bit URL-safe token、8 小时过期、logout 失效。
- 新增统一安全错误映射，未知异常只返回固定 500 文案，不返回堆栈或数据库细节。
- 新增 UserDto、ParcelDto、LoginRequest/LoginResponse；UserDto 不包含 passwordHash，ParcelDto 不包含 pickupCode。
- HttpServer 使用有上限的固定线程池，`close()` 同时 stop server 和 shutdown executor。

## API

- `GET /api/health`（公开）
- `POST /api/auth/login`（公开）
- `GET /api/auth/me`（登录）
- `POST /api/auth/logout`（登录）
- `GET /api/parcels`（登录，只读，复用 ParcelDao）
- `GET /api/parcels/{id}`（登录，只读，复用 ParcelDao）
- `GET /api/admin/ping`（ADMIN，用于服务端角色保护基线与测试）

认证统一使用 `Authorization: Bearer <token>`。Phase 3 的 API client 将复用该约定。

## 关键设计决策

- Handler 只处理 HTTP/DTO/鉴权并调用现有 Service/DAO；没有 SQL。
- 保留 Swing `ParcelStationApplication`，Web 使用独立入口，互不替换。
- Route 将路径模板编译为正则并提取参数；同路径错误 method 返回 405，不存在路径返回 404。
- 请求体限制为 1 MiB；响应设置 JSON UTF-8 与 `Cache-Control: no-store`。
- API 输出使用显式 DTO，避免数据库模型中的敏感字段被意外序列化。

## 测试

执行：

- `mvn spotless:apply`：成功。
- `mvn clean test`：BUILD SUCCESS；27 tests，0 failures，0 errors，0 skipped。

新增 `ApiServerTest` 共 9 项：

1. public health；
2. login -> me -> parcel list -> logout 完整会话；
3. 错误密码返回 401 且不泄漏输入/密码字段；
4. 未登录 parcel query 返回 401；
5. STAFF 访问 ADMIN route 返回 403；
6. malformed JSON 返回 400；
7. 404/405 区分；
8. parcel detail、missing ID、invalid ID；
9. 未知内部异常返回固定 500，不泄漏堆栈/数据库详情。

## 修改文件

- `pom.xml`
- `src/main/java/com/parcelstationx/app/ParcelStationWebApplication.java`
- `src/main/java/com/parcelstationx/api/auth/SessionManager.java`
- `src/main/java/com/parcelstationx/api/dto/*`
- `src/main/java/com/parcelstationx/api/error/*`
- `src/main/java/com/parcelstationx/api/http/*`
- `src/main/java/com/parcelstationx/api/json/JsonCodec.java`
- `src/test/java/com/parcelstationx/api/ApiServerTest.java`
- `TASKS_V2.md`

## 发现的问题与修复

- 为避免 JSON 自动暴露 record 全字段，API 使用白名单 DTO；用户密码哈希和快件取件码均未出现在基础查询响应。
- 为避免 HttpServer 默认执行器与应用生命周期不清晰，使用固定线程池并在 server close 时显式关闭。

## 未完成项、阻塞与下一阶段输入

- 本阶段无阻塞。
- Phase 2 需要扩展 Parcel 的 slotId/version，并同步修改 DAO SQL、backup snapshot 和既有测试构造参数。
- Phase 2 新增 migration、layout/slot/relocation DAO 与事务 Service 后，再扩展现有 ApiServer 的 warehouse/relocate routes。
