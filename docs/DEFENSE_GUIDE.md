# 答辩要点

- 不用 SpringBoot：课程要求直接展示 JDBC、事务、线程和泛型。
- 选 Swing：JDK 原生桌面 UI，部署简单，适合课程演示。
- JDBC 防注入：DAO 约定只用 PreparedStatement。
- 入库与出库事务：快件、货架占用、事件和日志必须同时成功或回滚。
- 集合用于通知队列；泛型复用 DAO/结果容器；序列化保存备份元数据；线程避免耗时通知卡住 EDT。
- 同一快件不能重复出库：`ParcelStateMachine` 只允许 `IN_STOCK -> PICKED_UP`。
