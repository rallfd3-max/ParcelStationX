# TASKS_V2_1_2 — 3D Interaction Hotfix

Execution rule: this is a focused correctness hotfix for the current V2.1/V2.2 baseline. Complete H1 -> H2 -> H3 without waiting for user confirmation unless blocked by unavailable browser/runtime access.

## H1 — Camera Front-Focus Correctness
Status: DONE

Fix parcel focus so the final camera is on the selected shelf's logical front side and looks at the actual rendered Parcel Object3D world position.

Requirements:
- preserve `resolveParcelFocus` actual Object3D world target;
- remove automatic opposite/back-side fallback for parcel focus;
- make camera distance aisle-aware and shorten/offset on obstruction while staying in the front hemisphere;
- replace target-crossing linear position interpolation with a flip-safe target-relative arc/spherical transition;
- keep world-up stable;
- do not break reset/front/top camera commands.

Acceptance:
- `OrbitControls.target` matches parcel mesh world position within tolerance;
- `dot(camera-target, shelfFront(layout)) > 0` after focus for rotation 0, PI, PI/2, -PI/2;
- first-row and rear-row selections do not flip the view;
- switching between opposite-facing rows 10 times remains stable;
- frontend type-check/test/build pass.

## H2 — Parcel-Adjacent Hover Tooltip
Status: TODO

Fix hover so parcel information appears immediately beside the hovered parcel.

Requirements:
- summary renders from WarehouseSnapshot synchronously;
- detail API is optional enhancement, not a prerequisite to visible tooltip;
- one parcel hover must not issue a request on every pointermove;
- cache detail by parcelId;
- compute tooltip anchor from parcel Object3D world position projected by active camera;
- clamp tooltip to canvas;
- hide only while truly dragging and restore after drag;
- preserve right-side click detail behavior.

Acceptance:
- hover over rendered parcel visibly shows tracking/courier/status/slot beside parcel;
- same parcel pointer movement does not refetch repeatedly;
- switching parcels fetches at most once per uncached parcel;
- pointer leave clears tooltip;
- tooltip remains in canvas bounds;
- frontend type-check/test/build pass.

## H3 — Browser Regression
Status: TODO

Run full regression after H1/H2.

Required:
- `mvn clean test`;
- frontend `npm run type-check`;
- frontend `npm run test`;
- frontend `npm run build`;
- real browser validation if available.

Browser matrix:
- first physical row: left/center/right parcels;
- second physical row: left/center/right parcels;
- include first/last shelf columns;
- repeat row-to-row focus at least 10 times;
- verify tooltip for all representative parcels.

Do not report DONE if browser access exists and these interactions are still visibly wrong.
