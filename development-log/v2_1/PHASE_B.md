# V2.1 Phase B — Expanded Warehouse & 3D Scene

## 完成内容

- 新增可重复执行的前向迁移 `V2_1_0__expand_demo_warehouse.sql`。
- 将真实业务数据扩展为 A/B/C/D 四区、八组货架，每组 30 个仓位，共 240 个真实 ShelfSlot。
- 迁移通过 shelf code/grid 唯一约束增量补齐数据，不删除或重建任何快件。
- 八组 ShelfLayout 以两排行列/中间通道布局，前后排朝向相反。
- 3D 场景新增入库暂存区和取件/出库区。
- 总览、正视、顶视相机改为根据全部 ShelfLayout 的旋转后边界动态构图。

## 新增/修改文件

- `src/main/resources/db/migration/V2_1_0__expand_demo_warehouse.sql`
- `src/test/java/com/parcelstationx/integration/MySqlConnectionIT.java`
- `frontend/src/three/OperationalAreas.ts`
- `frontend/src/three/sceneBounds.ts`
- `frontend/src/three/sceneBounds.test.ts`
- `frontend/src/three/CameraController.ts`
- `frontend/src/three/WarehouseScene.ts`
- `TASKS_V2_1.md`

## 执行命令与测试结果

- V2.1 SQL 迁移在本地 MySQL 连续执行两次：均成功。
- 数据核验：8 个目标货架、总容量 240、240 个仓位、52 个实际占用、0 个无效 slot 引用。
- `npm run type-check`：通过。
- `npm test -- --run`：9 个测试文件、23 个测试全部通过。
- `npm run build`：通过；仅保留既有 Three.js chunk 体积警告。
- 带真实 `PARCEL_DB_*` 环境的 `mvn -Pintegration-test verify`：37 个单元测试和 2 个 MySQL 集成测试全部通过，无跳过。

## 发现的问题与修复

- PowerShell 管道向 MySQL 传递中文 SQL 时发生编码错误，改用 MySQL 进程的 UTF-8 文件标准输入执行迁移。
- 旧集成测试写死三组货架，扩容后失败；改为明确验证 V2.1 八组货架和至少 240 个仓位，同时逐架核对 occupied 与实际快件数。

## 浏览器检查与剩余风险

- 数字孪生页面可正常加载，控制台无错误；当前内置浏览器截图未呈现 WebGL 像素内容，因此几何覆盖主要由真实 API 数据、场景边界单测和无控制台错误共同验证。
- Phase C 将在此真实八架布局上加入暂存快件、hover tooltip 和出入库交互，并再次执行浏览器验收。
