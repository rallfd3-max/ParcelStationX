# V2 Three.js 数字孪生规范

## 1. 建模策略

采用“Blender 环境 + Three.js 动态业务对象”的混合方案。

Blender/GLB 负责：地面、墙体、门、服务台、固定设备、灯具和装饰。

Three.js 负责：Shelf、Slot、Parcel、状态标识、选中高亮、拖拽预览和业务标签。

这样数据库结构变化时无需重新制作 GLB。

## 2. 场景结构

```text
WarehouseScene
├─ EnvironmentRoot
├─ ShelfRoot
│  ├─ Shelf A01
│  │  └─ Slot objects
│  └─ Shelf ...
├─ ParcelRoot
├─ InteractionOverlay
└─ Lights
```

必须建立 SceneIndex：

- `parcelId -> parcel visual/instance`；
- `slotId -> slot visual`；
- `shelfId -> shelf visual`。

任何业务对象都必须可从数据库 ID 定位到 3D 对象。

## 3. 坐标系统

Parcel 不存 world position。

ShelfLayout 保存货架 transform：positionX/Y/Z、rotationY、width/height/depth、columns、levels。

Slot 保存 shelfId、levelIndex、columnIndex。

前端使用纯函数：

```text
worldPosition = shelfTransform + slotLocalOffset(level,column)
```

必须为此写单元测试，避免视觉与数据库映射错位。

## 4. 货架生成

ShelfBuilder 根据 layout 动态生成立柱、层板、边框和 Slot anchor。几何体尽量复用。

Slot 不一定都渲染实体盒子，可使用透明交互平面/Box3 作为 Raycaster target。

## 5. Parcel 渲染

正常 Parcel 建议使用 InstancedMesh；如果为了实现初版拖拽需要先用独立低面数 Mesh，也允许，但 Phase 7 前必须完成性能评估。

状态视觉：

- NORMAL/IN_STOCK：蓝/青；
- selected：橙；
- OVERDUE：黄；
- EXCEPTION：红；
- drag preview：半透明；
- outbound：从 scene 移除。

## 6. 交互

### 点击

Raycaster 命中 Parcel 后：

1. SceneIndex 取 parcelId；
2. Pinia 设置 selectedParcelId；
3. 请求/读取详情；
4. 高亮；
5. 打开详情面板；
6. 可选调用 `focusParcel`。

点击 Shelf/Slot 时展示其占用和元数据。

### Orbit

使用 OrbitControls。支持旋转、缩放和平移；focus 动画期间临时限制用户操作，结束后恢复。

## 7. CameraController

至少提供：

```text
focusParcel(parcelId)
focusShelf(shelfId)
focusSlot(slotId)
resetCamera()
topView()
frontView()
cancelAnimation()
```

`focusParcel`：

1. 找到 parcel -> slot -> shelf；
2. 计算 slot world center；
3. 计算 shelf front direction；
4. 根据 shelf 尺寸计算 camera distance；
5. 插值 camera position 和 controls.target；
6. 600–1000ms easing；
7. 完成后允许 OrbitControls。

不得写死某个 A01 货架的位置。

## 8. 3D Drag / Snap

流程：

```text
pointerdown parcel
→ enter drag state
→ pointermove projected preview
→ raycast slot targets
→ validate local visual availability
→ green/red target feedback
→ pointerup
→ if no target: rollback
→ if target: snap preview
→ POST relocate
→ success: commit scene/store
→ failure: animate rollback and refresh relevant data
```

关键规则：

- Parcel 不允许停在任意 world xyz；
- Drop 只能落到有效 Slot；
- 目标占用时不得提交；
- 服务端才是最终真相；
- 409 冲突必须回滚；
- DragController 不直接写数据库/API，调用 store/action。

## 9. Blender GLB

默认路径：`frontend/public/models/warehouse.glb`。

如果 GLB 不存在或加载失败：必须显示 Three.js primitive fallback 环境，核心业务仍可演示。

另生成 `docs/v2/BLENDER_MODEL_GUIDE.md`（在真正进入 Three.js Phase 时创建），说明：

- 建议真实比例；
- 原点和坐标轴；
- mesh 命名；
- material 简化；
- 灯光烘焙建议；
- glTF/GLB export；
- 纹理尺寸；
- 禁止把 Parcel/Shelf 业务对象烘焙进 GLB。

## 10. 资源释放

页面卸载必须：

- cancelAnimationFrame；
- controls.dispose；
- renderer.dispose；
- remove pointer/resize listeners；
- dispose 本页面创建且不共享的 geometry/material/texture；
- 清空 SceneIndex。

重复进入/退出 DigitalTwin 页面 10 次不得出现明显持续内存增长或重复事件触发。

## 11. 性能验收

- 50 parcel：正常演示无明显卡顿；
- 500 parcel：可交互；
- 1000 parcel：作为压力测试记录帧率/交互情况，不要求高端游戏帧率；
- render loop 不得创建大量临时 Mesh/Material；
- resize 不得频繁重建场景。

## 12. 3D Phase 的最小完成标准

只有同时达到以下条件才可标 DONE：

- 数据库 Shelf/Slot/Parcel 可以映射到场景；
- 点击任意 Parcel 能得到正确 parcelId；
- 搜索 Parcel 能定位正确货架；
- Camera 自动移动到货架正面；
- 右侧详情与选中对象一致；
- 3D drag 能提交真实 relocate；
- 失败能 rollback；
- 出库后对象消失；
- 页面卸载无重复 RAF/listener。