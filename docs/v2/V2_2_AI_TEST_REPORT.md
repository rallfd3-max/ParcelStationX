# ParcelStationX V2.2 AI 测试报告

测试日期：2026-09-14。

| 项目 | 结果 | 说明 |
| --- | --- | --- |
| Java regression | 通过 | `mvn clean test`：78 tests，0 failure/error |
| MySQL integration | 通过 | `mvn -Pintegration-test verify`：2 IT，0 skipped |
| V2.2 索引 migration | 通过 | 已实际创建并核验 3 个非破坏性索引 |
| Frontend gates | 通过 | type-check、16 files/46 tests、production build |
| 安全 intent | 通过 | 单元测试覆盖注入、删除、Shell、Key/密码请求；浏览器也验证“删除所有快递”被拒绝 |
| 通知与 scheduler | 通过 | Fake/Mock 自动测试覆盖 commit、rollback、阶段去重、状态复查、失败重试和 close |
| MaiMaiYa short smoke | 通过 | 本机未提交配置下真实 OpenAI-Compatible 请求成功；未记录 Key |
| MaiMaiYa structured UI smoke | 通过 | Java 压缩聚合 context 并限制 320 tokens / 20 seconds 后，真实 Dashboard 洞察约 9.6 秒成功返回并显示 3 条风险、3 条建议 |

## 安全结论

模型不可执行 SQL、Shell、文件或数据库 mutation。AI 结果先经过 Java 枚举、过滤器和动作校验；敏感真实手机号与取件码不会发送给 Provider。AI 文案失败时使用 Java 固定模板。

## 运行时降级

如果 MaiMaiYa 在 20 秒内不可用、限流或超时，Dashboard 会返回基于同一组实时聚合指标的确定性本地洞察，不会阻断图表或其他核心业务。模型返回非法 JSON 仍按安全校验拒绝，不使用伪造的 AI 结果。
