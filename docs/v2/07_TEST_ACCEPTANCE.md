# V2 测试与最终验收规范

## 1. 原则

V2 不能以“能启动页面”作为完成标准。每个 Phase 必须有与其职责对应的自动测试或可重复人工验收步骤。测试失败不得通过跳过、删除断言、改成永远通过来处理。

## 2. Java 基线

每阶段至少执行：

```bash
mvn clean test
```

涉及真实 MySQL 时执行：

```bash
mvn -Pintegration-test verify
```

现有 18+ 测试不得回归。

## 3. API 测试

至少覆盖：

- login success/failure；
- session/me/logout；
- 401/403；
- parcel list/detail；
- inbound；
- relocate success；
- relocate occupied slot conflict；
- relocate stale version conflict；
- relocate transaction rollback；
- outbound；
- exception create/resolve；
- dashboard/statistics response；
- JSON malformed request；
- 404/405；
- 统一错误结构。

优先在 Java 测试中启动临时 HttpServer 监听随机端口，避免依赖固定端口。

## 4. Migration / MySQL

真实 MySQL 验收：

- 原 schema + V2 migration 可顺序执行；
- 旧数据不丢失；
- ShelfLayout/ShelfSlot/Relocation 创建成功；
- Parcel.slot_id/version 正确；
- FK/UNIQUE/CHECK/索引符合预期；
- seed 能生成完整 layout/slots；
- shelf occupied 与 parcel slot 分配一致；
- relocation rollback 不留下半状态。

没有真实 MySQL 时可继续 H2 MySQL mode，但对应最终状态必须保持 IN_PROGRESS。

## 5. 前端测试

建议使用 Vitest。至少覆盖：

- Session store；
- API client success/error mapping；
- Warehouse store initial snapshot；
- relocate optimistic preview；
- relocate success commit；
- relocate 409 rollback；
- slot availability；
- selected parcel；
- scene mapping pure functions；
- camera target calculation；
- drag state machine。

每阶段执行：

```bash
cd frontend
npm run type-check
npm run test
npm run build
```

## 6. 2D UI 验收

至少验证：

- 待上架 Parcel 正确显示；
- 空 Slot/占用 Slot 状态正确；
- drag 到空 Slot 成功；
- drag 到占用 Slot 不成功；
- 服务端 409 后对象回原位；
- 非拖拽“移动到...”操作可用；
- 搜索和过滤不破坏拖拽；
- resize 到 1366×768 可操作。

## 7. Three.js 验收

自动测试主要测数学和状态，人工 smoke test 测 WebGL：

- 场景可加载；
- GLB 不存在时 fallback 可用；
- Shelf/Slot/Parcel 数量与 API 数据一致；
- 点击 Parcel 返回正确 ID；
- `focusParcel` 到正确 Shelf；
- 相机正面方向正确；
- 右侧详情与 Parcel 一致；
- 3D drag 只能落 Slot；
- 目标占用显示失败；
- API 失败后回滚；
- outbound 后 Parcel 从 scene 移除；
- 页面退出后 RAF/listener 清理。

## 8. 性能 Smoke

准备可重复生成的 50/500/1000 Parcel mock scene（仅性能工具，不替代真实业务 API）。记录：

- 首屏 scene build 时间；
- 简单旋转/缩放是否可用；
- 选中响应；
- 500/1000 时是否需要 InstancedMesh。

不得把性能 mock 数据接到最终业务页面冒充数据库数据。

## 9. 安全/权限基础检查

- 密码不返回前端；
- Session token 不写日志；
- STAFF 无法调用 admin handler；
- API 错误不返回数据库密码/stack trace；
- application.properties 不提交；
- CORS 仅开发环境允许已知本地 origin。

## 10. 每 Phase 的证据

创建：

```text
development-log/v2/PHASE_0.md
...
development-log/v2/PHASE_7.md
```

必须记录：

- 本阶段目标；
- 实际修改文件；
- 关键设计决策；
- 运行命令；
- 测试数量/结果；
- 未通过项；
- 阻塞；
- 下一阶段输入。

## 11. 最终验收链路

必须用真实 API/数据库完成：

```text
登录
→ Dashboard
→ 新增/选择客户
→ 入库
→ 待上架区出现
→ 2D 拖到 Slot A
→ DB 确认
→ 3D 出现
→ 点击/搜索
→ Camera 定位货架正面
→ 详情正确
→ 3D 拖到 Slot B
→ relocation/event/log 正确
→ 出库
→ 2D/3D 消失
→ shelf utilization 变化
→ Dashboard 更新
→ 异常流程
```

## 12. 最终质量门

只有全部满足才可宣布 V2 完成：

- `TASKS_V2.md` 所有 Phase 验收真实通过；
- `mvn clean test` 成功；
- 真实 MySQL `mvn -Pintegration-test verify` 成功；
- `npm run type-check` 成功；
- `npm run test` 成功；
- `npm run build` 成功；
- Chrome/Edge 桌面人工全链路通过；
- 2D 与 3D 状态一致；
- relocate 冲突和 rollback 已验证；
- 无 SpringBoot/ORM 违规依赖；
- Swing legacy 仍可编译；
- 最终 README、V2 技术点、演示流程和已知限制已更新。

若真实 MySQL、浏览器或 GLB 资产环境缺失，应把对应 Phase 保持 IN_PROGRESS，并明确人工步骤；禁止虚假标 DONE。