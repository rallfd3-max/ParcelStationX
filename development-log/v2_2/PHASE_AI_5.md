# Phase AI-5 — Regression, MaiMaiYa Smoke & Defense Docs

日期：2026-09-14
状态：IN_PROGRESS（真实 Provider 的长结构化响应超时）

## 完成内容

- 执行 Java 56 项回归、2 项真实 MySQL integration、前端 type-check、39 项 Vitest 和 production build。
- 在真实 MySQL 中执行并核验 V2.2 三个非破坏性索引。
- 使用本机未提交的 MaiMaiYa 配置完成短 OpenAI-Compatible relay smoke；Key 未输出、未写日志或 Git。
- 启动 Java API/Vite，应用内浏览器验证登录、真实 Dashboard 指标和 AI Assistant 的危险输入拒绝。
- 核对 scheduler 的 Mock SMS 脱敏输出与现有自动测试。
- 更新 README、测试报告、功能审计、演示脚本、答辩指南与 V2.2 专项测试报告。

## 验证结果

```text
mvn clean test                         PASS (56 tests)
mvn -Pintegration-test verify          PASS (2 MySQL IT, 0 skipped)
npm run type-check                     PASS
npm run test -- --run                  PASS (15 files, 39 tests)
npm run build                          PASS
V2_2_0 index verification              PASS
MaiMaiYa short relay smoke             PASS
```

## 限制与下一步

真实 MaiMaiYa 模型在 30 秒内没有完成 Dashboard 的长结构化运营洞察；前端正确显示 `AI service timed out.`，图表和普通业务没有中断。保留 AI-5 `IN_PROGRESS`，待 Provider/model 的长生成稳定后复测首页洞察、异常建议、助手只读查询和 3D focus 的真实 relay 链路。原 V2 Phase 8 的 Chrome/Edge WebGL 验收也仍按既有记录保持进行中。
