# Codex V2.1.2 — 3D Interaction Hotfix Prompt

你现在负责 ParcelStationX 的一个**高优先级 3D 交互正确性热修复**。

这不是新功能开发。先把现有 3D 数字孪生的“快件镜头定位 + Hover 信息卡片”修正确，再继续其它 V2.2/V2.3 工作。

## 0. 仓库与分支

仓库：

```text
https://github.com/rallfd3-max/ParcelStationX
```

基线分支：

```text
codex/visualization-v2
```

如果你当前就在该分支并且存在未提交的 V2.2 工作：

- 先 `git status`；
- 不要 `reset --hard`；
- 不要丢弃本地修改；
- 可以先安全 commit 当前工作，或创建短期分支 `codex/3d-interaction-hotfix` 再实施；
- 最终必须让热修复 commit 可被 `codex/visualization-v2` 和后续 `codex/dynamic-shelf-agent` 集成。

禁止：

- merge main；
- 修改 main；
- 为修相机重写整个 Three.js 架构；
- 用“调大/调小一个常数”糊弄问题；
- 只跑单测不做可用浏览器验收（如果浏览器环境可用）。

## 1. 必读

完整读取：

```text
docs/v2/V2_1_2_3D_INTERACTION_HOTFIX.md
TASKS_V2_1_2.md
frontend/src/three/sceneMath.ts
frontend/src/three/CameraController.ts
frontend/src/three/parcelFocus.ts
frontend/src/three/HoverController.ts
frontend/src/three/SceneIndex.ts
frontend/src/three/DragController.ts
frontend/src/three/ParcelRenderer.ts
frontend/src/three/WarehouseScene.ts
frontend/src/views/DigitalTwinView.vue
frontend/src/styles/main.css
```

再审计现有 Three.js 相关 tests。

## 2. 用户真实复现

用户已在真实页面测试到：

1. 点击某个 3D 快件后，相机没有真正到达这个快件正前方；
2. 点击第一排货架上的快件时，镜头会出现明显的翻转/跑到错误一侧；
3. 鼠标悬停在快件上时，快件旁边不显示信息；
4. 点击以后右侧详情可以显示，说明 Parcel 选择链本身并非完全失效。

不要把这解释成“用户操作问题”。这是当前实现需要修复的交互 BUG。

# H1 — Camera Front Focus

## 3. 保留真实 Parcel Mesh target

当前 `parcelFocus.ts` 已使用：

```text
Object3D.getWorldPosition()
```

这一点是对的，必须保留。

focus 最终 target 必须是：

```text
被点击 Parcel Mesh 的真实 world position
```

不是 slot 中心的重新估算值，也不是 shelf center。

验收：

```text
controls.target ~= parcelObject.getWorldPosition()
```

## 4. 修复 front/back 逻辑

当前 `parcelCameraView()` 有：

```text
front
front + right
front + left
-back / opposite side
```

并会因为其它货架 AABB 遮挡而选择最后的 opposite/back candidate。

这会导致第一排快件被从错误一侧观察，用户看到“视角翻转”。

修复要求：

- **删除 parcel focus 的自动 back-side fallback**；
- final camera 必须保持在当前货架的 logical front hemisphere；
- 对所有 rotation：0、PI、PI/2、-PI/2，都满足：

```text
dot(cameraPosition - target, shelfFront(layout)) > 0
```

如果正前方空间不足：

1. 优先缩短 camera distance；
2. 其次在 front hemisphere 内做有限侧移；
3. 绝不自动跳去背面。

## 5. 使用 aisle-aware distance

不要继续用可能穿过另一排货架的固定大距离：

```text
max(width,height) * 1.65
```

设计确定性算法：

- preferred distance 大约在 1.8m~3.0m 的可视范围内，具体值可由 shelf dimensions 算；
- 沿 shelf front 方向检测最近其它 layout；
- 留出 clearance margin；
- 若前方货架较近，就缩短距离；
- 保证 camera 仍能看到目标快件和周围 slot context；
- 不得因为 obstruction 就反向。

可继续使用 layout bounds，但算法必须明确可靠并有测试。

## 6. 修复动画“穿 target”导致的翻转

当前 `CameraController.animate()` 是：

```text
camera.position linear lerp
controls.target linear lerp
```

这在前后排、相反朝向之间切换时可能使 camera-target 向量缩到接近 0 或穿过 target，从而产生视觉翻转。

改为 target-relative flip-safe animation。

推荐：

1. currentTarget = controls.target；
2. currentOffset = camera.position - currentTarget；
3. desiredTarget = parcel world target；
4. desiredOffset = desiredCameraPosition - desiredTarget；
5. 用 spherical/arc interpolation 处理 offset：
   - shortest azimuth around world Y；
   - interpolate radius；
   - interpolate elevation/vertical component；
   - radius 永远 >= minimumFocusRadius；
6. target 本身 ease interpolation；
7. 每帧重建：

```text
camera.position = interpolatedTarget + interpolatedOffset
```

8. 始终：

```text
camera.up.set(0,1,0)
```

9. `controls.update()`。

等价方案可以接受，但必须通过测试证明：

- camera 永远不会穿 target；
- 不 roll；
- 从前排切后排不会突然翻转。

## 7. 相机测试

必须新增/加强：

```text
front row left
front row center
front row right
rear row left
rear row center
rear row right
```

覆盖：

- first column；
- last column；
- rotation 0；
- rotation PI；
- rotation PI/2；
- rotation -PI/2。

断言：

```text
focus target == real parcel mesh world position
dot(finalCamera-target, shelfFront) > 0
camera-target distance >= minimum radius
camera.up remains world-up
```

还要做 front row -> rear row -> front row 往返测试。

# H2 — Hover Tooltip

## 8. 不允许网络请求阻塞 tooltip 出现

当前 DigitalTwinView 的模板是：

```text
hover && hoverDetail
```

而 `hoverDetail` 来自异步 `/api/parcels/{id}/details`。

这意味着 tooltip 是否出现取决于网络请求，这是错误设计。

改为：

```text
Hover hit
-> 立刻从 WarehouseSnapshot 构造 local summary
-> 立刻显示 tooltip
-> 如果需要 customer masked data，再异步补充
```

用户鼠标刚进入快件区域就必须看到信息卡，不需要等 API。

## 9. Hover summary

至少即时显示：

```text
trackingNo
courierCompany
status
slotCode / 入库暂存区
arrivedAt
dwell time
```

如果 customer name / maskedMobile 不在 snapshot：

- tooltip 先正常出现；
- detail fetch 完成后补充；
- fetch 失败也不能把 tooltip 隐藏。

## 10. 禁止 pointermove 请求风暴

同一个 parcelId 上移动鼠标时：

```text
不要每个 pointermove 都调用 details API
```

实现：

- currentHoveredParcelId；
- detail cache Map<parcelId, detail>；
- 只有 parcelId 变化且缓存没有数据时才 fetch；
- in-flight request 去重；
- mouse move 只更新 tooltip anchor。

## 11. Tooltip 必须真正贴近快件

不要只用：

```text
event.clientX / event.clientY
```

作为最终 tooltip 定位。

使用：

```text
hovered Parcel Object3D world position
-> Vector3.project(camera)
-> NDC
-> twin-canvas local pixels
```

然后在 projected parcel point 旁边 offset 12~18px。

必须 clamp 在 canvas 范围内，不能超出右边/下边被裁掉。

如果相机在 tooltip 显示期间移动，tooltip anchor 需要同步刷新，使标签继续跟着 parcel。

可以：

- 由 WarehouseScene render loop 更新当前 hovered projection；
- 或 HoverController 暴露稳定 projection callback。

不要为了实现 tooltip 重建整个 scene。

## 12. Hover 与 Drag

允许：

```text
真正拖动时隐藏 tooltip
```

但是普通 hover 必须工作。

pointerdown 之后如果没有形成实际 drag，不应该造成 tooltip 永久消失。

确认 DragController 当前“pointerdown 即进入 dragging phase”的行为是否干扰普通点击/hover；如有必要增加 drag threshold（例如鼠标移动超过几个像素才视为拖拽），以区分：

```text
click/select
vs
actual drag
```

这会同时改善点击与 hover 稳定性。

# H3 — Regression

## 13. Frontend quality gates

执行：

```text
cd frontend
npm run type-check
npm run test
npm run build
```

失败必须修复，不得删除测试。

然后：

```text
cd ..
mvn clean test
```

## 14. Real browser acceptance

如果有 localhost/browser control，必须真实启动并验证。

至少测试 6 个代表快件：

```text
第一排：左 / 中 / 右
第二排：左 / 中 / 右
```

再包含：

```text
第一列
最后一列
```

每个快件都验证：

1. hover 时快件旁边立即出现 tooltip；
2. tooltip trackingNo / courier / status / slot 正确；
3. 点击后右侧详情是同一 Parcel；
4. 点击“镜头定位”后，相机在当前货架正面；
5. 目标快件处于视野中心附近；
6. 不会跳到另一排前面；
7. 不会翻转；
8. 不需要 F5。

前排 -> 后排 -> 前排至少往返 10 次。

检查 Console：

- 无 uncaught error；
- 无 Three.js warning；
- 无重复监听器；
- 无大量重复 details 网络请求。

## 15. 文档与提交

每个 Phase 完成：

```text
H1 -> development-log/v2_1_2/PHASE_H1.md
H2 -> development-log/v2_1_2/PHASE_H2.md
H3 -> development-log/v2_1_2/PHASE_H3.md
```

更新：

```text
TASKS_V2_1_2.md
```

建议提交：

```text
v2.1.2-h1: fix front-side parcel camera focus
v2.1.2-h2: make parcel hover tooltip immediate and anchored
v2.1.2-h3: complete 3d interaction browser regression
```

push 当前工作分支。

完成后汇报：

- 根因；
- 修改文件；
- 相机算法变化；
- hover 数据流变化；
- 自动测试结果；
- 浏览器测试结果；
- commit SHA。

不要只说“已修复”。
