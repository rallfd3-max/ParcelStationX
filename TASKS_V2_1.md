# TASKS_V2_1

V2.1 execution rule: start from the earliest TODO/IN_PROGRESS item. Each phase must be implemented, tested, fixed, documented, committed and pushed separately. After a phase passes, continue automatically to the next phase. Do not wait for user confirmation unless blocked by credentials, OS permission or an unavailable external environment.

## Phase A — Warehouse Drag UX
Status: TODO

Fix the 2D warehouse so the center shelf canvas has its own vertical scroll viewport. Add drag-edge auto-scroll near the top/bottom edge, cleanup on drop/dragend/cancel, keep 409 rollback, and keep the non-drag “move to slot” fallback. Acceptance: at 1366x768 a waiting parcel can be dragged to the lowest visible/previously off-screen empty slot and remains there after refresh.

## Phase B — Expanded Warehouse & 3D Scene
Status: TODO

Add a safe incremental V2.1 migration. Expand demo/business layout to eight real shelves: A-01/A-02/B-01/B-02/C-01/C-02/D-01/D-02, about 240 slots total, without deleting existing parcels. Arrange real ShelfLayout records into zones and aisles. Add staging/pickup areas and improve reset/front/top camera framing for all layouts. Do not fake business shelves only in Three.js.

## Phase C — 3D Hover + Inbound/Outbound
Status: TODO

Add parcel hover raycast + HTML tooltip with tracking number, courier, status, shelf/slot, customer, masked mobile, arrival and dwell time. Disable normal hover while dragging. Add quick inbound on Digital Twin, reuse ParcelService.inbound, show slotId=null IN_STOCK parcels in a 3D staging zone, support dragging them to slots, and add selected-parcel outbound with pickup code and confirmation using ParcelService.outbound. Refresh 2D/3D/details/dashboard after successful mutations.

## Phase D — Home Analytics Merge
Status: TODO

Rename “驾驶舱” to “首页”, remove the separate analytics navigation item, redirect /analytics to /dashboard#analytics, and rebuild Dashboard as a vertically scrollable operations home. Add real ECharts sections for inbound/outbound trends, inventory trend, courier/status/exception distributions, shelf/zone utilization, dwell distribution, recent exceptions and recent operations. Add real Java dashboard trend/distribution/activity APIs and keep SQL in DAO/service layers.

## Phase E — Full Regression
Status: TODO

Run Java regression, frontend type-check/tests/build, MySQL integration when available, browser checks at 1366x768/1440x900/1920x1080, bottom-slot drag, expanded 3D overview, hover tooltip, 3D inbound/staging/relocate/outbound, long-scroll home, charts, rollback cases and repeated Three.js mount/dispose. Update docs. Keep original V2 Phase 8 IN_PROGRESS until real MySQL/browser acceptance is genuinely complete.
