# V2.1 Phase A — Warehouse Drag UX

## 完成内容

- 将二维仓库三栏区改为基于可视高度的稳定工作区，中间货架画布独立纵向滚动。
- 新增拖拽边缘自动滚动控制器：顶部/底部 60px 区域内按距离线性提速，单帧速度上限 22px。
- 在离开边缘、drop、dragend、Escape、窗口失焦和组件卸载时停止 RAF 并清理拖拽状态。
- 待上架快件和已上架快件均可作为拖拽源，保留右侧“移动到…”无障碍替代路径。
- 保留 WarehouseStore 的乐观预览、服务端提交和 409 回滚刷新机制。

## 新增/修改文件

- `frontend/src/features/warehouseDragScroll.ts`
- `frontend/src/features/warehouseDragScroll.test.ts`
- `frontend/src/views/WarehouseView.vue`
- `frontend/src/styles/main.css`
- `TASKS_V2_1.md`

## 执行命令与测试结果

- `npm run type-check`：通过。
- `npm test -- --run`：8 个测试文件、22 个测试全部通过。
- `npm run build`：通过；仅保留既有 Three.js 大 chunk 警告。
- `mvn clean test`：37 个 Java 测试全部通过。

## 浏览器验收

- 使用本地 MySQL、真实 Java API 和 Vite 页面，在 1366×768 视口检查。
- 中央画布出现独立滚动条，可从 A-01 滚动到屏外的 C-01 最底层仓位。
- 将待上架运单 `ACCEPT20260914` 移至最下方 `C-01-04-05`，页面提示成功。
- 刷新页面后 `C-01-04-05` 仍显示该运单，证明服务端持久化成功。
- 当前浏览器自动化接口不能稳定合成长时间 HTML5 drag hover；边缘方向、比例速度、最大速度与 RAF cleanup 由纯控制器单测覆盖，真实数据库写入使用同页无障碍替代操作验收。

## 发现的问题与修复

- 初版 520px 最小高度在 768px 视口会使工作区超出视口；调整为 `clamp(360px, calc(100vh - 350px), 720px)`。
- Vue 模板不能直接安全推断非响应式控制器变量；增加明确的 `stopAutoScroll` 事件函数。

## 剩余风险 / 对下一阶段影响

- HTML5 Drag & Drop 在不同浏览器的事件频率存在差异，但滚动控制器不依赖固定频率并设置速度上限。
- Phase B 扩展到八组货架后，中间独立滚动区可直接承载更长的真实仓位列表。
