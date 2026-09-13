# V2 UI / UX 设计规范

## 1. 设计方向

目标不是传统白底 CRUD 管理台，而是工业数字孪生驾驶舱。视觉参考智慧仓储大屏：深色背景、清晰网格、克制发光、蓝青信息色、橙色选中、红色异常。

必须优先保证：信息层级、可读性、业务反馈和交互可预测性。不得为了“炫”而牺牲表格可读性或拖拽准确性。

## 2. 桌面布局

主要适配：1920×1080、1600×900、1440×900、1366×768。

推荐全局结构：

```text
TopBar: Logo | Dashboard | Warehouse | Digital Twin | Parcels | Exceptions | Analytics | Settings | User

Content
├─ Page header / filters
└─ Main area
```

1024 以下可隐藏非关键侧栏或关闭 3D 拖拽，只保留查询与查看；手机端不是本项目核心目标。

## 3. Dashboard

推荐三段式：

```text
┌───────────────┬────────────────────────────┬──────────────┐
│ KPI /利用率    │ 趋势与仓库概览             │ 分布/异常     │
├───────────────┴────────────────────────────┴──────────────┤
│ 货架利用率 / 入出库趋势 / 异常与滞留 / 最近操作           │
└───────────────────────────────────────────────────────────┘
```

第一屏必须看到：今日入库、今日出库、库存、异常、滞留、仓位利用率。

ECharts 图表：

- 入库/出库趋势；
- 当前库存趋势；
- 快递公司占比；
- 异常类型；
- 货架利用率；
- 停留时长分布。

不允许所有图表都使用夸张渐变、发光或 3D 饼图。

## 4. Warehouse 作业中心

这是高频操作页，不应设计成纯大屏。

```text
┌──────────────┬──────────────────────────────┬────────────────┐
│ 待上架/搜索   │ 2D Warehouse Layout          │ 快件详情        │
│ Parcel cards │ Shelf + Slot grid            │ tracking       │
│ filters      │ drag/drop                    │ customer       │
│              │ zoom/pan/filter              │ status/events  │
└──────────────┴──────────────────────────────┴────────────────┘
```

### 2D Slot 状态

- 空闲：低亮灰蓝；
- 正常占用：蓝；
- 选中：橙；
- 拖拽可放：绿；
- 不可放/冲突：红；
- 滞留：黄；
- 异常：红并带图标；
- disabled：灰且不可 drop。

必须给拖拽提供非拖拽替代：选中 Parcel → “移动到...” → 选择 Shelf/Slot → 确认。

## 5. Digital Twin

建议布局：

```text
┌──────────────────────────────────────────────────────────┐
│ Search | 状态筛选 | 顶视 | 正视 | 重置 | 图例            │
├──────────────────────────────────────┬───────────────────┤
│                                      │ Selected Parcel   │
│          Three.js Scene              │ tracking          │
│                                      │ customer          │
│                                      │ shelf / slot      │
│                                      │ dwell / status    │
│                                      │ events/actions    │
└──────────────────────────────────────┴───────────────────┘
```

场景默认以 30–45° 俯视角展示整个驿站。用户选择 Parcel 后进入 focus 模式，但必须允许“返回全景”。

## 6. 快件详情

至少展示：

- trackingNo；
- courierCompany；
- customer name；
- masked mobile；
- status；
- shelfCode；
- slotCode；
- arrivedAt；
- dwellTime；
- operator；
- remark；
- exception；
- ParcelEvent timeline；
- ParcelRelocation history。

详情操作根据状态显示：定位、移动、出库、登记异常、处理异常。

## 7. 交互反馈

### Drag start
原位置降低透明度，目标 Slot 开始显示可放状态。

### Hover/drag over
可放绿色，不可放红色；必须有文字/图标反馈，不能只依赖颜色。

### Drop pending
目标显示短暂 loading，不立即永久改变位置。

### Success
位置确认、Toast 成功、相关统计刷新。

### Failure
对象平滑回原位置，并显示后端 message；409 时提示“仓位状态已变化，已刷新”。

## 8. 动效

动效服务于状态变化：

- Camera focus 600–1000ms；
- drag rollback 200–400ms；
- 面板切换 120–250ms；
- KPI 数字不需要持续滚动；
- 不使用无限呼吸、粒子雨等与业务无关动画。

尊重 `prefers-reduced-motion`，此时相机可以缩短动画但功能不变。

## 9. 可访问性

- 所有主要操作必须能通过按钮完成，不依赖 hover；
- 拖拽必须有非拖拽替代；
- 不仅靠颜色表达异常；
- input/button 保持清晰 focus；
- 重要错误用 aria-live/可见消息；
- 表格和详情保持足够对比度。

## 10. 不允许的实现

- 直接套 Element Plus 默认 admin template 作为最终效果；
- 大面积纯装饰 HUD 边框影响阅读；
- 关键数据使用极小字号；
- 3D 场景遮住所有操作；
- 只有 3D 没有可操作的 2D/列表 fallback；
- 业务状态变化后必须手动刷新页面才能看到结果。