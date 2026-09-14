# Phase W1 — Deterministic Shelf Management

## 目标与实施计划

- 在独立分支 `codex/dynamic-shelf-agent` 上增加确定性的单架/批量货架创建能力。
- 通过一个 JDBC transaction 同时写入 Shelf、ShelfLayout、ShelfSlot 与 OperationLog。
- 由 Java 完成区域规范化、连续货架编号和可读仓位编号；preview 不写数据库。
- 增加 ADMIN-only HTTP API，并覆盖参数边界、权限、编号、数量及批次 rollback。
- 运行 Java 全量回归与前端 type-check/test/build，浏览器验收按环境能力如实记录。

## 基线审计

- 基线：`origin/codex/visualization-v2` @ `9c2365a`。
- 原 V2.2 工作目录存在未提交 AI-3 改动，本阶段使用独立 worktree，未触碰该目录。
- `AbstractJdbcDao` 已提供 connection-aware `save/findAll/findById`，但接口未暴露；现有事务 Service 同样依赖 JDBC DAO 实现类。
- `ShelfLayoutDaoImpl.save(ShelfLayout)` 单连接 upsert 不适合结构创建事务；其继承的 `save(Connection, ...)` 可用于事务内 insert。
- 当前管理 API 只有布局修改和仓位启禁用，尚无结构创建 API。

## 完成内容

- 新增 `ShelfManagementService`、`ShelfCodeGenerator`、`ShelfMutationValidator` 及 typed request/result records。
- 支持单架、批量与只读 preview；`E区` 统一规范为 zone `E`，自动生成 `E-01` 及 `E-01-01-01` 风格编码。
- 每次结构 mutation 在一个 JDBC transaction 内创建 Shelf、ShelfLayout、全部 ShelfSlot 和 OperationLog；批次任一步失败整体 rollback。
- 新增 `POST /api/admin/shelves/preview`、`POST /api/admin/shelves`、`POST /api/admin/shelves/batch`，均由服务端强制 ADMIN（preview 也限制为 ADMIN）。
- 修复 `ShelfLayoutDaoImpl` 的 connection-aware save：由于 `shelf_id` 同时是业务外键与主键，通用 DAO 不能以非空 ID 判断已有行，改为事务内 upsert。
- `ShelfManagementServiceTest` 覆盖 1×5×6、4×5×6、唯一编码、preview 无写入、重复 code、参数范围和中途唯一键失败全回滚；`ManagementApiTest` 覆盖 ADMIN 成功、STAFF 403 与 preview 无写入。

## 验证

- `mvn -q test`：通过，53 tests，0 failure/error。
- `mvn -Pintegration-test verify`：构建通过；2 个 MySQL IT 因当前 worktree 未提供 `PARCEL_DB_*` 而 skipped，不能视为真实 MySQL 验收。
- `npm run type-check`：通过。
- `npm run test`：14 files / 37 tests 通过。
- `npm run build`：通过；仅保留 Vite 既有的大 chunk warning。
- 浏览器：W1 没有新增前端交互，HTTP 权限和 mutation 流程由真实随机端口 API 测试验证；可视化浏览器流程留到 W3。
- 初次前端门禁因新 worktree 无 `node_modules` 找不到 `vue-tsc`；执行 `npm ci` 后全部通过。安装时 npm 报 Node 22.17 低于两个间接包建议的 22.22.2，但没有阻止 type-check/test/build。

## 未完成项与下一阶段输入

- 外部项：真实 MySQL 下的 V2.3 新建批次流程需在后续集成阶段补充执行。
- W2 从本阶段的事务化结构 Service、布局 connection-aware save 和确定性 preview 坐标继续，实现 GRID/WIDE_MAIN_AISLE/TWO_SIDED_AISLE、防碰撞、move、resize 和安全停用。
