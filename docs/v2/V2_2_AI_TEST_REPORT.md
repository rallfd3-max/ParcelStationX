# ParcelStationX V2.2 AI 测试报告

测试日期：2026-09-14。

| 项目 | 结果 | 说明 |
| --- | --- | --- |
| Java regression | 通过 | `mvn clean test`：56 tests，0 failure/error |
| MySQL integration | 通过 | `mvn -Pintegration-test verify`：2 IT，0 skipped |
| V2.2 索引 migration | 通过 | 已实际创建并核验 3 个非破坏性索引 |
| Frontend gates | 通过 | type-check、15 files/39 tests、production build |
| 安全 intent | 通过 | 单元测试覆盖注入、删除、Shell、Key/密码请求；浏览器也验证“删除所有快递”被拒绝 |
| 通知与 scheduler | 通过 | Fake/Mock 自动测试覆盖 commit、rollback、阶段去重、状态复查、失败重试和 close |
| MaiMaiYa short smoke | 通过 | 本机未提交配置下真实 OpenAI-Compatible 请求成功；未记录 Key |
| MaiMaiYa structured UI smoke | 外部超时 | 运营洞察在 30 秒超时，页面显示可重试错误，正常 Dashboard 未受影响 |

## 安全结论

模型不可执行 SQL、Shell、文件或数据库 mutation。AI 结果先经过 Java 枚举、过滤器和动作校验；敏感真实手机号与取件码不会发送给 Provider。AI 文案失败时使用 Java 固定模板。

## 真实环境限制

当前 Provider/model 可返回最小 smoke，但未在 30 秒内完成真实聚合运营洞察的结构化生成。因此 AI-5 不能标记 DONE；待调整 MaiMaiYa 账号可用模型、配额或超时策略后，应重新执行 V2.2 演示脚本第 1–4 项。此限制不影响 AI-1 至 AI-4 的确定性自动测试，也不影响核心业务安全性。
