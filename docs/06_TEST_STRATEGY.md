# 测试策略

## 1. 测试目标

不仅证明“代码能编译”，还要证明：

- 业务状态正确；
- 数据一致；
- 异常可控；
- 线程可关闭；
- 数据可备份；
- 核心 UI 流程稳定。

## 2. 测试分层

### Unit

适合：

- pickupCode generator
- validation
- state transition
- statistics calculation
- serialization helper

### DAO Integration

适合：

- insert
- update
- find
- pagination
- duplicate constraints

### Service Integration

适合：

- inbound transaction
- outbound transaction
- exception flow
- shelf capacity
- rollback

### Concurrency

适合：

- notification queue
- retry
- shutdown
- task exception isolation

## 3. 必测清单

### Authentication

- 正确账号
- 错误密码
- disabled 用户
- 空用户名

### Customer

- 新增
- 修改
- 手机检索
- 非法手机号

### Shelf

- capacity = 0
- occupied == capacity
- disabled shelf
- normal allocation

### Inbound

- 正常入库
- trackingNo 重复
- customer 不存在
- shelf 满
- 事务中途失败

### Outbound

- 正常出库
- 错误 pickup code
- PICKED_UP 再出库
- RETURNED 出库
- shelf occupied 正确减少

### Exception

- 登记异常
- 重复处理
- 恢复库存
- 退回

### Notification

- 入库创建任务
- worker 成功
- worker 失败
- retry
- shutdown

### Backup

- serialize
- deserialize
- corrupt file
- version mismatch

### Statistics

- 0 数据
- 正常数据
- 百分比
- 平均时间
- Math 边界

## 4. 最终质量命令

```bash
mvn clean test
```

必须成功。

如有数据库测试 profile，则 README 中给出：

```bash
mvn -Pintegration-test clean verify
```

## 5. 回归测试脚本

最终由 Codex 生成：

`docs/FINAL_TEST_REPORT.md`

至少包含：

- 环境
- JDK
- Maven
- DB
- 测试总数
- 成功
- 失败
- 修复
- 最终结果

## 6. 手工答辩测试

答辩前人工至少执行：

1. 登录；
2. 添加客户；
3. 入库；
4. 查件；
5. 出库；
6. 异常件；
7. 统计；
8. 备份；
9. 退出；
10. 再启动检查数据仍存在。
