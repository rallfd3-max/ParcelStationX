# Phase AI-5 — Regression, MaiMaiYa Smoke & Defense Docs

日期：2026-09-14
状态：DONE

## 完成内容

- 执行 Java 56 项回归、2 项真实 MySQL integration、前端 type-check、39 项 Vitest 和 production build。
- 在真实 MySQL 中执行并核验 V2.2 三个非破坏性索引。
- 使用本机未提交的 MaiMaiYa 配置完成短 OpenAI-Compatible relay smoke；Key 未输出、未写日志或 Git。
- 启动 Java API/Vite，应用内浏览器验证登录、真实 Dashboard 指标和 AI Assistant 的危险输入拒绝。
- 核对 scheduler 的 Mock SMS 脱敏输出与现有自动测试。
- 更新 README、测试报告、功能审计、演示脚本、答辩指南与 V2.2 专项测试报告。

## 验证结果

```text
mvn clean test                         PASS (78 tests)
mvn -Pintegration-test verify          PASS (2 MySQL IT, 0 skipped)
npm run type-check                     PASS
npm run test -- --run                  PASS (16 files, 46 tests)
npm run build                          PASS
V2_2_0 index verification              PASS
MaiMaiYa short relay smoke             PASS
```

## 长结构化响应优化与最终验收

- 运营洞察改为 Java 先做本地聚合：只发送核心指标、Top 3 分区/货架利用率、快递与滞留分布，不再传递完整趋势与活动序列。
- 每次洞察调用限制为 320 output tokens / 20 seconds，响应最多 3 条风险、3 条建议。AI timeout、rate limit 或上游暂时失败时，使用确定性本地聚合洞察；非法结构化输出仍严格拒绝。
- 真实 MaiMaiYa 运营洞察 API 在约 9.6 秒成功返回合法 JSON；管理员在 Dashboard 点击后正常显示总结、3 条风险和 3 条建议。没有记录 Key 或 endpoint 凭据。
