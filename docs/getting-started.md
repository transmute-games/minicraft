# Getting started

## Requirements

- [Flox](https://flox.dev/) (recommended) **or** JDK 17+ on `PATH`
- Network access once to resolve Gradle / JitPack deps
- Local [transmute-core](https://github.com/transmute-games/transmute-core) publish (or JitPack)

## Enter the Flox environment

```bash
flox activate
```

This provides Temurin 17, `git`, `gh`, `pre-commit`, `markdownlint-cli2`, and `mermaid-cli`, installs the git pre-commit hook when missing, and defines aliases (`mc-run`, `mc-test`, `mc-fmt`, `mc-docs`, `mc-server`).

One-shot without an interactive shell:

```bash
flox activate -- ./gradlew :client:run
```

## Publish the engine (once)

```bash
cd ../transmute-core
./gradlew :transmute-core:publishToMavenLocal
cd ../minicraft
```

## Run the game

```bash
./gradlew :client:run
```

## Run a dedicated server

```bash
./gradlew :server:run --args='--port 4225'
```

Clients join over LAN using the protocol described in [multiplayer](multiplayer.md).

## Verify

```bash
./gradlew spotlessCheck deadCodeCheck test :client:verifyHeadless
```

See [testing](testing.md) and [development](development.md) for details.
