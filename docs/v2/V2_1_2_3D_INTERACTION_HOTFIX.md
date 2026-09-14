# ParcelStationX V2.1.2 — 3D Camera & Hover Interaction Hotfix

## 1. Problem statement

Real browser testing still exposes three user-visible 3D interaction defects:

1. Clicking a parcel does not reliably place the camera directly in front of that parcel.
2. Clicking parcels on the first physical shelf row can make the camera appear to flip/reverse to the wrong side.
3. Hovering a parcel does not reliably show the parcel information beside the parcel, although clicking can still load the right-side detail panel.

This hotfix is intentionally narrow. Do not mix it with V2.2 AI feature work or V2.3 dynamic shelf feature work.

## 2. Current code audit findings

### 2.1 Focus target is now the real parcel world position

`parcelFocus.ts` correctly resolves Parcel -> Slot -> Layout -> rendered Parcel Object3D and uses `object.getWorldPosition(...)`. This part should be preserved.

### 2.2 Camera placement is still unstable

`sceneMath.parcelCameraView(...)` currently creates four candidates:

- front;
- front + right;
- front + left;
- **back/opposite side**.

It picks the first candidate whose segment to the target does not intersect another layout. In the current warehouse, the physical front aisle for the first row can be partially blocked by the opposite row according to the simplified AABB intersection test. That can force the algorithm to choose the final back candidate. The result is a visible viewpoint reversal instead of a front-facing focus.

The camera animation also linearly interpolates camera position and OrbitControls target independently. When moving between opposite-facing shelf rows, the camera-to-target vector can become too small or cross through the target during interpolation, producing an apparent flip.

### 2.3 Hover is coupled to an async detail request

`HoverController` emits `{ parcelId, x, y }` from pointer movement, but `DigitalTwinView.showHover(...)` waits for `/api/parcels/{id}/details` before the tooltip can render because the template requires both `hover && hoverDetail`.

Pointer movement can therefore create repeated requests and races. The right-side detail works because click selection has a separate request path, while hover visibility is unnecessarily dependent on network timing.

The current tooltip position also follows mouse coordinates rather than being anchored to the rendered parcel world/screen position, so it is not a true parcel-adjacent label.

## 3. Camera correction design

### 3.1 Non-negotiable focus invariant

For an in-slot parcel, after focus completes:

```text
OrbitControls.target == actual rendered Parcel Object3D world position
```

within a small floating-point tolerance.

### 3.2 Front hemisphere invariant

The final camera must stay on the selected shelf's logical front side:

```text
dot(cameraPosition - target, shelfFront(layout)) > 0
```

Do not use an automatic opposite/back candidate for parcel focus.

If the exact straight-front position is constrained, shorten the camera distance or add a limited left/right offset while remaining in the same front hemisphere.

### 3.3 Aisle-aware front distance

Instead of using a fixed `max(width,height) * 1.65` that can place the camera behind another shelf row, calculate a desired front distance with lower/upper bounds, then reduce it when another shelf is closer along the front ray.

Suggested behavior:

- preferred parcel-camera distance: roughly 1.8m–3.0m depending on shelf dimensions;
- minimum safe distance: enough to see one parcel and nearby slot context;
- preserve a clearance margin from the opposite shelf/layout;
- never solve an obstruction by jumping to the back side.

A limited side offset may be used only if it still satisfies the front-hemisphere dot-product invariant.

### 3.4 Flip-free camera animation

Do not linearly lerp `camera.position` and `controls.target` in a way that allows the camera to pass through the target.

Use a target-relative transition:

1. represent current camera offset as `camera.position - controls.target`;
2. represent desired offset as `desiredPosition - desiredTarget`;
3. interpolate azimuth around world Y using the shortest angular path;
4. interpolate radius and vertical component while enforcing a minimum radius;
5. interpolate the target separately;
6. reconstruct `camera.position = interpolatedTarget + interpolatedOffset`;
7. keep `camera.up = (0,1,0)` and call `controls.update()`.

Equivalent arc/spherical interpolation is acceptable if it guarantees no target crossing and no roll/inversion.

## 4. Hover tooltip correction design

### 4.1 Tooltip must appear immediately

Hover visibility must not wait for a network request.

Use the already loaded WarehouseSnapshot to render the immediate summary:

- tracking number;
- courier;
- parcel status;
- slot code / staging area;
- arrived-at and dwell text if already available.

Customer masked data may be loaded asynchronously only as an enhancement.

### 4.2 Do not refetch on every pointer pixel

When the pointer remains over the same parcel:

- update tooltip position;
- do not call the detail API again.

Fetch details only when `parcelId` changes and cache by parcelId for the lifetime of the DigitalTwinView.

### 4.3 Anchor tooltip beside the parcel

The tooltip should be attached visually to the parcel, not simply to the mouse.

Recommended implementation:

- get the hovered parcel Object3D world position;
- project it through the active camera to NDC;
- convert NDC to the canvas-local pixel coordinates;
- position the HTML tooltip with a small screen offset from that projected parcel point;
- clamp the final tooltip box inside the `.twin-canvas` bounds.

If the camera moves while the same parcel remains hovered, refresh the anchor position so the tooltip stays with the parcel.

### 4.4 Hover and drag separation

Normal hover must work while not dragging.

During an actual drag operation the tooltip may hide, but merely hovering or clicking without a drag must not permanently disable it.

## 5. Files to audit

At minimum:

```text
frontend/src/three/sceneMath.ts
frontend/src/three/CameraController.ts
frontend/src/three/parcelFocus.ts
frontend/src/three/HoverController.ts
frontend/src/three/SceneIndex.ts
frontend/src/three/DragController.ts
frontend/src/three/ParcelRenderer.ts
frontend/src/three/WarehouseScene.ts
frontend/src/views/DigitalTwinView.vue
frontend/src/styles/main.css
```

Also inspect existing frontend tests before changing APIs.

## 6. Required automated tests

Add or strengthen tests for all of the following:

### Camera

- focus target equals actual parcel mesh world position;
- first and last columns produce different targets;
- front and rear physical rows produce different targets;
- final camera remains in the selected shelf front hemisphere for rotation 0, PI, PI/2 and -PI/2;
- no focus path crosses within the configured minimum camera-target radius;
- transition between opposite-facing shelf rows does not invert the view;
- final camera `up.y` remains positive / world-up stable;
- a nearby opposite shelf causes distance shortening/side adjustment, not automatic back-side focus.

### Hover

- raycast over a parcel resolves its parcelId;
- tooltip summary can render from snapshot without waiting for HTTP details;
- moving within the same parcel does not trigger repeated detail fetches;
- changing to another parcel triggers at most one new detail request;
- tooltip anchor is based on parcel projection, not raw mouse-only coordinates;
- tooltip clamps inside canvas bounds;
- pointer leave clears tooltip;
- active drag hides hover and normal hover returns after drag finishes.

## 7. Browser acceptance

Use a real browser if available.

Test at least six representative parcels:

- first physical row: left / center / right;
- second physical row: left / center / right;
- include first and last shelf columns.

For every selected parcel confirm:

1. right detail panel shows the same tracking number;
2. camera ends directly in front of that parcel, not in front of another row;
3. the view does not flip/roll;
4. the selected parcel remains visible and near the center;
5. moving the mouse over the parcel shows a tooltip adjacent to that parcel;
6. tooltip shows correct tracking/courier/status/slot;
7. no F5 is required.

Repeat front-row <-> rear-row selections at least 10 times.

## 8. Quality gates

```text
cd frontend
npm run type-check
npm run test
npm run build

cd ..
mvn clean test
```

Do not mark the hotfix complete only because unit tests pass; browser acceptance is required when browser access exists.

## 9. Scope boundary

Do not implement AI, dynamic shelves, or new business APIs in this hotfix.

The purpose is only to make the existing Three.js parcel focus and hover behavior correct and stable before further V2.2/V2.3 work builds on it.
