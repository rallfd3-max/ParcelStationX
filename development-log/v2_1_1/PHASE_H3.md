# Phase H3 — Browser Regression

## 完成内容

- 在 Codex 应用内浏览器的新会话中登录真实本地 Java API / MySQL 演示环境。
- 首页确认 ECharts 实际类型为 `line`、`donut`、`pie`、`pie`、`bar`、`bar`、`bar`。
- 数字孪生页确认 WebGL 画布正常渲染，画布客户端尺寸为 907 × 538。
- 搜索并定位六个代表性快件，覆盖前排、后排、左端、右端、首列与末列。
- 使用场景内实际点击验证 Raycaster 选择、详情面板与相机聚焦共享同一 parcelId 链路。
- 在全新浏览器会话中检查首页和数字孪生页控制台，均无 error。

## 浏览器验收记录

| 运单号 | 仓位 | 覆盖位置 | 结果 |
| --- | --- | --- | --- |
| `DEMO000001` | `A-01-01-01` | 前排、首列、左端 | 通过 |
| `DEMO000006` | `A-01-01-06` | 前排、末列、右端 | 通过 |
| `DEMO000031` | `B-01-01-01` | 另一前排、首列 | 通过 |
| `DEMO000036` | `B-01-01-06` | 另一前排、末列 | 通过 |
| `ACCEPT20260913` | `C-01-02-03` | 后排、左侧 | 通过 |
| `ACCEPT20260914` | `C-01-04-05` | 后排、右侧 | 通过 |

在 3D 场景中直接点击青色快件后，右侧详情从 `ACCEPT20260914 / C-01-04-05` 切换为 `ACCEPT20260913 / C-01-02-03`，证明点击目标、详情内容与相机聚焦使用同一快件映射。

## 新增/修改文件

- `TASKS_V2_1_1.md`
- `docs/v2/V2_TEST_REPORT.md`
- `development-log/v2_1_1/PHASE_H3.md`

## 执行命令与结果

- `mvn clean test`：37 tests，0 failures，0 errors，成功。
- `npm run type-check`：成功。
- `npm test -- --run`：12 test files、30 tests，全部通过。
- `npm run build`：成功；仅保留 Vite 大 chunk 提示，不影响构建。

## 发现的问题与修复

- 首次把前端门禁命令误从仓库根目录执行，npm 因根目录无对应脚本而退出；切换到 `frontend` 后重新执行，三项门禁全部通过，未产生代码变更。

## 剩余风险

- 当前可用的真实浏览器控制端为 Codex 应用内浏览器，没有可用 Chrome/Edge 控制端，因此原 `TASKS_V2.md` Phase 8 继续如实保持 `IN_PROGRESS`。
- Vite 报告 Dashboard 与 Digital Twin chunk 超过 500 kB；这是性能优化提示，不影响本次功能验收。

## 对下一阶段的影响

V2.1.1 H1–H3 已闭环。后续若继续原 V2 Phase 8，只需在 Chrome/Edge 可用时执行完整演示脚本，不应重新打开本次图表或相机热修复范围。
