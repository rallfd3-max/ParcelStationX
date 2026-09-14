# Phase H3 — 3D Interaction Browser Regression

## Real browser matrix

The real local Java/MySQL/Vite application was exercised as an administrator in the Digital Twin.

- Front physical row A-01: left `DEMO000001` / centre `DEMO000015` / last column `DEMO000006`.
- Rear physical row B-01: left `DEMO000031` / centre `DEMO000045` / last column `DEMO000036`.
- Each search-and-focus selection displayed a right-side detail panel with the exact requested tracking number and matching slot code.
- A real rendered parcel click selected `DEMO000015` at `A-01-03-03`; its parcel-adjacent tooltip immediately displayed tracking number, courier, status, slot, masked customer, arrival time and dwell time.
- Front/rear focus switched ten times without an observed flip, roll, scene duplication or reload requirement.
- A forced page refresh restored the single Digital Twin scene; the same parcel click again showed matching tooltip and detail.

## Quality gates

```text
mvn -q clean test          PASS
npm run type-check         PASS
npm run test -- --run      16 files / 46 tests PASS
npm run build              PASS (existing non-fatal large chunk warning)
```

## Console note

The development console retained historical Vite hot-reload errors from the transient period while a source edit was syntactically incomplete. After the final successful type-check/build and forced browser reload, the production source loaded and the interaction matrix completed normally; no runtime scene error was observed.
