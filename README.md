# Minicraft (transmute-games)

![Minicraft](https://minicraftplus.github.io/Minicraft_logo.png)

Private adaptation of Minicraft+ onto [transmute-core](https://github.com/transmute-games/transmute-core).

## Modules

| Module | Role |
|--------|------|
| `:common` | Protocol, `GameServer` / `GameClient`, shared types |
| `:client` | Transmute-backed game (`MinicraftGame`) |
| `:server` | Dedicated headless server |

## Prerequisites

- JDK 17+
- Publish transmute-core locally (or use JitPack):

```bash
cd ../transmute-core && ./gradlew :transmute-core:publishToMavenLocal
```

## Run

```bash
./gradlew :client:run
./gradlew :server:run --args='--port 4225'
```

Headless smoke:

```bash
./gradlew :client:verifyHeadless
./gradlew deadCodeCheck test
```

## Architecture

Transmute owns the window, game loop, and pixel presentation. Minicraft domain packages (`level`, `entity`, `item`, `screen`, `saveload`) remain adapted game code. Each frame, `MinicraftGame` ticks `Updater` and blits `Renderer` pixels into `Context`.

Multiplayer uses the Minicraft+ Online null-delimited protocol framing (`MinicraftProtocol`) with a LAN-authoritative `GameServer` in `:common`.

## License

Game code adapted from Minicraft+ / Minicraft+ Online under GPL-3.0 (see `LICENSE`, `NOTICE`). Engine dependency: transmute-core (PolyForm Shield).
