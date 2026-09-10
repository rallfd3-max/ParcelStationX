# Phase 4 开发日志

## 完成内容

- 增加 `LoginFrame`、`MainFrame` 和 CardLayout 导航骨架。
- 应用在 Swing EDT 中创建界面，UI 未包含 SQL。

## 测试结果

- `mvn clean test`：通过。

## 发现的问题与修复

- 无代码错误。

## 剩余风险

- 运行 UI 需要可用数据库配置；Phase 5 将填充实际页面工作流。

## 第二轮实现

- LoginFrame 接收真实用户名和密码，不再无条件放行。
- 新增可复用 SearchBar、PaginationPanel 和 StatusBar。
- MainFrame 支持注入真实业务页面；组件继续只在 EDT 创建。
- 首次构建发现入口仍使用旧的无参数登录回调；已改为调用 `AuthenticationService` 并显示友好错误。

## 对下一阶段的影响

- 各核心工作区已有稳定导航位置，Phase 5 可独立填充其页面。
