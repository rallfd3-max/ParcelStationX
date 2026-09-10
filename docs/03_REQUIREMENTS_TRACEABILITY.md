# 课程要求追踪矩阵

## 1. 系统实现完整

对应：

- 登录
- 客户
- 货架
- 快件入库
- 库存
- 出库
- 异常
- 通知
- 统计
- 备份恢复
- 日志

验收：主流程完整运行。

---

## 2. 不使用现有业务框架

禁止：

- SpringBoot
- Spring
- Struts
- Hibernate
- MyBatis
- SSH
- SSM
- JPA

检查方式：

```bash
mvn dependency:tree
```

最终答辩展示 `pom.xml`。

---

## 3. 课程 5 技术点

| 技术 | 业务位置 | 计划类 |
|---|---|---|
| 集合 | 通知队列、统计、查询 | NotificationQueue, StatisticsService |
| 泛型 | DAO、结果包装 | BaseDao<T,ID>, Result<T>, PageResult<T> |
| 序列化 | 备份元数据/快照 | BackupMetadata, BackupService |
| 多线程 | 通知、统计、备份 | TaskExecutor, NotificationWorker |
| 数据库编程 | 全部持久化 | ConnectionFactory, XxxDaoImpl |

开发完成后 Codex 必须把“计划类”更新为实际类名、方法名和代码路径。

---

## 4. 常用类

### String

- 运单号；
- 手机号；
- 取件码；
- 字符串校验；
- 搜索关键字。

### Date / 时间类

- 入库时间；
- 出库时间；
- 异常时间；
- 通知时间；
- 统计时间。

至少有一处明确使用 `java.util.Date` 或能向教师解释课程 Date 与现代时间 API 的关系。

### Math

- 货架占用率；
- 平均滞留时长；
- 图表比例；
- 统计百分比。

---

## 5. Git

要求：

- main
- codex/development
- 分阶段 commit
- 每阶段开发日志

验收：

```bash
git log --oneline --graph
```

答辩展示提交历史。

---

## 6. 最终答辩定位表

开发完成后追加：

```text
技术：泛型
文件：
类：
方法：
为什么使用：
如果不用会怎样：
答辩演示方式：
```

五个技术点都必须补齐。
