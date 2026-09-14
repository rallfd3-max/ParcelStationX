# TASKS_V2_2

V2.2 execution rule: start from the earliest TODO/IN_PROGRESS item. Each phase must be implemented, tested, fixed, documented, committed and pushed separately. After a phase passes, continue automatically to the next phase. Do not wait for user confirmation unless blocked by credentials, OS permission or an unavailable external environment.

Provider override: before any AI implementation, read `docs/v2/V2_2_AI_PROVIDER_MAIMAIYA.md`. V2.2 uses **MaiMaiYa** as the OpenAI-Compatible relay provider. The provider/account portal is `https://maimaiya.click/profile`; this portal URL must not be assumed to be the actual API Base URL. The real Base URL, chat path and model must come from MaiMaiYa's current account/API configuration and remain runtime-configurable.

## Phase AI-1 — AI Infrastructure
Status: DONE

Build the MaiMaiYa OpenAI-Compatible relay integration using Java 17 HttpClient. Add environment/config loading for provider, portal URL, actual API base URL, API key, model, chat path, timeout and max tokens. Keep secrets server-side only. Add AiClient abstraction, real MaiMaiYa-compatible relay implementation, fake test implementation, prompt catalog, structured-output validator and `/api/ai/status`. Handle timeout, invalid JSON, 401/403, 429 and 5xx without breaking core ParcelStationX features. Do not add Spring.

Required configuration contract:

```text
PARCEL_AI_ENABLED=true
PARCEL_AI_PROVIDER=maimaiya
PARCEL_AI_PORTAL_URL=https://maimaiya.click/profile
PARCEL_AI_BASE_URL=<MaiMaiYa 提供的实际 OpenAI-Compatible API Base URL>
PARCEL_AI_API_KEY=<secret>
PARCEL_AI_MODEL=<MaiMaiYa 当前账号可用模型>
PARCEL_AI_CHAT_PATH=<MaiMaiYa 实际兼容路径>
PARCEL_AI_TIMEOUT_SECONDS=15
PARCEL_AI_MAX_OUTPUT_TOKENS=1200
```

Acceptance:
- application starts with AI disabled and all legacy features still work;
- application starts with AI enabled when MaiMaiYa config exists;
- `/api/ai/status` reports `provider=maimaiya` without exposing credentials;
- profile/portal URL is never used as the request endpoint unless MaiMaiYa itself explicitly provides it as the API endpoint;
- API key never reaches frontend/logs/Git;
- fake client tests cover success/failure;
- `mvn clean test`, frontend type-check/test/build pass.

## Phase AI-2 — Operations Insight & Exception Advice
Status: DONE

Add homepage “AI运营洞察” based on real DashboardAnalyticsService data. Add exception “AI处置建议” based on selected exception/parcel context. Send only minimum/sanitized context to MaiMaiYa GPT. Structured outputs must be validated. AI advice must never automatically modify Parcel/Exception state; user confirmation continues through existing ExceptionService.

Acceptance:
- homepage AI insight shows summary/risks/recommendations;
- AI outage leaves ECharts and all normal pages usable;
- exception AI advice can fill/show a draft only;
- no raw phone/pickupCode/passwordHash enters prompts;
- tests cover invalid model output and fallback/error UI.

## Phase AI-3 — Global Assistant & 3D Link
Status: DONE

Add `/ai` global assistant page and navigation. Implement whitelist natural-language intents: PARCEL_SEARCH, OVERDUE_PARCELS, PARCEL_LOCATE, SHELF_AVAILABILITY, SHELF_UTILIZATION, UNRESOLVED_EXCEPTIONS, TODAY_SUMMARY, UNSUPPORTED. GPT through MaiMaiYa only parses intent/filters; Java validates and executes existing Service/DAO queries with PreparedStatement. Never execute model-generated SQL. Return constrained actions such as FOCUS_PARCEL. Reuse `/digital-twin?parcelId=...` / WarehouseScene.focusParcel for 3D navigation.

Acceptance:
- “找出超过7天没取的顺丰快递” works against real data;
- “A区还有多少空仓位” works;
- “定位 DEMO000019” can open/focus 3D;
- multiple parcel matches require user selection;
- prompt-injection inputs cannot execute SQL/files/shell/actions outside enum whitelist;
- tests cover intent/filters/action validation.

## Phase AI-4 — Intelligent Notifications
Status: DONE

Wire existing NotificationService/NotificationQueue/NotificationGateway into ParcelStationWebApplication and ParcelService. Add stable MockSmsGateway for course demo. Keep inbound notification strictly after successful transaction commit. Add safe notification content generation: deterministic fallback template plus optional MaiMaiYa AI placeholder-template polish; do not send real mobile/pickupCode to GPT. Add configurable overdue stages (default 7,10,15 days), ScheduledExecutorService scanner, persistent dedupe by parcel + notification stage, retry limits, send-time IN_STOCK recheck and shutdown cleanup.

Acceptance:
- successful inbound produces one INBOUND notification;
- rolled-back inbound produces none;
- gateway failure records FAILED and retry can succeed;
- Day 6 none, Day 7 one, Day 8/9 no duplicate, Day 10 one, Day 15 one;
- restart/re-scan does not repeat a stage;
- picked-up parcel never receives overdue reminder;
- AI copy failure falls back to deterministic template;
- mock mode clearly identifies itself and never claims external SMS delivery.

## Phase AI-5 — Full Regression, MaiMaiYa Real Relay Smoke & Defense Docs
Status: IN_PROGRESS

Run full Java/frontend/MySQL regression. If local MaiMaiYa credentials/config are available, perform a small real MaiMaiYa relay smoke test without logging secrets; otherwise record the external blocker and keep fake-client coverage green. If Computer Use can access an already-authorized local MaiMaiYa session, it may inspect `https://maimaiya.click/profile` only to obtain the actual API Base URL/model/config needed for the local environment. Do not copy full keys into chat, Git, screenshots or reports. Use browser/computer control when available to validate homepage AI insight, exception advice, global assistant, 3D focus, inbound notification and overdue demo. Update README, V2 test/final audit/demo/defense docs and add V2.2 test report/development logs.

Acceptance quality gates:

```text
mvn clean test
mvn -Pintegration-test verify   # when MySQL env is available
cd frontend
npm run type-check
npm run test
npm run build
```

Final V2.2 DONE requires:
- provider is explicitly MaiMaiYa and `/api/ai/status` reports it;
- actual MaiMaiYa API endpoint/model are runtime-configurable rather than hardcoded guesses;
- no committed AI/SMS credentials;
- no model-generated SQL execution path;
- all AI actions server-validated;
- AI failure does not block core business;
- notification scheduler has no thread leak;
- all deterministic tests pass;
- browser workflow is documented with truthful limitations.

AI-5 progress (2026-09-14): Java unit regression, real MySQL integration, frontend gates, V2.2 index migration, local login/dashboard, scheduler mock delivery and the read-only assistant injection rejection all passed. A short real MaiMaiYa OpenAI-Compatible smoke passed with the locally configured model. The provider timed out on the longer structured operations-insight request at the configured 30-second boundary; the UI displayed its retryable failure state and normal business data remained available. Keep this phase `IN_PROGRESS` until the configured provider/model can return the structured production prompts within the chosen timeout, then re-run the documented browser chain.
