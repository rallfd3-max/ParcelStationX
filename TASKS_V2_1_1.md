# TASKS_V2_1_1

V2.1.1 hotfix rule: work only on camera-focus correctness and real ECharts line/pie visualization. Do not reopen unrelated V2.1 features.

## Phase H1 — Real Line/Pie Charts
Status: DONE

Replace the current bar-only chart behavior for trend/distribution sections. Add at least one real line chart for 7/14/30-day inbound/outbound/inventory trends and at least one real pie/donut chart for courier/status/exception distribution. Data must come from existing real dashboard APIs. Add loading/empty/resize/dispose behavior and frontend tests.

Acceptance:
- trend series render with ECharts `type: line`;
- distribution renders with ECharts `type: pie`;
- at least one pie/donut is visible on the home page;
- no mock business data;
- type-check/test/build pass.

## Phase H2 — Parcel Camera Focus Correctness
Status: DONE

Fix 3D focus so clicking a parcel on any of the eight real shelves focuses that exact parcel/shelf/slot rather than visually landing on the front row. Prefer the actual Parcel Object3D world position from SceneIndex as camera target, then derive view direction from the parcel's ShelfLayout. Verify parcelId/slotId mapping and front/back shelf orientation. Add automated scene/camera tests for left/right columns and front/rear shelf rows.

Acceptance:
- first/last columns on one shelf have distinct targets;
- A/B and C/D shelf rows focus different physical rows;
- focus target matches the clicked Parcel Mesh world position within tolerance;
- clicked tracking number/shelf/slot matches the right-side detail panel;
- front/rear shelf targets are not occluded by another shelf in the normal focus view;
- frontend tests/type-check/build pass.

## Phase H3 — Browser Regression
Status: TODO

Use a real browser when available. Verify line chart, pie chart, and parcel focus on at least six representative positions: front-left, front-right, rear-left, rear-right, first column and last column. Keep original V2 Phase 8 status truthful if external MySQL/browser acceptance remains incomplete.
