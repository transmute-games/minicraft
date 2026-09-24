# Agent guide — minicraft

## Golden path

1. Ensure `games.transmute:transmute-core:1.0.0` is in mavenLocal (or JitPack).
2. `./gradlew deadCodeCheck :client:test :client:verifyHeadless :common:test`
3. Run: `./gradlew :client:run`
4. Dedicated server: `./gradlew :server:run --args='--port 4225'`

## Authoring seam

- Entry: `games.transmute.minicraft.MinicraftGame` extends `TransmuteCore`.
- Bootstrap: `minicraft.core.GameBootstrap` (no Swing window).
- Input: `InputBridge` copies Transmute/`SimulatedInput` into Minicraft `InputHandler`.
- Render: `Renderer.renderFrame()` → blit into `Context` pixels.
- Do not replace Minicraft `Level`/`Entity` with transmute `World` for full fidelity.

## Verify

```bash
./gradlew deadCodeCheck
./gradlew :client:verifyHeadless
./gradlew :client:test --tests games.transmute.minicraft.SinglePlayerPlaytest
./gradlew :common:test --tests minicraft.network.MultiplayerIntegrationTest
```

`deadCodeCheck` runs PMD unused-code rules on Transmute-authored packages (`games.transmute.**`, LAN `GameServer`/`GameClient`) and fails on empty type stubs (`scripts/check-empty-types.sh`).

## Baseline

Adapted from Minicraft+ `2.3.0-dev` (not unstable `main` infdev).
