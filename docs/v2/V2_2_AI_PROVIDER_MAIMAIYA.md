# ParcelStationX V2.2 — MaiMaiYa AI 中转站接入说明

## 1. Provider 定位

V2.2 的 GPT 能力统一通过 **MaiMaiYa** 中转站接入。

用户提供的 MaiMaiYa 账号/API 管理入口：

```text
https://maimaiya.click/profile
```

该地址用于识别供应商、查看账号/API 信息和获取中转站配置来源。

**重要：`https://maimaiya.click/profile` 是管理/资料入口，不应直接假定为 OpenAI-Compatible API Base URL。**

Codex 实现时必须从 MaiMaiYa 页面/其提供的 API 配置中取得实际的 OpenAI-Compatible Base URL、模型名和 Key 使用方式；如果当前自动化环境无法读取登录后的资料，则保持 Base URL 与模型配置化，不得猜测或硬编码不存在的接口地址。

---

## 2. 运行时配置

统一使用后端环境变量：

```text
PARCEL_AI_ENABLED=true
PARCEL_AI_PROVIDER=maimaiya
PARCEL_AI_PORTAL_URL=https://maimaiya.click/profile
PARCEL_AI_BASE_URL=<MaiMaiYa 提供的实际 OpenAI-Compatible API Base URL>
PARCEL_AI_API_KEY=<MaiMaiYa Key，仅本机环境变量>
PARCEL_AI_MODEL=<MaiMaiYa 当前账号可用模型>
PARCEL_AI_CHAT_PATH=<按 MaiMaiYa 实际兼容接口配置>
PARCEL_AI_TIMEOUT_SECONDS=15
PARCEL_AI_MAX_OUTPUT_TOKENS=1200
```

规则：

- `PARCEL_AI_PORTAL_URL` 只是供应商管理入口；
- `PARCEL_AI_BASE_URL` 才是 Java `HttpClient` 实际请求目标；
- `PARCEL_AI_API_KEY` 不允许提交 Git；
- `PARCEL_AI_API_KEY` 不允许进入 Vue、localStorage、浏览器网络响应、日志和异常消息；
- 模型名不得硬编码，应使用 MaiMaiYa 当前可用模型配置；
- Chat path 不得默认拼接到产生 `/v1/v1/...` 之类错误地址。

---

## 3. 接入架构

```text
Vue
  -> ParcelStationX Java API
      -> AiClient
          -> OpenAiCompatibleAiClient
              -> MaiMaiYa OpenAI-Compatible API
```

禁止：

```text
Vue -> MaiMaiYa
```

原因是前端直连会泄露 API Key。

AI Provider 只影响 `AiClient` 外部请求，不改变：

- ParcelService；
- ExceptionService；
- DashboardAnalyticsService；
- WarehouseLayoutService；
- RelocationService；
- DAO/JDBC/MySQL；
- 2D/3D 数字孪生核心业务。

---

## 4. Codex 获取 MaiMaiYa 配置的规则

如果 Codex/Computer Use 可以访问本机已登录的 MaiMaiYa 页面：

1. 打开 `https://maimaiya.click/profile`；
2. 只读取完成接入所必需的 API 配置信息；
3. 确认实际 Base URL、兼容路径、可用模型；
4. Key 只写入本机环境变量或未提交的本地配置；
5. 不在聊天、开发日志、Git diff、测试报告中打印完整 Key；
6. 截图/日志中如果出现 Key 必须避免保存或进行脱敏。

如果页面需要用户重新登录、验证码或人工授权：

- Codex 只暂停在该外部授权步骤；
- 不要求用户把完整 Key 发送到聊天；
- 用户可以在本机终端自行设置环境变量后继续。

---

## 5. 兼容性要求

MaiMaiYa 被作为 OpenAI-Compatible relay 使用，但实现不得过度假设供应商永远完全等同 OpenAI 官方接口。

`OpenAiCompatibleAiClient` 应把以下内容配置化/有限兼容：

- Base URL；
- Chat API path；
- model；
- Authorization Bearer header；
- 请求超时；
- `choices[0].message.content` 常规响应；
- 401/403；
- 429；
- 5xx；
- 非 JSON/空 content。

如果 MaiMaiYa 实际返回格式与标准兼容格式有小差异，只允许增加明确、可测试的适配；禁止用脆弱字符串切割或吞掉错误。

---

## 6. `/api/ai/status` Provider 信息

建议返回：

```json
{
  "enabled": true,
  "configured": true,
  "provider": "maimaiya",
  "model": "configured-model"
}
```

不得返回：

- API Key；
- Authorization Header；
- 用户账号凭据；
- MaiMaiYa 私密资料。

---

## 7. 真实 Relay Smoke Test

Phase AI-5 的真实中转站测试必须明确针对 MaiMaiYa。

测试最小化：

1. `/api/ai/status` 显示 `provider = maimaiya`；
2. 发起一次非常短的 AI 请求；
3. 确认 HTTP 成功并能解析内容；
4. 确认 Key 未出现在日志；
5. 测试完成后不把真实凭据写入仓库。

如果没有可用 MaiMaiYa Key/登录授权，真实 smoke test 可以标记为外部阻塞，但 FakeAiClient、接口测试、安全测试和所有核心业务测试必须全部完成。

---

## 8. 与 V2.2 功能的关系

MaiMaiYa GPT 将用于：

1. 首页 AI 运营洞察；
2. 异常件 AI 处置建议；
3. 全局 AI 助手自然语言 Intent 解析；
4. AI 查询结果联动 3D 快件定位；
5. 可选通知文案润色。

以下行为仍由 Java 确定性逻辑完成，不能交给 MaiMaiYa/GPT：

- 入库/出库事务；
- 取件码校验；
- 仓位占用；
- 2D/3D 换位；
- 异常状态修改；
- 短信发送时机；
- 7/10/15 天提醒调度；
- 权限判断；
- SQL 执行。

本文件对 `V2_2_AI_INTELLIGENT_OPERATIONS_PLAN.md`、`V2_2_AI_FEASIBILITY_ANALYSIS.md` 和 `CODEX_V2_2_AI_PROMPT.md` 中所有“OpenAI-Compatible relay/用户中转站”描述做 Provider 具体化：**当前 Provider 为 MaiMaiYa，管理入口为 `https://maimaiya.click/profile`，实际 API endpoint 必须以 MaiMaiYa 账号页面/官方配置提供的信息为准。**
