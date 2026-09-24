# Agent guide — minicraft

## Golden path

1. `flox activate` (Temurin 17 + docs tools).
2. Publish Java engine if needed: `(cd ../transmute-core && ./gradlew :transmute-core:publishToMavenLocal)`
   (multi-lang monorepo; Minicraft only uses the Java artifact).
3. `./gradlew spotlessApply deadCodeCheck test :client:verifyHeadless`
4. `./gradlew :client:run` / `./gradlew :server:run --args='--port 4225'`

Docs live under [`docs/`](docs/README.md). Keep the root README slim.

## Authoring seam

- Entry: `games.transmute.minicraft.MinicraftGame` extends `TransmuteCore`.
- Config: `.size(VIEW_W * SCALE, VIEW_H * SCALE).scale(1)` so the window is 3× not 9×
  (Transmute applies `size * scale` for the window); render nearest-neighbor upscales into Context.
- Bootstrap: `minicraft.core.GameBootstrap` (no Swing window).
- Input: `InputBridge` copies Transmute/`SimulatedInput` into Minicraft `InputHandler`.
- Render: `Renderer.renderFrame()` → blit/upscale into `Context` pixels.
- Do not replace Minicraft `Level`/`Entity` with transmute `World` for full fidelity.
- Prefer `PlaytestScript` for scripted input slices in headless tests.

## Verify

```bash
flox activate -- ./gradlew spotlessCheck deadCodeCheck test :client:verifyHeadless
flox activate -- bash -lc 'markdownlint-cli2 && ./scripts/check-mermaid.sh'
```

## Baseline

Adapted from Minicraft+ `2.3.0-dev` (not unstable `main` infdev).
