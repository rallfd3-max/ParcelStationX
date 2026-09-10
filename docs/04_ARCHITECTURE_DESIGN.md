# 架构设计

## 1. 总体架构

```text
┌────────────────────────────┐
│ Swing UI                   │
│ Frame / Panel / Dialog     │
└─────────────┬──────────────┘
              │
┌─────────────▼──────────────┐
│ Service                    │
│ Validation / Transaction   │
│ State Machine / Tasks      │
└─────────────┬──────────────┘
              │
┌─────────────▼──────────────┐
│ DAO                        │
│ Generic CRUD / SQL         │
└─────────────┬──────────────┘
              │
┌─────────────▼──────────────┐
│ JDBC                       │
└─────────────┬──────────────┘
              │
┌─────────────▼──────────────┐
│ MySQL                      │
└────────────────────────────┘
```

## 2. 包结构

```text
src/main/java/com/parcelstationx/
├── app
│   └── ParcelStationApplication.java
├── config
│   ├── AppConfig.java
│   └── DatabaseConfig.java
├── model
├── dao
│   ├── BaseDao.java
│   └── impl
├── service
│   └── impl
├── ui
│   ├── frame
│   ├── panel
│   ├── dialog
│   └── component
├── task
├── backup
├── util
└── exception
```

## 3. 核心模型

### Parcel

最核心领域对象。

关键字段：

- id
- trackingNo
- courierCompany
- customerId
- shelfId
- pickupCode
- status
- arrivedAt
- pickedUpAt
- operatorId

### ParcelEvent

保存生命周期事件。

例如：

- CREATED
- STORED
- NOTIFIED
- PICKED_UP
- MARKED_EXCEPTION
- RETURNED

这样答辩时可以展示“快件不仅有当前状态，还有历史”。

## 4. 状态机设计

状态变化必须通过 `ParcelService`。

推荐函数：

```java
boolean canTransition(ParcelStatus from, ParcelStatus to)
```

任何 UI 都不得绕过 Service 直接改 parcel.status。

## 5. 事务边界

### 入库事务

同一事务：

- insert parcel
- update shelf occupied
- insert parcel_event
- insert operation_log

通知任务可在事务成功后异步提交。

### 出库事务

同一事务：

- update parcel
- update shelf
- insert parcel_event
- insert operation_log

任何一步失败必须 rollback。

## 6. 并发设计

统一：

```text
TaskExecutor
  ├── notificationExecutor
  ├── backupExecutor
  └── statisticsExecutor
```

但避免线程池过多。

课程项目建议统一 fixed thread pool 2–4 threads。

应用退出：

```java
executor.shutdown()
executor.awaitTermination(...)
```

## 7. UI 线程

Swing EDT：

- 创建 UI；
- 轻量事件。

后台：

- DB 查询；
- 备份；
- 统计；
- 大数据加载。

结果再回到 EDT 更新组件。

## 8. 错误处理

异常层次建议：

```text
AppException
├── ValidationException
├── DatabaseException
├── BusinessException
├── BackupException
└── AuthenticationException
```

UI 只显示友好信息。

日志记录完整异常。

## 9. 配置

提交：

`src/main/resources/application.example.properties`

不提交：

`application.properties`

示例：

```properties
db.url=jdbc:mysql://localhost:3306/parcel_station_x?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
db.username=root
db.password=CHANGE_ME
```

## 10. 安全与输入

课程项目最低要求：

- PreparedStatement 防 SQL 注入；
- 密码不可直接打印；
- 手机号基础校验；
- 运单号基础校验；
- 删除/恢复二次确认；
- 不允许 UI 构造任意 SQL。
