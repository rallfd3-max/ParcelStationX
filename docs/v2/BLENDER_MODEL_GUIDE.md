# ParcelStationX Warehouse GLB 制作指南

模型导出为 `frontend/public/models/warehouse.glb`；文件缺失或加载失败时自动使用 primitive 环境。

- 单位为米、Y 轴向上，原点为仓库地面中心，正面朝 +Z；建议环境约 18m × 12m。
- 固定物命名 `ENV_Wall_*`、`ENV_Door_*`、`ENV_Counter_*`、`ENV_Light_*`。
- 只建模地面、墙、门、服务台、灯具和装饰。Shelf、Slot、Parcel 必须由数据库和 Three.js 动态生成。
- 应用 transform，删除不可见面并控制倒角；PBR 纹理优先 1024px、最大 2048px。
- 可烘焙环境 AO，但避免导出干扰动态对象的强灯光。
- 导出 glTF Binary，启用 Selected Objects、Y Up、Apply Modifiers；无必要时关闭动画。
- 分别验证 GLB 成功和删除 GLB 后 fallback，两者不得改变业务货架坐标。
