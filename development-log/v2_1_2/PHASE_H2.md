# Phase H2 — Parcel-Adjacent Hover Tooltip

## Root cause

Tooltip rendering required an asynchronous parcel-details response and used raw pointer coordinates. Repeated pointer movement over one parcel re-entered the request path, while `DragController` treated pointer-down itself as an active drag and disabled hover immediately.

## Changes

- Build the visible tracking/courier/status/slot/arrival/dwell summary synchronously from WarehouseSnapshot.
- Keep customer details as an optional asynchronous enhancement with per-parcel cache and in-flight request de-duplication.
- Anchor the label to the hovered Object3D world position projected through the active camera; clamp it inside the canvas.
- Refresh the projected anchor from the render loop so it follows camera movement.
- Added a five-pixel drag threshold. Click selection remains immediate, while controls and hover are disabled only after real movement begins.
- Preserve pointer-leave cleanup and restore ordinary hover after drag completion.
- Added snapshot-summary and projection/clamping tests.

## Verification

```text
npm run type-check       PASS
npm run test -- --run   16 files / 46 tests PASS
npm run build            PASS (existing non-fatal large chunk warning)
```

The combined real-browser interaction matrix follows in H3.
