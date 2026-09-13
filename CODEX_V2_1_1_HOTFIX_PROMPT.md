# CODEX_V2_1_1_HOTFIX_PROMPT

Continue ParcelStationX on branch `codex/visualization-v2`.

This run is a focused V2.1.1 hotfix. Do not add unrelated features and do not reopen already completed V2.1 work.

Read first:
1. `docs/v2/V2_1_1_CAMERA_CHART_HOTFIX_PLAN.md`
2. `TASKS_V2_1_1.md`
3. current `frontend/src/components/DataChart.vue`
4. current `frontend/src/views/DashboardView.vue`
5. current `frontend/src/three/CameraController.ts`
6. current `frontend/src/three/sceneMath.ts`
7. current `frontend/src/three/WarehouseScene.ts`
8. current `frontend/src/three/ParcelInteraction.ts`
9. current `frontend/src/three/SceneIndex.ts`
10. current V2.1 migration/layout data

Then execute H1 -> H2 -> H3 automatically without waiting for user confirmation.

## H1: Real line and pie charts

The current chart component is bar-only. Fix this properly.

Requirements:
- add a true ECharts line chart for inbound/outbound/inventory trends;
- add at least one true pie/donut chart on the home page;
- preferably use courier distribution as donut and keep status/exception distributions as pie/donut if useful;
- keep shelf/zone utilization as bar where bar is semantically correct;
- use real dashboard API data only;
- support legend, tooltip, resize, dispose, loading/empty;
- preserve dark industrial visual theme;
- if using a generic DataChart, make chart type explicit and typed instead of hardcoding `bar`;
- otherwise create dedicated typed components such as `TrendLineChart.vue` and `DistributionPieChart.vue`.

Add tests proving line series are `type: line` and pie series are `type: pie` rather than just testing labels.

## H2: Fix 3D parcel camera focus

Observed real bug: clicking a parcel on the second/rear row or second physical column of warehouse shelves can move the camera to the front-most shelf plane instead of the selected parcel's actual shelf.

Do not solve this by only changing one distance constant.

Audit the full mapping:
`raycast hit -> parcelId -> Parcel -> slotId -> ShelfSlot -> shelfId -> ShelfLayout -> rendered Parcel Object3D`.

Required implementation direction:
- add a real `WarehouseScene.focusParcel(parcelId)` or equivalent semantic method;
- locate the actual parcel Object3D through `SceneIndex`;
- call `updateWorldMatrix(true, false)` and `getWorldPosition()`;
- use that world position as the camera target;
- resolve the selected parcel's slot and shelf layout from business data;
- use shelf orientation only to choose the camera approach vector;
- do not use `slotId` as if it were `parcelId`;
- ensure right-side selected detail, parcel object and camera target all refer to the same parcel.

For the V2.1 layout:
- A/B row uses z around -3 and rotationY 0;
- C/D row uses z around +2 and rotationY near PI.

The focus view must approach the selected shelf from its actual front/aisle side and must not visually land on the other row.

Add occlusion-safe framing. At minimum, if the default camera-to-target segment passes through another shelf bounding box, choose a safer candidate view using the opposite/side offset while preserving the exact selected parcel as OrbitControls target.

Tests must include:
- same shelf first column vs last column -> different target.x;
- A-01 vs A-02 -> different targets;
- A/B front row vs C/D rear row -> physically different row focus;
- rotationY 0 and PI;
- camera target matches actual parcel mesh world position within tolerance;
- selected trackingNo/shelf/slot matches focused object;
- no parcelId/slotId semantic mix-up.

## H3: regression

Run:
- `mvn clean test`
- `cd frontend`
- `npm run type-check`
- `npm run test`
- `npm run build`

If browser/computer control is available, verify:
- line chart visible;
- pie/donut chart visible;
- click front-left parcel;
- click front-right parcel;
- click rear-left parcel;
- click rear-right parcel;
- click first column parcel;
- click last column parcel;
- each camera flight ends on the correct parcel/shelf/slot.

If a browser bug is found, fix it before declaring the hotfix complete.

For each H phase:
implement -> test -> fix -> update `TASKS_V2_1_1.md` -> write `development-log/v2_1_1/PHASE_HX.md` -> independent commit -> push -> automatically continue.

Do not merge main and do not create a PR.
