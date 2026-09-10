# 项目计划书

## 一、项目基本信息

### 1. 项目名称

**ParcelStationX — 社区快递驿站综合管理系统**

### 2. 项目类型

Java 桌面管理系统 / 数据库应用 / 课程综合实践项目。

### 3. 建设目标

开发一套无需大型业务框架、能够在普通 Windows 电脑上运行的快递驿站管理系统，用于完成快件从到站入库到用户取件出库的完整生命周期管理。

系统既要满足真实驿站业务中常见的“入库、库存、货架定位、取件、异常、通知、统计”需求，又要突出 Java 课程中的基础技术使用，使每个技术点都能在答辩中解释。

---

## 二、项目背景

社区快递驿站的日常核心工作并不只是记录快递单号，而是维护一条完整业务链：

1. 快件到站；
2. 识别收件人；
3. 登记快递公司和运单；
4. 分配货架；
5. 生成取件码；
6. 通知客户；
7. 客户到店查件；
8. 校验取件信息；
9. 完成出库；
10. 处理破损、错分、拒收、长期滞留等异常；
11. 留存操作记录；
12. 汇总每天业务数据。

传统的手工登记或 Excel 方式难以处理状态一致性、重复运单、货架定位、责任追溯和实时库存，因此适合作为 Java 数据库管理系统课程项目。

---

## 三、课程约束转化

本项目必须严格遵守课程要求：

### 1. 不使用现成业务框架

不使用 SpringBoot、Spring、Struts、Hibernate、MyBatis、SSH、SSM、JPA 等。

采用：

- Swing：桌面 UI；
- JDBC：数据库访问；
- Java 标准库：线程、序列化、集合、泛型；
- MySQL：关系数据库。

### 2. 选取 5 项课程技术

#### 集合

使用场景：

- 通知任务 Queue；
- 页面查询 List；
- 统计 Map；
- 货架占用缓存 Map。

#### 泛型

使用场景：

- `BaseDao<T, ID>`
- `Result<T>`
- `PageResult<T>`

#### 序列化

使用场景：

- 备份元数据；
- 本地系统快照；
- 恢复校验数据。

#### 多线程

使用场景：

- 模拟通知异步发送；
- 后台统计；
- 备份任务；
- SwingWorker 防止 UI 卡死。

#### 数据库编程

使用场景：

- JDBC；
- PreparedStatement；
- Transaction；
- ResultSet；
- MySQL。

---

## 四、用户角色

### 1. 管理员

权限：

- 员工管理；
- 客户管理；
- 快件全部操作；
- 货架管理；
- 异常处理；
- 统计；
- 日志；
- 备份恢复；
- 系统配置。

### 2. 驿站员工

权限：

- 快件入库；
- 查件；
- 出库；
- 异常登记；
- 客户查询；
- 查看自己相关操作。

不允许：

- 删除管理员；
- 恢复数据库；
- 修改关键系统配置。

---

## 五、业务模块

### 模块 A：登录与权限

功能：

- 用户名密码登录；
- 密码校验；
- 角色权限；
- 登录日志；
- 连续错误提示。

课程展示点：

- String；
- DAO；
- Service；
- JDBC。

### 模块 B：客户管理

字段建议：

- customer_id
- name
- mobile
- building
- room
- remark
- created_at

功能：

- 新增；
- 修改；
- 查询；
- 手机号检索；
- 查看客户未取快件。

### 模块 C：货架管理

字段：

- shelf_id
- shelf_code
- zone
- capacity
- occupied
- status

功能：

- 新增货架；
- 修改容量；
- 启用/停用；
- 占用率；
- 推荐可用货架。

### 模块 D：快件入库

字段：

- parcel_id
- tracking_no
- courier_company
- customer_id
- shelf_id
- pickup_code
- status
- arrived_at
- operator_id

流程：

1. 输入运单号；
2. 检查重复；
3. 选择/匹配客户；
4. 选择快递公司；
5. 推荐货架；
6. 生成取件码；
7. 数据库事务保存；
8. 写 ParcelEvent；
9. 创建通知任务；
10. UI 显示成功信息。

### 模块 E：库存与查件

支持：

- 运单号；
- 手机号；
- 姓名；
- 取件码；
- 货架号；
- 快递公司；
- 状态；
- 日期范围。

### 模块 F：快件出库

流程：

1. 输入手机号/取件码/运单；
2. 查找候选快件；
3. 选择目标；
4. 校验状态；
5. 确认领取；
6. 事务更新为 PICKED_UP；
7. 释放货架占用；
8. 写事件；
9. 写操作日志。

### 模块 G：异常件

类型：

- DAMAGED：破损
- WRONG_SORT：错分
- REFUSED：拒收
- LOST_SUSPECTED：疑似丢失
- OVERDUE：长期滞留
- OTHER：其他

功能：

- 登记；
- 处理；
- 处理说明；
- 状态跟踪；
- 查询；
- 关闭异常。

### 模块 H：通知管理

课程版采用“模拟通知”，不接真实短信平台。

状态：

- PENDING
- SENDING
- SUCCESS
- FAILED

功能：

- 新快件通知；
- 滞留提醒；
- 异常提醒；
- 通知队列；
- 失败重试；
- 通知记录。

### 模块 I：数据统计

指标：

- 今日入库；
- 今日出库；
- 当前库存；
- 异常件；
- 超期未取；
- 平均滞留时间；
- 快递公司件量；
- 货架占用率；
- 员工操作量。

展示：

- 数字卡片；
- JTable；
- Java2D 柱状图。

### 模块 J：备份与恢复

实现：

- 备份信息对象实现 Serializable；
- 后台生成备份文件；
- 保存时间、版本、数据量、校验值；
- 恢复前确认；
- 记录恢复日志。

---

## 六、核心业务规则

### 1. 运单唯一

同一 `tracking_no` 不允许存在两条有效入库记录。

### 2. 状态机

建议：

```text
IN_STOCK
  ├──> PICKED_UP
  ├──> EXCEPTION
  └──> RETURNED

EXCEPTION
  ├──> IN_STOCK
  └──> RETURNED
```

禁止：

- PICKED_UP 再次出库；
- RETURNED 再取件；
- 停用货架继续分配新包裹。

### 3. 货架容量

`occupied <= capacity`

入库成功后 occupied + 1。

出库/退回后 occupied - 1。

### 4. 取件码

建议生成 6 位数字或“区域 + 数字”形式。

要求：

- 在当前在库快件范围内避免冲突；
- 生成逻辑单独封装；
- 可测试。

### 5. 操作日志

关键操作必须留痕：

- 登录；
- 入库；
- 出库；
- 修改快件；
- 异常登记；
- 异常关闭；
- 用户管理；
- 备份；
- 恢复。

---

## 七、界面规划

主窗口：

```text
┌────────────────────────────────────────────┐
│ ParcelStationX      当前用户：xxx  [退出] │
├──────────┬─────────────────────────────────┤
│ 首页     │                                 │
│ 快件入库 │          主工作区               │
│ 在库快件 │                                 │
│ 快件出库 │                                 │
│ 异常件   │                                 │
│ 客户管理 │                                 │
│ 货架管理 │                                 │
│ 员工管理 │                                 │
│ 统计报表 │                                 │
│ 操作日志 │                                 │
│ 备份恢复 │                                 │
└──────────┴─────────────────────────────────┘
```

UI 风格：

- 左侧导航；
- 顶部用户栏；
- 主区域 CardLayout；
- 表格统一；
- 表单统一；
- 状态颜色统一；
- 不追求复杂动画；
- 重点保证操作流畅和答辩稳定。

---

## 八、数据架构

数据表初步划分：

1. users
2. customers
3. shelves
4. parcels
5. parcel_events
6. exception_records
7. notification_records
8. operation_logs

详见 `05_DATABASE_DESIGN.md`。

---

## 九、项目架构

采用传统分层：

```text
UI
 ↓
Service
 ↓
DAO
 ↓
JDBC
 ↓
MySQL
```

### UI

只处理显示、用户输入、事件监听。

### Service

负责：

- 校验；
- 业务规则；
- 状态机；
- 多 DAO 事务；
- 任务提交。

### DAO

负责：

- SQL；
- PreparedStatement；
- ResultSet 映射。

---

## 十、开发周期

建议按 10 个 Phase 执行，而非一次性开发。

### P0 需求与环境

检查开发环境与约束。

### P1 骨架与数据库

建立可编译基线。

### P2 Model + DAO

完成数据访问。

### P3 Service

实现核心业务。

### P4 UI 骨架

搭建前端基础。

### P5 核心页面

打通业务闭环。

### P6 异常与并发通知

加入特色功能。

### P7 统计与备份

完成课程技术点。

### P8 测试与演示数据

提高稳定性。

### P9 答辩与交付

最终回归。

---

## 十一、Git 管理计划

推荐：

- `main`：稳定版本；
- `codex/development`：Codex 自动开发；
- 必要时 feature 分支。

提交示例：

```text
docs: add project plan and course constraints
phase-1: initialize maven and database schema
phase-2: add domain model and generic dao
phase-3: implement parcel lifecycle services
phase-4: build swing application shell
phase-5: complete core management screens
phase-6: add exception and async notification
phase-7: add statistics and backup
phase-8: improve tests and seed data
phase-9: finalize delivery and defense docs
```

Git 历史本身也是答辩证据。

---

## 十二、测试计划

至少覆盖：

- 登录成功/失败；
- 重复运单；
- 货架满；
- 货架停用；
- 正常入库；
- 正常出库；
- 重复出库；
- 错误取件码；
- 异常登记；
- 异常恢复；
- 通知失败重试；
- 数据库事务回滚；
- 并发任务关闭；
- 备份/恢复；
- 统计结果。

---

## 十三、主要风险

### 风险 1：Swing 卡顿

措施：

- EDT 只做 UI；
- 后台任务用 SwingWorker/ExecutorService。

### 风险 2：数据库配置不一致

措施：

- example 配置；
- 初始化脚本；
- 启动检查；
- 明确报错。

### 风险 3：状态混乱

措施：

- Service 统一状态机；
- UI 不直接改状态；
- 状态转换测试。

### 风险 4：功能过多导致不稳定

措施：

- 先闭环，后增强；
- Phase 闸门；
- 不接真实外部短信 API。

### 风险 5：课程判定“用了框架”

措施：

- 生产依赖严格控制；
- README 清晰列出依赖；
- 保证 JDBC/线程/序列化均由代码直接实现。

---

## 十四、最终答辩演示建议

推荐 8–10 分钟演示：

1. 展示 Git 提交历史；
2. 登录；
3. 首页统计；
4. 新增一个客户；
5. 入库一个包裹；
6. 查看货架和取件码；
7. 通知记录自动产生；
8. 用取件码出库；
9. 登记一个异常件；
10. 查看统计图；
11. 演示备份；
12. 打开技术点定位表讲 5 项 Java 技术。

整个流程覆盖业务完整性、课程技术和 Git 管理。
