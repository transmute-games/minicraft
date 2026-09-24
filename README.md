# Minicraft

![Minicraft](https://minicraftplus.github.io/Minicraft_logo.png)

[![CI](https://github.com/transmute-games/minicraft/actions/workflows/ci.yml/badge.svg)](https://github.com/transmute-games/minicraft/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Gradle](https://img.shields.io/badge/Gradle-8-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![Flox](https://img.shields.io/badge/Flox-dev_env-7C3AED)](https://flox.dev/)
[![Transmute](https://img.shields.io/badge/engine-transmute--core-1a1a2e)](https://github.com/transmute-games/transmute-core)
[![Spotless](https://img.shields.io/badge/format-google--java--format-4285F4)](https://github.com/diffplug/spotless)
[![Mermaid](https://img.shields.io/badge/docs-Mermaid-FF3670?logo=mermaid&logoColor=white)](https://mermaid.js.org/)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)

Private adaptation of [Minicraft+](https://github.com/MinicraftPlus/minicraft-plus-revived) onto [transmute-core](https://github.com/transmute-games/transmute-core): singleplayer survival plus LAN multiplayer.

## Status

Playable transmute-backed client with headless verify, Spotless formatting, dead-code gates, and a LAN `GameServer` / `GameClient`.

## Quick start

```bash
flox activate                 # JDK 17 + docs/dev tools
# publish transmute-core once if needed:
#   (cd ../transmute-core && ./gradlew :transmute-core:publishToMavenLocal)
./gradlew :client:run
```

```bash
./gradlew :server:run --args='--port 4225'
./gradlew spotlessCheck deadCodeCheck test :client:verifyHeadless
```

## Modules

| Module | Role |
|--------|------|
| `:common` | Protocol + LAN server/client |
| `:client` | Transmute-backed game |
| `:server` | Dedicated headless server |

## Docs

| Doc | Topic |
|-----|--------|
| [Getting started](docs/getting-started.md) | Install, Flox, first run |
| [Architecture](docs/architecture.md) | Platform vs game domain |
| [Development](docs/development.md) | Format, pre-commit, workflows |
| [Multiplayer](docs/multiplayer.md) | Protocol and LAN host/join |
| [Testing](docs/testing.md) | Headless playtests |
| [CI](docs/ci.md) | Pipelines and doc validation |

## License

Game code: GPL-3.0 (see `LICENSE`, `NOTICE`, `Credits.md`). Engine: transmute-core (PolyForm Shield).
