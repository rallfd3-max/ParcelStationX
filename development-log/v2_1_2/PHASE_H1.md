# Phase H1 — Camera Front-Focus Correctness

## Root cause

`parcelCameraView` offered an opposite-side fallback when an AABB obstruction rejected the straight-front candidates. `CameraController` also interpolated absolute camera position and target independently, allowing their relative vector to approach or cross zero between opposite-facing shelf rows.

## Changes

- Preserved the actual rendered parcel Object3D world target from `resolveParcelFocus`.
- Replaced the back-side candidate with deterministic front-only distance shortening and limited lateral offsets.
- Bounded parcel focus distance and retained a positive shelf-front dot product for every supported rotation.
- Replaced absolute linear camera movement with shortest-azimuth target-relative spherical interpolation.
- Enforced a minimum camera-target radius and restored world-up on every animation frame.
- Added coverage for real mesh targets, first/last columns, opposite rows, four rotations, obstruction shortening and transition radius stability.

## Verification

```text
npm run type-check       PASS
npm run test -- --run   15 files / 43 tests PASS
npm run build            PASS (existing non-fatal large chunk warning)
```

The complete six-parcel and repeated row-switch browser matrix is intentionally recorded in H3 after H2 is integrated, so camera and tooltip behavior are exercised together.
